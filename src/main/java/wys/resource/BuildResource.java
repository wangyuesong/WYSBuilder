package wys.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.security.GeneralSecurityException;
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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
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
        // gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
        // .initialRetryDelayMillis(10)
        // .retryMaxAttempts(10)
        // .totalRetryPeriodMillis(15000)
        // .build());
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
        gitClient.setOAuth2Token(headerToken);

        // Job name is userLogin + repoName + buildName(head commit's id)
        String jobName = userLogin + "/" + repoName + "/" + payload.getHeadCommit().getId();
        String projectUrl = payload.getRepository().getUrl();
        String url = payload.getRepository().getSshUrl();
        String commitMessage = payload.getHeadCommit().getMessage();
        String commiter = payload.getHeadCommit().getCommitter().getName();
        String commitTime = payload.getHeadCommit().getTimestamp();
        String commitUrl = payload.getHeadCommit().getUrl();

        String credentialsId = Constants.getCredentialsId();
        String targets = Constants.getTargets();
        String commitHash = payload.getHeadCommit().getId() + "";

        // Add build job, jobName is just commitHash
        queueService.add(TaskOptions.Builder.withUrl(Constants.getBuildWorkerUrl()).
                param("jobName", commitHash).
                param("projectUrl", projectUrl).
                param("url", url).
                param("targets", targets).
                param("commitHash", commitHash).
                param("credentialsId", credentialsId).
                method(Method.POST));

        String ref = payload.getRef();
        String buildBranch = ref.substring(ref.lastIndexOf('/') + 1);

        String jenkinsLogUrl = Constants.getJenkinsBuildLogUrlFromJobName(jobName);
        String serverUrl = Constants.getServerAddress(request);
        String gcsLogPath = Constants.getGCSBuildLogUrlFromUserLoginRepoNameJobNameAndServerURL(userLogin, repoName,
                jobName, serverUrl);

        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Key grandChildKey = KeyFactory.createKey(childKey, "Build", commitHash);

        Entity build = new Entity(grandChildKey);
        build.setProperty("name", commitHash);
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
        queueService.add(TaskOptions.Builder.withUrl(Constants.getLogFecthWorkerUrl(userLogin, repoName, jobName)).
                param("jenkinsLogUrl", jenkinsLogUrl).
                param("interval", "5000").
                param("currentOffset", "0").
                // Job full path is used for worker to update job status when finished
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
        gitClient.setOAuth2Token(headerToken);

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
     * @throws GeneralSecurityException
     */
    @GET
    @Path("/{buildName}")
    public Response getOneBuildDetail(@HeaderParam("Authentication") String headerToken,
            @PathParam("buildName") String buildName) throws IOException, EntityNotFoundException,
            GeneralSecurityException {
        logger.info("Fetch Build log: " + userLogin + "/" + repoName + "/" + buildName);
        String objectPath = userLogin + "/" + repoName + "/" + buildName;
        HashMap<String, String> result = new HashMap<String, String>();

        // If cannot find such file, means the log is not ready, tell client to retry
        if (!CloudStorageUtils.isObjectExist(objectPath)) {
            result.put("status", "LOG_NOT_AVAILABLE");
            result.put("log", "");
            return Response.ok().entity(result).build();
        }

        ByteArrayOutputStream byteOutputStream = CloudStorageUtils.getObject(objectPath);
        result.put("log", new String(byteOutputStream.toByteArray(), "UTF-8"));

        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Key grandChildKey = KeyFactory.createKey(childKey, "Build", buildName);

        Entity e = DatastoreUtils.getOneResultByKey(datastore, grandChildKey);
        if (e != null) {
            result.put("status", (String) e.getProperty("status"));
        }
        else {
            result.put("status", BuildResult.UNKNOWN.toString());
        }
        return Response.ok().entity(result).build();
    }

}
