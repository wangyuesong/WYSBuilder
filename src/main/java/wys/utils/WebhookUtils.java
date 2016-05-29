package wys.utils;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import wys.resource.RepoResource;

/**
 * @Project: wysbuilder
 * @Title: WebhookUtils.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 5, 2016 12:19:47 AM
 * @version V1.0
 */
public class WebhookUtils {

    /**
     * Description: Stupid method to create webhook
     * 
     * @param request
     * @param headerToken
     * @param repoName
     * @param userLogin
     *            void
     */
    public static AddhookResponse addWebhook(String hookReceiverUrl, String oauthToken, String repoName,
            String userLogin) {

        Client client = ClientBuilder.newClient();
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("name", "web");
        paraMap.put("active", Boolean.TRUE);
        paraMap.put("events", new String[] { "push", "pull_request" });
        HashMap<String, Object> innerParaHashMap = new HashMap<String, Object>();
        innerParaHashMap.put("url", hookReceiverUrl);
        innerParaHashMap.put("content_type", "json");
        paraMap.put("config", innerParaHashMap);
        WebTarget target = client.target(Constants.GITHUB_API_ENDPOINT + "/repos/" + userLogin + "/" + repoName
                + "/hooks");
        AddhookResponse r = target.request(MediaType.APPLICATION_JSON).
                header("Authorization", String.format("Bearer %s", oauthToken))
                .post(javax.ws.rs.client.Entity.entity(paraMap, MediaType.APPLICATION_JSON), AddhookResponse.class);
        return r;
    }

    /**
     * 
     * Description: Delete a webhook
     * 
     * @param request
     * @param oauthToken
     * @param repoName
     * @param userLogin
     * @param hookId
     * @return
     *         Response
     */

    public static Response deleteWebhook(String oauthToken, String userLogin, String repoName, String hookId) {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(Constants.GITHUB_API_ENDPOINT + "/repos/" + userLogin + "/" + repoName
                + "/hooks/" + hookId);
        Response r = target.request(MediaType.APPLICATION_JSON).
                header("Authorization", String.format("Bearer %s", oauthToken))
                .delete();
        return r;
    }

    public static class AddhookResponse
    {
        private String id;

        private String ping_url;

        private String updated_at;

        private String test_url;

        private Last_response last_response;

        private String[] events;

        private String name;

        private String created_at;

        private Config config;

        private String active;

        private String url;

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getPing_url()
        {
            return ping_url;
        }

        public void setPing_url(String ping_url)
        {
            this.ping_url = ping_url;
        }

        public String getUpdated_at()
        {
            return updated_at;
        }

        public void setUpdated_at(String updated_at)
        {
            this.updated_at = updated_at;
        }

        public String getTest_url()
        {
            return test_url;
        }

        public void setTest_url(String test_url)
        {
            this.test_url = test_url;
        }

        public Last_response getLast_response()
        {
            return last_response;
        }

        public void setLast_response(Last_response last_response)
        {
            this.last_response = last_response;
        }

        public String[] getEvents()
        {
            return events;
        }

        public void setEvents(String[] events)
        {
            this.events = events;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getCreated_at()
        {
            return created_at;
        }

        public void setCreated_at(String created_at)
        {
            this.created_at = created_at;
        }

        public Config getConfig()
        {
            return config;
        }

        public void setConfig(Config config)
        {
            this.config = config;
        }

        public String getActive()
        {
            return active;
        }

        public void setActive(String active)
        {
            this.active = active;
        }

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        @Override
        public String toString()
        {
            return "ClassPojo [id = " + id + ", ping_url = " + ping_url + ", updated_at = " + updated_at
                    + ", test_url = " + test_url + ", last_response = " + last_response + ", events = " + events
                    + ", name = " + name + ", created_at = " + created_at + ", config = " + config + ", active = "
                    + active + ", url = " + url + "]";
        }
    }

    public static class Last_response
    {
        private String message;

        private String status;

        private int code;

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }

        public int getCode()
        {
            return code;
        }

        public void setCode(int code)
        {
            this.code = code;
        }

        @Override
        public String toString()
        {
            return "ClassPojo [message = " + message + ", status = " + status + ", code = " + code + "]";
        }
    }

    public static class Config
    {
        private String content_type;

        private String url;

        public String getContent_type()
        {
            return content_type;
        }

        public void setContent_type(String content_type)
        {
            this.content_type = content_type;
        }

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        @Override
        public String toString()
        {
            return "ClassPojo [content_type = " + content_type + ", url = " + url + "]";
        }
    }

}
