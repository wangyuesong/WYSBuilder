package wys.resource;

import java.util.concurrent.ThreadFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.pojos.hookpayload.HookPayload;
import wys.utils.Constants;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.sun.istack.logging.Logger;

/**
 * @Project: wysbuilder
 * @Title: HookResource.java
 * @Package wys.resource
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 10, 2016 9:35:45 PM
 * @version V1.0
 */
public class BuildResource {
    String userLogin;
    String repoName;

    DatastoreService datastore;
    MemcacheService syncCache;
    Queue queueService;
    GitHubClient gitClient;
    RepositoryService repositoryService;
    UserService userService;
    ThreadFactory threadFactory;

    private static Logger logger = Logger.getLogger(BuildResource.class);

    public BuildResource(String userLogin, String repoName) {
        super();
        gitClient = new GitHubClient();
        System.out.println(userLogin);
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        queueService = QueueFactory.getDefaultQueue();
        repositoryService = new RepositoryService(gitClient);
        userService = new UserService(gitClient);
        threadFactory = ThreadManager.currentRequestThreadFactory();
        this.userLogin = userLogin;
        this.repoName = repoName;
    }

    /**
     * 
     * Description: This function create a build on jenkins server, use head commit hash as job's name.
     * 
     * @param headerToken
     * @param payload
     * @return
     *         Response
     */
    @POST
    public Response addBuild(@HeaderParam("Authentication") String headerToken, HookPayload payload,
            @Context HttpServletRequest request) {
        // if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
        // return Response.status(Response.Status.UNAUTHORIZED).build();
        // }

        String jobName = payload.getHeadCommit().getId();
        String projectUrl = payload.getRepository().getUrl();
        String url = payload.getRepository().getSshUrl();
        String credentialsId = "03f2a0cf-a27a-44bd-912f-b5c3e9c5117a";
        String targets = "clean install";
        // Actually it's commit id
        String branch = payload.getHeadCommit().getId();

        queueService.add(TaskOptions.Builder.withUrl("/doBuild").
                param("jobName", jobName).
                param("projectUrl", projectUrl).
                param("url", url).
                param("targets", targets).
                param("branch", branch + "").
                param("credentialsId", credentialsId).
                method(Method.POST));

        String ref = payload.getRef();
        String buildBranch = ref.substring(ref.lastIndexOf('/'));

        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Key grandChildKey = KeyFactory.createKey(childKey, "Build", payload.getHeadCommit().getId());
        Entity build = new Entity(grandChildKey);
        String jenkinsLogUrl = Constants.getJenkinsBuildLogUrlFromJobName(jobName);
        String serverUrl = "http://" + request.getLocalAddr() + ":" + request.getServerPort();

        String gcsLogPath = Constants.getGCSBuildLogUrlFromRepoNameJobNameAndServerURL(repoName, jobName, serverUrl);
        build.setProperty("name", jobName);
        build.setProperty("branch", buildBranch);
        build.setProperty("jenkins_log_url", jenkinsLogUrl);
        build.setProperty("gcs_log_path", repoName + "/" + jobName);
        // build.setProperty("log_last_fetch_time", "");
        build.setProperty("status", "");
        queueService.add(TaskOptions.Builder.withUrl("/gcs/" + repoName + "/" + jobName).
                param("jenkinsLogUrl", jenkinsLogUrl).
                param("interval", "5000").
                param("currentOffset", "0").
                method(Method.POST));

        datastore.put(build);

        return Response.ok().build();
    }
}
