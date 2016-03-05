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

import wys.utils.DatastoreUtils;
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
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        repositoryService = new RepositoryService(gitClient);
        userService = new UserService(gitClient);
        // System.err.println("Fuck");
        this.userLogin = userLogin;
    }

    public RepoResource() {
        super();
    }

    /**
     * 
     * Description: List all repositories of a user (from db/memecache)
     * 
     * @param headerToken
     * @return
     *         Response
     */
    @GET
    @Produces("application/json")
    public Response getAllRepos(@HeaderParam("Authentication") String headerToken) {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);

        // Repo cache key
        String reposCacheKey = DatastoreUtils.getUserReposCacheKey(userLogin);
        Object cacheResult = syncCache.get(reposCacheKey);
        if (cacheResult != null) {
            return Response.ok().entity(cacheResult).build();
        }

        Key k = KeyFactory.createKey("User", userLogin);
        Query query = new Query("Repository").setAncestor(k);
        List<Entity> entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        List<RepositoryModel> models = new ArrayList<RepoResource.RepositoryModel>();
        for (Entity e : entities) {
            RepositoryModel r = new RepositoryModel();
            convertEntityToModel(e, r);
            models.add(r);
        }
        GenericEntity<List<RepositoryModel>> genericModels =
                new GenericEntity<List<RepositoryModel>>(models) {
                };

        return Response.ok().entity(genericModels).build();
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
        convertEntityToModel(entity, r);
        return Response.ok().entity(r).build();
    }

    @POST
    @Path("{repoId}/addHook")
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
        // JSONObject obj = new JSONObject();
        // JSONObject configObject = new JSONObject();
        // try {
        // obj.put("name", "web");
        // obj.put("active", true);
        // obj.put("events", new String[] { "push", "pull_request" });
        // configObject.put("url", hookUrl);
        // configObject.put("content_type", "json");
        // obj.put("config", configObject);
        // } catch (JSONException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        User user = userService.getUser();
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("name", "web");
        paraMap.put("active", Boolean.TRUE);
        paraMap.put("events", new String[] { "push", "pull_request" });
        HashMap<String, Object> innerParaHashMap = new HashMap<String, Object>();
        innerParaHashMap.put("url", hookUrl);
        innerParaHashMap.put("content_type", "json");
        paraMap.put("config", innerParaHashMap);
        // ObjectWriter ow = new ObjectMapper().writer();
        // String json = ow.writeValueAsString(paraMap);
        // System.out.println(json);
        WebTarget target = client.target(GITHUB_API_ENDPOINT + "repos/" + user.getLogin() + "/" + repo.getName()
                + "/hooks");
        Response r = target.request(MediaType.APPLICATION_JSON).
                header("Authorization", String.format("Bearer %s", headerToken))
                .post(javax.ws.rs.client.Entity.entity(paraMap, MediaType.APPLICATION_JSON), Response.class);
        System.out.println("Url:" + GITHUB_API_ENDPOINT + "repos/" + user.getLogin() + "/" + repo.getName() + "/hooks");
        System.out.println("Status:" + r.getStatus());
        // GitHubResponse respone = gitClient.post(uri, params, new HashedMap().getClass().getT)
        return Response.ok().build();
    }

    @POST
    @Path("{repoId}/hookReceiver")
    @Produces("application/json")
    public Response receiveHook() {
        System.out.println("Hook received");
        return Response.ok().build();
    }

    /**
     * 
     * Description: Update a user repo from GithubAPI
     * 
     * @param headerToken
     * @return
     * @throws IOException
     *             Response
     */
    @PUT
    public Response syncRepos(@HeaderParam("Authentication") String headerToken) throws IOException {
        if (!HeaderUtils.checkHeader(headerToken, userId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);
        List<Repository> repos = repositoryService.getRepositories();
        for (Repository r : repos) {
            Key parentKey = KeyFactory.createKey("User", userId);
            Key childKey = KeyFactory.createKey(parentKey, "Repository", r.getId());
            Entity e = new Entity(childKey);
            convertGithubModelToEntity(r, e);
            datastore.put(e);
            // Invalidate cache
        }
        syncCache.delete(DatastoreUtils.getUserReposCacheKey(userId));
        return Response.ok().build();
    }

    /**
     * Description: Convert GithubAPIModel to entity. Used when save.
     * 
     * @param r
     * @param e
     *            void
     */
    private void convertGithubModelToEntity(Repository r, Entity e) {
        e.setProperty("repo_id", r.getId());
        e.setProperty("name", r.getName());
        e.setProperty("description", r.getDescription());
        e.setProperty("git_url", r.getGitUrl());
        e.setProperty("homepage", r.getHomepage());
        e.setProperty("master_branch", r.getMasterBranch());
        e.setProperty("created_at", r.getCreatedAt());
        e.setProperty("updated_at", r.getUpdatedAt());
    }

    /**
     * 
     * Description: Convert Entity to Model. Used when show
     * 
     * @param e
     * @param r
     *            void
     */
    private void convertEntityToModel(Entity e, RepositoryModel r) {
        r.setRepo_id((Long) e.getProperty("repo_id"));
        r.setName((String) e.getProperty("name"));
        r.setDescription((String) e.getProperty("description"));
        r.setGit_url((String) e.getProperty("git_url"));
        r.setHomepage((String) e.getProperty("homepage"));
        r.setMaster_branch((String) e.getProperty("master_branch"));
        r.setCreated_at(e.getProperty("created_at") == null ? null : e.getProperty("created_at").toString());
        r.setUpdated_at(e.getProperty("updated_at") == null ? null : e.getProperty("updated_at").toString());
    }

    /**
     * 
     * @Project: wysbuilder
     * @Title: RepoResource.java
     * @Package wys.resource
     * @Description: Repository Model
     * @author YuesongWang
     * @date Mar 2, 2016 1:38:19 AM
     * @version V1.0
     */
    @XmlRootElement
    public static class RepositoryModel {
        private long repo_id;
        private String name;
        private String description;
        private String git_url;
        private String homepage;
        private String master_branch;
        private String created_at;
        private String updated_at;

        public long getRepo_id() {
            return repo_id;
        }

        public void setRepo_id(long repo_id) {
            this.repo_id = repo_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getGit_url() {
            return git_url;
        }

        public void setGit_url(String git_url) {
            this.git_url = git_url;
        }

        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }

        public String getMaster_branch() {
            return master_branch;
        }

        public void setMaster_branch(String master_branch) {
            this.master_branch = master_branch;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

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
