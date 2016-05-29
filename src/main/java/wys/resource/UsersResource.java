package wys.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.GithubModelToEntityUtils;
import wys.utils.HeaderUtils;
import wys.viewmodel.RepositoryModel;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.sun.istack.logging.Logger;

/**
 * @Project: BuilderBackend
 * @Title: UserResource.java
 * @Package wys.user
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 11, 2016 12:48:40 AM
 * @version V1.0
 */
@Path("/{userLogin}")
public class UsersResource {

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    GitHubClient gitClient;
    RepositoryService repositoryService;
    UserService userService;
    MemcacheService syncCache;
    private static Logger logger = Logger.getLogger(UsersResource.class);
    
    public UsersResource() {
        super();
        gitClient = new GitHubClient();
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        repositoryService = new RepositoryService(gitClient);
        userService = new UserService(gitClient);
    }
    
    @SuppressWarnings("unchecked")
    @GET
    @Produces("application/json")
    public Response getAllRepos(@HeaderParam("Authentication") String headerToken,
            @PathParam("userLogin") String userLogin) {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);

        List<Entity> entities = null;
        // Repo cache key
        String reposCacheKey = DatastoreUtils.getUserReposCacheKey(userLogin);
        Object cacheResult = syncCache.get(reposCacheKey);
      
        if (cacheResult != null) {
            logger.info("Repos fetch from cache");
            entities = (List<Entity>) cacheResult;
        }
        else {
            Key k = KeyFactory.createKey("User", userLogin);
            Query query = new Query("Repository").setAncestor(k);
            entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
            syncCache.put(reposCacheKey, entities);
        }
        
        List<RepositoryModel> models = new ArrayList<RepositoryModel>();
        for (Entity e : entities) {
            RepositoryModel r = new RepositoryModel();
            EntityToViewModelUtils.convertEntityToRepoModel(e, r);
            models.add(r);
        }
        
        GenericEntity<List<RepositoryModel>> genericModels =
                new GenericEntity<List<RepositoryModel>>(models) {
                };

        return Response.ok().entity(genericModels).build();
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
    public Response syncRepos(@HeaderParam("Authentication") String headerToken,
            @PathParam("userLogin") String userLogin) throws IOException {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);
        List<Repository> repos = repositoryService.getRepositories();
        for (Repository r : repos) {
            Key parentKey = KeyFactory.createKey("User", userLogin);
            Key childKey = KeyFactory.createKey(parentKey, "Repository", r.getName());
            Entity e = new Entity(childKey);
            GithubModelToEntityUtils.convertRepoModelToEntity(r, e);
            datastore.put(e);
            // Invalidate cache
        }
        syncCache.delete(DatastoreUtils.getUserReposCacheKey(userLogin));
        return Response.ok().build();
    }

    /**
     * 
     * Description: Sub-resource for handling one repo related request
     * 
     * @param userLogin
     * @return
     *         RepoResource
     */
    @Path("/")
    
    public RepoResource getOneRepo(@PathParam("userLogin") String userLogin) {
        return new RepoResource(userLogin);
    }

}
