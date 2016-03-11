package wys.utils;

import java.net.URI;

import com.offbytwo.jenkins.JenkinsServer;

/**
 * @Project: wysbuilder
 * @Title: Constants.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 1, 2016 10:58:02 PM
 * @version V1.0
 */
public class Constants {
    public final static String CLIENT_ID = "2dec25a28baf921db035";
    public final static String CLIENT_SECRET = "ef560b8ceed05ae3ecf4a8872877871e91871991";
    public final static String REDIRECT_URI = "http://127.0.0.1:8080/rest/auth/github";
    public final static String GITHUB_API_ENDPOINT = "https://api.github.com";
    public final static String JENKINS_SERVER_API_ENDPOINT = "http://104.197.212.129";
    public final static String JENKINS_SERVER_JOB_API_ENDPOINT = JENKINS_SERVER_API_ENDPOINT + "/job";
    
    public final static String SERVER_BASE_URI = "http://169.231.17.81:8080";
  
}
