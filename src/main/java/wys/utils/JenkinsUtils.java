package wys.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.offbytwo.jenkins.JenkinsServer;

/**
 * @Project: wysbuilder
 * @Title: JenkinsUtils.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date May 27, 2016 4:13:03 PM
 * @version V1.0
 */
public class JenkinsUtils {

    public static class CredentialPair {
        private String endPoint;
        private String credential;

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }

        public String getCredential() {
            return credential;
        }

        public void setCredential(String credential) {
            this.credential = credential;
        }

        public CredentialPair(String endPoint, String credential) {
            super();
            this.endPoint = endPoint;
            this.credential = credential;
        }

    }

    // public static String[] jenkinsMasterPool = new String[] { "http://128.111.84.178:8080","http://104.197.153.90" };
    public static CredentialPair[] credentialPool  = new CredentialPair[]{
        new CredentialPair("http://128.111.84.178:8080", "d0a708cb-57c3-4cd2-9e3c-b6f094b492bc"),
        new CredentialPair("http://104.197.153.90", "9e9b1339-b553-45ab-ac18-9f9eba570c40")
    };
    
   
    public static int currentIndex = 0;

    public static String getAvailableJenkinsMasterEndpoint() throws URISyntaxException {
        while (true) {
            JenkinsServer jenkins = new JenkinsServer(new URI(credentialPool[currentIndex].getEndPoint()), "", "");
            if(jenkins.isRunning()){
                String returnValue = credentialPool[currentIndex].getEndPoint();
                currentIndex = (currentIndex + 1) % credentialPool.length;
                System.out.println(currentIndex);
                return returnValue;
            }
            else
                currentIndex = (currentIndex + 1) % credentialPool.length;
        }

    }

    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        while (true) {
            Thread.sleep(2000);
            System.out.println(JenkinsUtils.getAvailableJenkinsMasterEndpoint());
        }
    }

    /**
     * Description: TODO
     * 
     * @param jenkinsEndpointUrl
     * @return
     *         String
     */
    public static String getCredentialOf(String jenkinsEndpointUrl) {
        for(CredentialPair cp : credentialPool){
            if(cp.getEndPoint().equals(jenkinsEndpointUrl))
                return cp.getCredential();
        }
        return null;
    }

}
