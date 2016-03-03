package wys.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import wys.utils.DatastoreUtils;
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
import com.google.appengine.repackaged.com.google.api.client.util.store.DataStoreUtils;

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

    long userId;

    // Client client = ClientBuilder.newClient();
    DatastoreService datastore;
    MemcacheService syncCache;
    GitHubClient gitClient;
    RepositoryService repositoryService;

    public RepoResource(long userId) {
        super();
        gitClient = new GitHubClient();
        datastore = DatastoreServiceFactory.getDatastoreService();
        syncCache = MemcacheServiceFactory.getMemcacheService();
        repositoryService = new RepositoryService(gitClient);
//        System.err.println("Fuck");
        this.userId = userId;
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
        if (!HeaderUtils.checkHeader(headerToken, userId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);
        Key k = KeyFactory.createKey("User", userId);
        //Repo cache key
        String reposCacheKey = DatastoreUtils.getUserReposCacheKey(userId);
        Object cacheResult = syncCache.get(reposCacheKey);
        if (cacheResult != null) {
            return Response.ok().entity(cacheResult).build();
        }
        
        Query query = new Query("Repository").setAncestor(k);
        List<Entity> entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        List<RepositoryModel> models = new ArrayList<RepoResource.RepositoryModel>();
        for (Entity e : entities) {
            RepositoryModel r = new RepositoryModel();
            convertEntityToModel(e, r);
            models.add(r);
        }
        GenericEntity<List<RepositoryModel>> genericModels = 
                new GenericEntity<List<RepositoryModel>>(models) {};
                
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
    public Response syncRepos(@HeaderParam("Authentication") String headerToken) throws IOException {
        if (!HeaderUtils.checkHeader(headerToken, userId)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);
        List<Repository> repos = repositoryService.getRepositories();
        for (Repository r : repos) {
            Key userKey = KeyFactory.createKey("User", userId);
            Key k = KeyFactory.createKey("Repository", r.getId());
            Entity e = new Entity("Repository",r.getId(),userKey);
            convertGithubModelToEntity(r, e);
            datastore.put(e);
            // Invalidate cache
        }
        syncCache.delete(DatastoreUtils.getUserReposCacheKey(userId));
        return Response.ok().build();
    }

    /**
     * Description: Convert GithubAPIModel to entity
     * 
     * @param r
     * @param e
     *            void
     */
    private void convertGithubModelToEntity(Repository r, Entity e) {
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
     * Description: Convert Entity to Model
     * 
     * @param e
     * @param r
     *            void
     */
    private void convertEntityToModel(Entity e, RepositoryModel r) {
        r.setName((String) e.getProperty("name"));
        r.setDescription((String) e.getProperty("description"));
        r.setGit_url((String) e.getProperty("git_url"));
        r.setHomepage((String) e.getProperty("homepage"));
        r.setMaster_branch((String) e.getProperty("master_branch"));
        r.setCreated_at((String) e.getProperty("created_at").toString());
        r.setUpdated_at((String) e.getProperty("updated_at").toString());
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
        private String name;
        private String description;
        private String git_url;
        private String homepage;
        private String master_branch;
        private String created_at;
        private String updated_at;

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
