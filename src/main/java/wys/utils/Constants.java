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
    public final static String GITHUB_API_ENDPOINT = "https://api.github.com";
    public final static String JENKINS_SERVER_API_ENDPOINT = "http://104.197.212.129";
    public final static String JENKINS_SERVER_JOB_API_ENDPOINT = JENKINS_SERVER_API_ENDPOINT + "/job";

    /**
     * 
     * Description: Generate log url on jenkins server based on job's name.
     * 
     * @param jobName
     * @return
     *         String
     */
    public final static String getJenkinsBuildLogUrlFromJobName(String jobName) {
        // FIXME not sure if /1/ is ok
        return Constants.JENKINS_SERVER_JOB_API_ENDPOINT + "/" + jobName + "/1/" + "logText/progressiveHtml";
    }

    /**
     * Description: Generate log url(bucket/file) on Google Cloud Storage based on repoName and jobName
     * 
     * @param repoName
     * @param jobName
     * @return
     *         String
     */
    public static String getGCSBuildLogUrlFromUserLoginRepoNameJobNameAndServerURL(String userLogin,String repoName, String jobName,
            String serverUrl) {
        return serverUrl + "/rest/" + userLogin + "/" + repoName + "/" + jobName;
    }
}
