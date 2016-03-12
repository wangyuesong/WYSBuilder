package wys.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.pojos.hookpayload.HookPayload;
import wys.utils.Constants;

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
import com.offbytwo.jenkins.model.Executor;
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
    ExecutorService executorService;

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
        executorService = Executors.newCachedThreadPool();
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
    public Response addBuild(@HeaderParam("Authentication") String headerToken, HookPayload payload) {
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
                param("crednetialsId", credentialsId).
                method(Method.POST));

        String ref = payload.getRef();
        String buildBranch = ref.substring(ref.lastIndexOf('/'));

        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Key grandChildKey = KeyFactory.createKey(childKey, "Build", payload.getHeadCommit().getId());
        Entity build = new Entity(grandChildKey);
        String jenkinsLogUrl = Constants.getBuildLogUrlFromJobName(jobName);
        build.setProperty("name", jobName);
        build.setProperty("branch", buildBranch);
        build.setProperty("jenkins_log_url", jenkinsLogUrl);
        build.setProperty("gcs_log_path", repoName + "/" + jobName);
        // build.setProperty("log_last_fetch_time", "");
        build.setProperty("status", "");
        logger.info("Jenkins Url: " + jenkinsLogUrl);
        Future<Boolean> future = executorService.submit(new LogFetcherExecutor(jenkinsLogUrl, "", 5000));
        datastore.put(build);

        return Response.ok().build();
    }

    private static class LogFetcherExecutor implements Callable<Boolean> {

        private String jenkinsLogUrl;
        private String gcsLogPath;
        private int interval;
        private int currentOffset;
        private WebTarget target;
        private Client client;

        public LogFetcherExecutor(String jenkinsLogUrl, String gcsLogPath, int interval) {
            super();
            this.jenkinsLogUrl = jenkinsLogUrl;
            this.gcsLogPath = gcsLogPath;
            this.interval = interval;
            client = ClientBuilder.newClient();
            target = client.target(jenkinsLogUrl);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public Boolean call() throws Exception {
            while (true) {
                Response response = target.queryParam("start", currentOffset).request().get();
                String xTextSize = response.getHeaderString("X-Text-Size");
                currentOffset = Integer.parseInt(xTextSize);
                System.out.println(response.getEntity().toString());

                if (response.getHeaderString("X-More-Data") != null) {
                    Thread.sleep(interval);
                }
                else {
                    System.out.println("Finished");
                    break;
                }
            }
            return true;
            // return null;
        }

        private String readFromJenkins() throws UnsupportedEncodingException, IOException {
            URL url = new URL(jenkinsLogUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            InputStreamReader input = new InputStreamReader(httpConn
                    .getInputStream(), "utf-8");
            BufferedReader bufReader = new BufferedReader(input);
            String line = "";
            StringBuilder contentBuf = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                contentBuf.append(line);
            }
            return contentBuf.toString();
        }

    }
}
