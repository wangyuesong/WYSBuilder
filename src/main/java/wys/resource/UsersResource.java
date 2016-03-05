package wys.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.GithubModelToEntityUtils;
import wys.utils.HeaderUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

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

    public UsersResource() {
        super();
        gitClient = new GitHubClient();
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        repositoryService = new RepositoryService(gitClient);
        userService = new UserService(gitClient);
    }

    @GET
    @Produces("application/json")
    public Response getAllRepos(@HeaderParam("Authentication") String headerToken,
            @PathParam("userLogin") String userLogin) {
        System.out.println(userLogin);
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
        private boolean is_hooked;

        public boolean isIs_hooked() {
            return is_hooked;
        }

        public void setIs_hooked(boolean is_hooked) {
            this.is_hooked = is_hooked;
        }

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
}
