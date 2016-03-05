package wys.resource;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.resource.UsersResource.RepositoryModel;
import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.GithubModelToEntityUtils;
import wys.utils.HeaderUtils;
import wys.utils.WebhookUtils;
import wys.utils.WebhookUtils.AddhookResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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

    /**
     * 
     * Description: Add webhook to github. Update Repository model to have hook info.
     * 
     * @param request
     * @param headerToken
     * @param repoName
     * @return
     * @throws EntityNotFoundException
     * @throws IOException
     *             Response
     */

    @POST
    @Path("{repoName}/hook")
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
        if (repo == null)
            return Response.status(400).entity("No such repository").build();
        User user = userService.getUser();
        String userLogin = user.getLogin();

        // Call Github Webhook API to add hook
        // FIXME Need refactor
        
        String hookReceiverUrl = "http://" + request.getLocalAddr() + ":" + request.getServerPort() +
                request.getRequestURI().replace("hook", "hookReceiver");
        AddhookResponse hookResponse = WebhookUtils.addWebhook(hookReceiverUrl, headerToken, repoName, userLogin);

        // Receive response and save it to Repository model in datastore
        EmbeddedEntity hookEntity = new EmbeddedEntity();
        GithubModelToEntityUtils.convertAddhookResponseModelToEntity(hookResponse, hookEntity);
        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Entity entity = datastore.get(childKey);
        entity.setProperty("hook", hookEntity);
        datastore.put(entity);
        // Invalidate cache
        syncCache.delete(DatastoreUtils.getUserOneRepoCacheKey(userLogin, repoName));
        return Response.ok().entity("Webhook added").build();
    }

    /**
     * 
     * Description: Delete a webhook
     * @param headerToken
     * @param repoName
     * @return
     * @throws EntityNotFoundException
     * Response
     */
    @DELETE
    @Path("{repoName}/hook")
    public Response deleteWebhook(@HeaderParam("Authentication") String headerToken, @PathParam("repoName") String repoName) throws EntityNotFoundException {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Key parentKey = KeyFactory.createKey("User", userLogin);
        Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
        Entity entity = datastore.get(childKey);
        Object object = entity.getProperty("hook");
        if(object == null)
            return Response.status(400).entity("No hook existed").build();
        EmbeddedEntity e = (EmbeddedEntity)object;
        int hookId = (Integer)e.getProperty("id");
        WebhookUtils.deleteWebhook(headerToken, userLogin, repoName, hookId);
        entity.removeProperty("hook");
        // Invalidate cache
        syncCache.delete(DatastoreUtils.getUserOneRepoCacheKey(userLogin, repoName));
        
        return Response.ok().entity("Webhook deleted").build();
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
