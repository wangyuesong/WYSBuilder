package wys.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.pojos.hookpayload.HookPayload;
import wys.utils.CloudStorageUtils;
import wys.utils.Constants;
import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.HeaderUtils;
import wys.viewmodel.BuildModel;

import com.google.api.client.util.store.DataStoreUtils;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.offbytwo.jenkins.model.BuildResult;
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
    private final GcsService gcsService;

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
        gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
                .initialRetryDelayMillis(10)
                .retryMaxAttempts(10)
                .totalRetryPeriodMillis(15000)
                .build());
        this.userLogin = userLogin;
        this.repoName = repoName;
    }

    /**
     * 
     * Description: This function create a build on jenkins server, use head commit hash as job's name.
     * 
     * @param headerToken
     * @param payload
     * @param request
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
        String commitMessage = payload.getHeadCommit().getMessage();
        String commiter = payload.getHeadCommit().getCommitter().getName();
        String commitTime = payload.getHeadCommit().getTimestamp();
        String commitUrl = payload.getHeadCommit().getUrl();

        String credentialsId = "03f2a0cf-a27a-44bd-912f-b5c3e9c5117a";
        String targets = "clean install";
        // Actually it's commit id
        String branch = payload.getHeadCommit().getId();

        // Add build job
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

        String jenkinsLogUrl = Constants.getJenkinsBuildLogUrlFromJobName(jobName);
        String serverUrl = "http://" + request.getLocalAddr() + ":" + request.getServerPort();
        String gcsLogPath = Constants.getGCSBuildLogUrlFromUserLoginRepoNameJobNameAndServerURL(userLogin, repoName,
                jobName, serverUrl);

        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Key grandChildKey = KeyFactory.createKey(childKey, "Build", payload.getHeadCommit().getId());

        Entity build = new Entity(grandChildKey);
        build.setProperty("name", jobName);
        build.setProperty("branch", buildBranch);
        build.setProperty("jenkins_log_url", jenkinsLogUrl);
        build.setProperty("gcs_log_path", gcsLogPath);
        build.setProperty("commit_message", commitMessage);
        build.setProperty("commiter", commiter);
        build.setProperty("commit_time", commitTime);
        build.setProperty("commit_url", commitUrl);
        build.setProperty("status", BuildResult.UNKNOWN.toString());
        build.setProperty("duration", "");

        // Add fecth log job
        queueService.add(TaskOptions.Builder.withUrl("/gcs/" + repoName + "/" + jobName).
                param("jenkinsLogUrl", jenkinsLogUrl).
                param("interval", "5000").
                param("currentOffset", "0").
                // Job full path is used for worker to update job status when finished
                param("jobFullPath", userLogin + "/" + repoName + "/" + jobName).
                method(Method.POST));

        datastore.put(build);

        return Response.ok().build();
    }

    /**
     * 
     * Description: Get all one repository's all builds from datastore
     * 
     * @param headerToken
     * @param request
     * @return
     *         Response
     */
    @GET
    public Response getAllBuilds(@HeaderParam("Authentication") String headerToken, @Context HttpServletRequest request)
    {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);

        Query buildsQuery = new Query("Build").setAncestor(childKey);
        List<Entity> results = datastore.prepare(buildsQuery).asList(FetchOptions.Builder.withDefaults());
        List<BuildModel> buildModels = new ArrayList<BuildModel>();
        for (Entity e : results) {
            BuildModel b = new BuildModel();
            EntityToViewModelUtils.convertEntityToBuildModel(e, b);
            buildModels.add(b);
        }
        GenericEntity<List<BuildModel>> genericModels =
                new GenericEntity<List<BuildModel>>(buildModels) {
                };
        return Response.ok().entity(genericModels).build();
    }

    /**
     * 
     * Description: Get one build's log detail from GCS
     * 
     * @param buildName
     * @return
     * @throws IOException
     *             Response
     * @throws EntityNotFoundException
     */
    @GET
    @Path("/{buildName}")
    public Response getOneBuildDetail(@HeaderParam("Authentication") String headerToken,
            @PathParam("buildName") String buildName) throws IOException, EntityNotFoundException {
        logger.info("Fetch Build log:" + buildName); 
        HashMap<String, String> result = new HashMap<String, String>();
        GcsFilename fileName = new GcsFilename(repoName, buildName);
        
        //If cannot find such file, means the log is not ready, tell client to retry
        if (gcsService.getMetadata(fileName) == null) {
            result.put("status", "LOG_NOT_AVAILABLE");
            result.put("log", "");
            return Response.ok().entity(result).build();
        }

        GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, CloudStorageUtils.BUFFER_SIZE);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        CloudStorageUtils.copy(Channels.newInputStream(readChannel), byteOutputStream);
       
        result.put("log", new String(byteOutputStream.toByteArray(), "UTF-8"));

        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Key grandChildKey = KeyFactory.createKey(childKey, "Build", buildName);
        
        Entity e = DatastoreUtils.getOneResultByKey(datastore, grandChildKey);
        if (e != null) {
            result.put("status", (String) e.getProperty("status"));
        }
        else{
            result.put("status", BuildResult.UNKNOWN.toString());
        }
        return Response.ok().entity(result).build();
    }

}
