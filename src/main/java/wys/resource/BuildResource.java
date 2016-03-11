package wys.resource;

import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.pojos.hookpayload.HookPayload;
import wys.utils.Constants;
import wys.utils.HeaderUtils;

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
import com.google.appengine.repackaged.com.google.api.client.auth.openidconnect.IdToken.Payload;
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
        this.userLogin = userLogin;
        this.repoName = repoName;
    }

    @POST
    public Response addBuild(@HeaderParam("Authentication") String headerToken, HookPayload payload) {
        // if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
        // return Response.status(Response.Status.UNAUTHORIZED).build();
        // }
        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Entity build = new Entity("Build", childKey);
        datastore.put(build);
        long buildId = build.getKey().getId();
        String projectUrl = payload.getRepository().getUrl();
        String url = payload.getRepository().getSshUrl();
        String credentialsId = "";
        String targets = "clean install";
        // Actually it's commit id
        String branch = payload.getHeadCommit().getId();
        String ref = payload.getRef();
        String buildBranch = ref.substring(ref.lastIndexOf('/'));
        System.out.println("In add build");
        System.out.println(branch);
        System.out.println(buildBranch);
        queueService.add(TaskOptions.Builder.withUrl("/doBuild").
                param("jobName", buildId + "").
                param("projectUrl", projectUrl).
                param("url", url).
                param("targets", targets).
                param("branch", branch + "").
                method(Method.POST));

        System.out.println(buildId);

        return Response.ok().build();
        // queueService.add(TaskOptions.Builder.withUrl("/doBuild").
        // param("keyname", key).
        // param("value", value)
        // .method(Method.POST));

    }
}
