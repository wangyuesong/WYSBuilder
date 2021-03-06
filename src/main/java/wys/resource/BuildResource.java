package wys.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.pojos.hookpayload.HookPayload;
import wys.utils.CloudStorageUtils;
import wys.utils.Constants;
import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.HeaderUtils;
import wys.utils.JenkinsUtils;
import wys.viewmodel.BuildDetailModel;
import wys.viewmodel.BuildModel;

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
     * @throws URISyntaxException 
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBuild(@HeaderParam("Authentication") String headerToken, HookPayload payload,
            @Context HttpServletRequest request) throws URISyntaxException {
        // if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
        // return Response.status(Response.Status.UNAUTHORIZED).build();
        // }
        gitClient.setOAuth2Token(headerToken);

        String projectUrl = payload.getRepository().getUrl();
        String url = payload.getRepository().getSshUrl();
        String commitMessage = payload.getHeadCommit().getMessage();
        String commiter = payload.getHeadCommit().getCommitter().getName();
        String commitTime = payload.getHeadCommit().getTimestamp();
        // String commitTime =
        // DatastoreUtils.convertISO8601DateStringToddMMyyyy(payload.getHeadCommit().getTimestamp());
        String commitUrl = payload.getHeadCommit().getUrl();


        String targets = Constants.getTargets();
        String commitHash = payload.getHeadCommit().getId() + "";

        //Get one available Jenkins master endpoint
        String jenkinsEndpointUrl = JenkinsUtils.getAvailableJenkinsMasterEndpoint();
        //Get corresponding Credential
        String credentialsId = JenkinsUtils.getCredentialOf(jenkinsEndpointUrl);
        // Add build job, jobName is just commitHash
        queueService.add(TaskOptions.Builder.withUrl(Constants.getBuildWorkerUrl()).
                param("jobName", commitHash).
                //Project's page url
                param("projectUrl", projectUrl).
                //Jenkins's endpoint url
                param("jenkinsEndpointUrl", jenkinsEndpointUrl).
                //Some url..
                param("url", url).
                param("targets", targets).
                param("commitHash", commitHash).
                param("credentialsId", credentialsId).
                method(Method.POST));

        String ref = payload.getRef();
        String buildBranch = ref.substring(ref.lastIndexOf('/') + 1);
        //Progressive log
        String jenkinsLogUrl = Constants.getJenkinsBuildLogUrlFromCommitHash(commitHash, jenkinsEndpointUrl);
        System.out.println("Jenkins Endpoint location:" + jenkinsEndpointUrl);
        System.out.println("Progressive Log Location:" + jenkinsLogUrl);
        String serverUrl = Constants.getServerAddress(request);
        String gcsLogPath = Constants.getGCSBuildLogUrlFromUserLoginRepoNameJobNameAndServerURL(userLogin, repoName,
                commitHash, serverUrl);

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
        queueService.add(TaskOptions.Builder.withUrl(Constants.getLogFecthWorkerUrl(userLogin, repoName, commitHash)).
                param("jenkinsLogUrl", jenkinsLogUrl).
                param("jenkinsEndpointUrl",jenkinsEndpointUrl).
                param("interval", "5000").
                param("currentOffset", "0").

                // Job full path is used for worker to update job status when finished
                method(Method.POST));

        datastore.put(build);

        return Response.ok().build();
    }

    /**
     * 
     * Description: Get all one repository's all builds from datastore.
     * Or find the most recent build by giving paramter mostRecent=true
     * 
     * @param headerToken
     * @param request
     * @return
     *         Response
     * @throws GeneralSecurityException
     * @throws EntityNotFoundException
     * @throws IOException
     */
    @GET
    @Produces("application/json")
    public Response getAllBuilds(@HeaderParam("Authentication") String headerToken, @Context HttpServletRequest request)
            throws IOException, EntityNotFoundException, GeneralSecurityException
    {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);
        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);

        Query buildsQuery = new Query("Build").setAncestor(childKey);
        List<Entity> results = datastore.prepare(buildsQuery).asList(FetchOptions.Builder.withDefaults());

        // No builds available
        if (results.size() == 0)
        {
            BuildModel bm = new BuildModel();
            return Response.ok().entity(bm).build();
        }
        // Get most recent
        if (request.getParameter("mostRecent") != null && request.getParameter("mostRecent").equals("true")) {
            Entity e = results.get(0);
            for (int i = 1; i < results.size(); i++) {
                Date origin = DatatypeConverter.parseDateTime((String) e.getProperty("commit_time")).getTime();
                Date another = DatatypeConverter.parseDate((String) results.get(i).getProperty("commit_time"))
                        .getTime();
                if (another.after(origin)) {
                    e = results.get(i);
                }
            }

            return getOneBuildDetail(headerToken, (String) e.getProperty("name"));
        }

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
     * Description: Get one build's log detail from GCS(for log) and Datastore(for status)
     * 
     * @param buildName
     * @return
     * @throws IOException
     *             Response
     * @throws EntityNotFoundException
     * @throws GeneralSecurityException
     */
    @GET
    @Produces("application/json")
    @Path("/{buildName}")
    public Response getOneBuildDetail(@HeaderParam("Authentication") String headerToken,
            @PathParam("buildName") String buildName) throws IOException, EntityNotFoundException,
            GeneralSecurityException {
        logger.info("Fetch Build log from Datastore: " + userLogin + "/" + repoName + "/" + buildName);
        String objectPath = userLogin + "/" + repoName + "/" + buildName;
        BuildDetailModel result = new BuildDetailModel();

        // If cannot find such file, means the log is not ready, tell client to retry
        if (!CloudStorageUtils.isObjectExist(objectPath)) {
            result.setStatus("LOG_NOT_AVAILABLE");
            result.setLog("");
            return Response.ok().entity(result).build();
        }

        // Get log from GCS
        ByteArrayOutputStream byteOutputStream = CloudStorageUtils.getObject(objectPath);
        result.setLog(new String(byteOutputStream.toByteArray()));

        // Get status from datastore
        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Key grandChildKey = KeyFactory.createKey(childKey, "Build", buildName);

        Entity e = DatastoreUtils.getOneResultByKey(datastore, grandChildKey);
        if (e != null) {
            result.setStatus((String) e.getProperty("status"));
        }
        else {
            result.setStatus(BuildResult.UNKNOWN.toString());
        }

        return Response.ok().entity(result).build();
    }

}
