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
public class GcsExampleServlet extends HttpServlet {
    /**
     * 
     */

    private String jenkinsLogUrl;
    private int interval;
    private String jobFullPath;
    private String currentOffset;
    private WebTarget target;
    private Client client;
    private Queue queueService;
    private static Logger logger = Logger.getLogger(GcsExampleServlet.class);
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    private static final long serialVersionUID = 1L;
    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GcsFilename fileName = getFileName(req);
        GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, CloudStorageUtils.BUFFER_SIZE);
        CloudStorageUtils.copy(Channels.newInputStream(readChannel), resp.getOutputStream());

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GcsFilename fileName = getFileName(req);
        jenkinsLogUrl = req.getParameter("jenkinsLogUrl");
        interval = Integer.parseInt(req.getParameter("interval"));
        currentOffset = req.getParameter("currentOffset");
        jobFullPath = req.getParameter("jobFullPath");
        client = ClientBuilder.newClient();
        target = client.target(jenkinsLogUrl + "?start=" + currentOffset);
        queueService = QueueFactory.getDefaultQueue();

        String result = null;

        Response response = target.request().get();
        String xTextSize = response.getHeaderString("X-Text-Size");

        // Server not started yet
        if (xTextSize == null || xTextSize == "") {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            queueService.add(TaskOptions.Builder
                    .withUrl("/gcs/" + fileName.getBucketName() + "/" + fileName.getObjectName()).
                    param("jenkinsLogUrl", jenkinsLogUrl).
                    param("interval", "5000").
                    param("jobFullPath", jobFullPath).
                    param("currentOffset", "0").
                    method(Method.POST));
        }
        else {
            //Update build status in datastore
            JenkinsServer jenkins;
            try {
                String[] jobPath = jobFullPath.split("/");
                jenkins = new JenkinsServer(new URI(wys.utils.Constants.JENKINS_SERVER_API_ENDPOINT), "", "");
                Job job = jenkins.getJob(fileName.getObjectName());
                Key parentKey = KeyFactory.createKey("User", jobPath[0]);
                Key childKey = KeyFactory.createKey(parentKey, "Repository", jobPath[1]);
                Key grandChildKey = KeyFactory.createKey(childKey, "Build", jobPath[2]);
                Entity buildEntity = datastore.get(grandChildKey);
                if(buildEntity != null){
                    if(job != null){
                        BuildResult buildResult = job.details().getLastBuild().details().getResult();
                        if(buildResult != null){
                            buildEntity.setProperty("status", buildResult.toString());
                            datastore.put(buildEntity);
                        }
                    }
                }
                
                
            } catch (URISyntaxException | EntityNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            result = response.readEntity(String.class);
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Still more log, Invoke another request
            if (response.getHeaderString("X-More-Data") != null) {
                queueService.add(TaskOptions.Builder
                        .withUrl("/gcs/" + fileName.getBucketName() + "/" + fileName.getObjectName()).
                        param("jenkinsLogUrl", jenkinsLogUrl).
                        param("interval", "5000").
                        param("jobFullPath", jobFullPath).
                        param("currentOffset", xTextSize).
                        method(Method.POST));
            }
            // No more log
            else {
                logger.info("Fetching job final result: " + fileName.getObjectName());
                logger.info("Finished retriving log");
                
            }
        }
        // Read something
        if (result != null) {
            // Create it not exist
            if (gcsService.getMetadata(fileName) == null) {
                GcsOutputChannel outputChannel =
                        gcsService
                                .createOrReplace(fileName, new GcsFileOptions.Builder().mimeType("text/html").build());
                outputChannel.close();
            }

            GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0,
                    CloudStorageUtils.BUFFER_SIZE);

            ByteArrayOutputStream outputToBeAppended = new ByteArrayOutputStream();
            // Output file to a outputstream
            CloudStorageUtils.copy(Channels.newInputStream(readChannel), outputToBeAppended);
            // Append fetched content to it
            outputToBeAppended.write(result.getBytes());
            // Convert outputstream to inputstream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputToBeAppended.toByteArray());
            // Store it to GCS
            GcsOutputChannel outputChannel =
                    gcsService.createOrReplace(fileName, new GcsFileOptions.Builder().mimeType("text/html").build());
            CloudStorageUtils.copy(inputStream, Channels.newOutputStream(outputChannel));
        }
    }

    private GcsFilename getFileName(HttpServletRequest req) {
        String[] splits = req.getRequestURI().split("/", 4);
        if (!splits[0].equals("") || !splits[1].equals("gcs")) {
            throw new IllegalArgumentException("The URL is not formed as expected. " +
                    "Expecting /gcs/<bucket>/<object>");
        }
        return new GcsFilename(splits[2], splits[3]);
    }

}
