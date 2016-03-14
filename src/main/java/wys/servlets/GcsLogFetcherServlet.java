package wys.servlets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import wys.utils.CloudStorageUtils;
import wys.utils.Constants;
import wys.utils.CloudStorageUtils.GCSObjectPath;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.Job;
import com.sun.istack.logging.Logger;

/**
 * @Project: wysbuilder
 * @Title: GcsExampleServlet.java
 * @Package wys.servlets
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 11, 2016 11:57:17 PM
 * @version V1.0
 */
public class GcsLogFetcherServlet extends HttpServlet {
    /**
     * 
     */

    private String jenkinsLogUrl;
    private int interval;
    private String currentOffset;
    private WebTarget target;
    private Client client;
    private Queue queueService = QueueFactory.getDefaultQueue();;
    private static Logger logger = Logger.getLogger(GcsLogFetcherServlet.class);
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GCSObjectPath objectPath = getObjectPath(req);
        String objectPathString = objectPath.toString();

        jenkinsLogUrl = req.getParameter("jenkinsLogUrl");
        interval = Integer.parseInt(req.getParameter("interval"));
        currentOffset = req.getParameter("currentOffset");
        client = ClientBuilder.newClient();
        target = client.target(jenkinsLogUrl + "?start=" + currentOffset);

        String result = null;

        Response response = target.request().get();
        String xTextSize = response.getHeaderString("X-Text-Size");

        // Server not started that job yet, retry
        if (xTextSize == null || xTextSize == "") {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            queueService.add(TaskOptions.Builder
                    .withUrl("/" + objectPathString).
                    param("jenkinsLogUrl", jenkinsLogUrl).
                    param("interval", "5000").
                    param("currentOffset", "0").
                    method(Method.POST));
            return;
        }

        // Server started that job, append log to Cloud storage, update build status in datastore
        JenkinsServer jenkins;
        try {
            // Update build status in jenkins
            jenkins = new JenkinsServer(new URI(wys.utils.Constants.JENKINS_SERVER_API_ENDPOINT), "", "");
            Job job = jenkins.getJob(objectPath.getBuildName());
            Key parentKey = KeyFactory.createKey("User", objectPath.getUserLogin());
            Key childKey = KeyFactory.createKey(parentKey, "Repository", objectPath.getRepoName());
            Key grandChildKey = KeyFactory.createKey(childKey, "Build", objectPath.getBuildName());
            Entity buildEntity = datastore.get(grandChildKey);
            if (buildEntity != null) {
                if (job != null) {
                    BuildResult buildResult = job.details().getLastBuild().details().getResult();
                    if (buildResult != null) {
                        buildEntity.setProperty("status", buildResult.toString());
                        datastore.put(buildEntity);
                    }
                }
            }
            result = response.readEntity(String.class);

            // Wait for a while
            Thread.sleep(interval);

            // Append the log to the cloud storage
            ByteArrayOutputStream outputStream = null;
            if (CloudStorageUtils.isObjectExist(objectPath)) {
                outputStream = CloudStorageUtils.getObject(objectPath);
            } else {
                outputStream = new ByteArrayOutputStream();
            }
            outputStream.write(result.getBytes());
            CloudStorageUtils.uploadStream(objectPath, "text/html",
                    new ByteArrayInputStream(outputStream.toByteArray()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Still more log, Invoke another request
        if (response.getHeaderString("X-More-Data") != null) {
            queueService.add(TaskOptions.Builder
                    .withUrl("/" + objectPathString).
                    param("jenkinsLogUrl", jenkinsLogUrl).
                    param("interval", "5000").
                    param("currentOffset", xTextSize).
                    method(Method.POST));
        }
        // No more log
        else {
            logger.info("Fetching job final result: " + objectPathString);
            logger.info("Finished retriving log");
        }

    }

    private GCSObjectPath getObjectPath(HttpServletRequest req) {
        String[] splits = req.getRequestURI().split("/");
        for (int i = 0; i < splits.length; i++) {
            System.out.println(splits[i]);
        }
        if (!splits[0].equals("") || !splits[1].equals("gcs")) {
            throw new IllegalArgumentException("The URL is not formed as expected. " +
                    "Expecting /gcs/<bucket>/<object>");
        }
        return new GCSObjectPath(splits[2], splits[3], splits[4]);
    }

}
