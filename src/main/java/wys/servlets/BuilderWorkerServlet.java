package wys.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.sun.istack.logging.Logger;

/**
 * @Project: wysbuilder
 * @Title: BuilderWorkerServlet.java
 * @Package wys.servlets
 * @Description: Servlet Path : /doBuild
 * @author YuesongWang
 * @date Mar 11, 2016 12:27:43 AM
 * @version V1.0
 */
public class BuilderWorkerServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static Logger logger = Logger.getLogger(BuilderWorkerServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String jobName = request.getParameter("jobName");
        String projectUrl = request.getParameter("projectUrl");
        String url = request.getParameter("url");
        String credentialsId = request.getParameter("credentialsId");
        String targets = request.getParameter("targets");
        String commitHash = request.getParameter("commitHash");
        String jenkinsEndpointUrl = request.getParameter("jenkinsEndpointUrl");

        ServletContext context = getServletContext();
        try {
            ByteArrayOutputStream outputStream = createDom(context, projectUrl, url, credentialsId, targets, commitHash);
            JenkinsServer jenkins = new JenkinsServer(new URI(jenkinsEndpointUrl), "", "");
            
            logger.info("About to create job: " + jobName);
            jenkins.createJob(
                    jobName,
                    inputStream2String(new ByteArrayInputStream(outputStream.toByteArray())), false);
             Job job = jenkins.getJob(jobName);
             System.out.println("Try to start jenkins build on:" + job.getUrl());
             job.build(false);
//            Client client = ClientBuilder.newClient();
//            WebTarget target = client.target(Constants.JENKINS_SERVER_JOB_API_ENDPOINT + "/" + jobName + "/build");
//            Response r = target
//                    .request(MediaType.APPLICATION_JSON).post(null);
//            System.out.println("Post status" + r.getStatus());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    public static ByteArrayOutputStream createDom(ServletContext context, String p_projectUrl, String p_url,
            String p_credentialsId,
            String p_targets,
            String p_commitHash) throws ParserConfigurationException, SAXException, IOException, TransformerException,
            URISyntaxException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(context.getResourceAsStream("/WEB-INF/maven_template.xml"));

        Node projectUrl = doc.getElementsByTagName("projectUrl").item(0);
        // projectUrl.setTextContent("https://github.com/YuesongWang/TestJenkins/");
        projectUrl.setTextContent(p_projectUrl);

        Node url = doc.getElementsByTagName("url").item(0);
        // url.setTextContent("git@github.com:gabrielf/maven-samples.git");
        url.setTextContent(p_url);

        Node credentialsId = doc.getElementsByTagName("credentialsId").item(0);
        // credentialsId.setTextContent("03f2a0cf-a27a-44bd-912f-b5c3e9c5117a");
        credentialsId.setTextContent(p_credentialsId);

        Node targets = doc.getElementsByTagName("targets").item(0);
        // targets.setTextContent("clean install");
        targets.setTextContent(p_targets);

        Node branch = doc.getElementsByTagName("name").item(0);
        // branch.setTextContent("/refs/heads/master");
        branch.setTextContent(p_commitHash);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        ByteArrayOutputStream returnStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(returnStream);
        // StreamResult result2 = new StreamResult
        transformer.transform(source, result);
        return returnStream;
    }
}