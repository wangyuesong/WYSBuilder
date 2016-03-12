package wys.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.pojos.hookpayload.HookPayload;
import wys.resource.UsersResource.RepositoryModel;
import wys.utils.Constants;
import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.HeaderUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.sun.istack.logging.Logger;

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
    Client client;

    private static Logger logger = Logger.getLogger(RepoResource.class);

    public RepoResource(String userLogin) {
        super();
        gitClient = new GitHubClient();
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        repositoryService = new RepositoryService(gitClient);
        userService = new UserService(gitClient);
        client = ClientBuilder.newClient();

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
        
        Entity entity = null;
        String reposCacheKey = DatastoreUtils.getUserOneRepoCacheKey(userLogin, repoName);
        Object cacheResult = syncCache.get(reposCacheKey);

        if (cacheResult != null) {
            logger.info("One repo fecth from cache");
            entity = (Entity) cacheResult;
            syncCache.put(repoName, entity);
        }
        else {
            Key parentKey = KeyFactory.createKey("User", userLogin);
            Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
            entity = datastore.get(childKey);
        }
        RepositoryModel r = new RepositoryModel();
        EntityToViewModelUtils.convertEntityToRepoModel(entity, r);

        return Response.ok().entity(r).build();
    }

    /**
     * 
     * Description: Sub-resource for handling one hook related request
     * 
     * @param userLogin
     * @return
     *         RepoResource
     */
    @Path("/{repoName}/hook")
    public HookResource getHook(@PathParam("repoName") String repoName) {
        return new HookResource(userLogin, repoName);
    }

    /**
     * 
     * Description: Sub-resource for handling branches related request
     * 
     * @param userLogin
     * @return
     *         RepoResource
     */
    @Path("/{repoName}/branches")
    public BranchResource getBranches(@PathParam("repoName") String repoName) {
        return new BranchResource(userLogin, repoName);
    }

    /**
     * 
     * Description: Sub-resource for handling builds related request
     * 
     * @param userLogin
     * @return
     *         RepoResource
     */
    @Path("/{repoName}/builds")
    public BuildResource getBuilds(@PathParam("repoName") String repoName) {
        return new BuildResource(userLogin, repoName);
    }

    @POST
    @Path("{repoName}/hookReceiver")
    @Produces("application/json")
    public Response receiveHook(@HeaderParam("Authentication") String headerToken,
            @PathParam("repoName") String repoName, @Context HttpServletRequest request,
            HookPayload payload) {
        logger.info("Request received");
        
        String serverUrl = "http://" + request.getLocalAddr() + ":" + request.getServerPort();
        WebTarget target = client.target(serverUrl);
        try {
            Response response = target
                    .path("rest").path(userLogin).path(repoName).path("builds")
                    .request(MediaType.APPLICATION_JSON).header("Authentication", headerToken)
                    .post(javax.ws.rs.client.Entity.entity(payload, MediaType.APPLICATION_JSON));
        }
        // FIXME Github api inconsistency
        catch (Exception e) {
            logger.info("Receive hook test message");
            return Response.ok().build();
        }
        return Response.ok().build();
    }
}
