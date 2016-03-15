package wys.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.utils.DatastoreUtils;
import wys.utils.EntityToViewModelUtils;
import wys.utils.GithubModelToEntityUtils;
import wys.utils.HeaderUtils;
import wys.utils.WebhookUtils;
import wys.utils.WebhookUtils.AddhookResponse;
import wys.viewmodel.BranchModel;
import wys.viewmodel.BuildModel;
import wys.viewmodel.RepositoryModel;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
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

    
    @GET
    public Response getOneRepoAllBranches(@HeaderParam("Authentication") String headerToken) throws IOException
    {
        if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        gitClient.setOAuth2Token(headerToken);

        Repository repository = repositoryService.getRepository(userLogin, repoName);
        List<RepositoryBranch> branches = repositoryService.getBranches(repository);
        
        List<BranchModel> returnModels = new ArrayList<BranchModel>();
        for (RepositoryBranch branch : branches) {
            Key parentKey = KeyFactory.createKey("User", userLogin);
            Key childKey = KeyFactory.createKey(parentKey, "Repository", repoName);
            Filter keyFilter = new FilterPredicate("branch", FilterOperator.EQUAL, branch.getName());
            Query q = new Query("Build").setAncestor(childKey).setFilter(keyFilter)
                    .addSort("commit_time", SortDirection.DESCENDING);
            List<Entity> builds = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
            List<BuildModel> buildModels = new ArrayList<BuildModel>();
            // Get lastest 5 builds
            for (int i = 0; i < (builds.size() > 5 ? 5 : builds.size()); i++) {
                BuildModel model = new BuildModel();
                EntityToViewModelUtils.convertEntityToBuildModel(builds.get(i), model);
                buildModels.add(model);
            }
            BranchModel branchModel = new BranchModel();
            branchModel.setBranch_name(branch.getName());
            branchModel.setBuilds(buildModels);
            returnModels.add(branchModel);
        }
        
        System.out.println(returnModels.size());
        GenericEntity<List<BranchModel>> genericModels =
                new GenericEntity<List<BranchModel>>(returnModels) {
                };
        return Response.ok().entity(genericModels).build();
    }

    // @SuppressWarnings("unchecked")
    // @PUT
    // public Response syncOneRepoAllBranches(@HeaderParam("Authentication") String headerToken) throws IOException
    // {
    // if (!HeaderUtils.checkHeader(headerToken, userLogin)) {
    // return Response.status(Response.Status.UNAUTHORIZED).build();
    // }
    //
    // Repository repository = repositoryService.getRepository(userLogin, repoName);
    // List<RepositoryBranch> branches = repositoryService.getBranches(repository);
    //
    // for (RepositoryBranch branch : branches) {
    // TypedResource commit = branch.getCommit();
    //
    // // commit.
    // }
    // return Response.ok().build();
    // }


}
