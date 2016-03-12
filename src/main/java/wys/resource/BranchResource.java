package wys.resource;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.utils.DatastoreUtils;
import wys.utils.GithubModelToEntityUtils;
import wys.utils.HeaderUtils;
import wys.utils.WebhookUtils;
import wys.utils.WebhookUtils.AddhookResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.sun.istack.logging.Logger;

/**
 * @Project: wysbuilder
 * @Title: HookResource.java
 * @Package wys.resource
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 10, 2016 9:35:45 PM
 * @version V1.0
 */
public class BranchResource {
    String userLogin;
    String repoName;

    DatastoreService datastore;
    MemcacheService syncCache;
    GitHubClient gitClient;
    RepositoryService repositoryService;
    UserService userService;

    private static Logger logger = Logger.getLogger(BranchResource.class);

    public BranchResource(String userLogin, String repoName) {
        super();
        gitClient = new GitHubClient();
        System.out.println(userLogin);
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        repositoryService = new RepositoryService(gitClient);
        userService = new UserService(gitClient);
        this.userLogin = userLogin;
        this.repoName = repoName;
    }

    @SuppressWarnings("unchecked")
    @GET
    public Response getOneRepoAllBranches(@HeaderParam("Authentication") String headerToken) throws IOException
    {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<Entity> entities = null;
        // Repo cache key
        String reposCacheKey = DatastoreUtils.getUserOneRepoBranchesCacheKey(userLogin, repoName);
        Object cacheResult = syncCache.get(reposCacheKey);

        if (cacheResult != null) {
            logger.info("Repos fetch from cache");
            entities = (List<Entity>) cacheResult;
        }
        else {
            Key parentKey = KeyFactory.createKey("User", userLogin);
            Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
            Query query = new Query("Repository").setAncestor(childKey);
            entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
            syncCache.put(reposCacheKey, entities);
        }

        return Response.ok().entity(entities).build();
    }

    @SuppressWarnings("unchecked")
    @PUT
    public Response syncOneRepoAllBranches(@HeaderParam("Authentication") String headerToken) throws IOException
    {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Repository repository = repositoryService.getRepository(userLogin, repoName);
        List<RepositoryBranch> branches = repositoryService.getBranches(repository);

        for (RepositoryBranch branch : branches) {
            TypedResource commit = branch.getCommit();
            // commit.
        }
        return Response.ok().build();
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
    public Response addWebhook(@Context HttpServletRequest request,
            @HeaderParam("Authentication") String headerToken)
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
        logger.info(userLogin + "/" + repoName + "add hook");
        return Response.ok().entity("Webhook added").build();
    }

}
