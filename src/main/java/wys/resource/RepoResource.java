package wys.resource;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.resource.UsersResource.RepositoryModel;
import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.HeaderUtils;

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
import com.google.appengine.repackaged.org.apache.commons.httpclient.UsernamePasswordCredentials;

/**
 * @Project: wysbuilder
 * @Title: AuthResource.java
 * @Package wys.user
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 17, 2016 10:41:52 PM
 * @version V1.0
 */
public class RepoResource {

    String userLogin;
    // Client client = ClientBuilder.newClient();
    DatastoreService datastore;
    MemcacheService syncCache;
    GitHubClient gitClient;
    RepositoryService repositoryService;
    UserService userService;
    public final static String GITHUB_API_ENDPOINT = "https://api.github.com/";

    public RepoResource(String userLogin) {
        super();
        gitClient = new GitHubClient();
        System.out.println(userLogin);
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        repositoryService = new RepositoryService(gitClient);
        userService = new UserService(gitClient);
        this.userLogin = userLogin;
    }
   
    /**
     * 
     * Description: Get a repository
     * 
     * @param headerToken
     * @param repoId
     * @return
     *         Response
     * @throws EntityNotFoundException
     */
    @GET
    @Path("{repoName}")
    @Produces("application/json")
    public Response getOneRepo(@HeaderParam("Authentication") String headerToken, @PathParam("repoName") String repoName)
            throws EntityNotFoundException {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);

        String reposCacheKey = DatastoreUtils.getUserOneRepoCacheKey(userLogin, repoName);
        Object cacheResult = syncCache.get(reposCacheKey);
        if (cacheResult != null) {
            return Response.ok().entity(cacheResult).build();
        }
        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);

        Entity entity = datastore.get(childKey);
        RepositoryModel r = new RepositoryModel();
        EntityToViewModelUtils.convertEntityToRepoModel(entity, r);
        return Response.ok().entity(r).build();
    }

    @POST
    @Path("{repoName}/addHook")
    public Response addWebhook(@Context HttpServletRequest request,
            @HeaderParam("Authentication") String headerToken, @PathParam("repoName") String repoName)
            throws EntityNotFoundException, IOException {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);
       
        Repository repo = null;
        List<Repository> repos = repositoryService.getRepositories();
        for (Repository r : repos) {
            if (r.getName().equals(repoName))
                repo = r;
        }
        if(repo == null)
            return Response.status(400).entity("No such repository").build();

        String hookUrl = "http://" + request.getLocalAddr() + ":" + request.getServerPort() +
                request.getRequestURI().replace("addHook", "hookReceiver");
        Client client = ClientBuilder.newClient();
        User user = userService.getUser();
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("name", "web");
        paraMap.put("active", Boolean.TRUE);
        paraMap.put("events", new String[] { "push", "pull_request" });
        HashMap<String, Object> innerParaHashMap = new HashMap<String, Object>();
        innerParaHashMap.put("url", hookUrl);
        innerParaHashMap.put("content_type", "json");
        paraMap.put("config", innerParaHashMap);
        WebTarget target = client.target(GITHUB_API_ENDPOINT + "repos/" + user.getLogin() + "/" + repo.getName()
                + "/hooks");
        Response r = target.request(MediaType.APPLICATION_JSON).
                header("Authorization", String.format("Bearer %s", headerToken))
                .post(javax.ws.rs.client.Entity.entity(paraMap, MediaType.APPLICATION_JSON), Response.class);
        // GitHubResponse respone = gitClient.post(uri, params, new HashedMap().getClass().getT)

        return Response.ok().entity("Web hook added").build();
    }

    @POST
    @Path("{repoName}/hookReceiver")
    @Produces("application/json")
    public Response receiveHook() {
        System.out.println("Hook received");
        return Response.ok().build();
    }

  

   
 
  

    @XmlRootElement
    public static class SetHookModel {

        private String name;
        private boolean active;
        private String[] events;
        private SetHookModelConfig config;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String[] getEvents() {
            return events;
        }

        public void setEvents(String[] events) {
            this.events = events;
        }

        public SetHookModelConfig getConfig() {
            return config;
        }

        public void setConfig(SetHookModelConfig config) {
            this.config = config;
        }

        public static class SetHookModelConfig {
            String url;
            String content_type;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getContent_type() {
                return content_type;
            }

            public void setContent_type(String content_type) {
                this.content_type = content_type;
            }

        }

    }

}
