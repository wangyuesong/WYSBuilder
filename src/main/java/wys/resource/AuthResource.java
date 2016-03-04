package wys.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import wys.resource.AuthResource.OAuthResponseModel.UserInfo;
import wys.utils.DatastoreUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * @Project: wysbuilder
 * @Title: AuthResource.java
 * @Package wys.user
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 17, 2016 10:41:52 PM
 * @version V1.0
 */
@Path("/auth")
public class AuthResource {

  
//    Client client = ClientBuilder.newClient();
//    String accessTokenUrl = "https://github.com/login/oauth/access_token";
    DatastoreService datastore;
    GitHubClient gitClient;
    
    
    public AuthResource() {
        super();
        gitClient  = new GitHubClient();
        datastore =  DatastoreServiceFactory.getDatastoreService();
    }

    @POST
    @Path("github")
    public OAuthResponseModel loginGithub(@Context HttpServletRequest request,
            @Context HttpServletResponse currentResponse, OAuthTokenModel model) throws IOException {
        gitClient.setOAuth2Token(model.getAccess_token());
        User u = new UserService(gitClient).getUser();
       
        createOrUpdateUser(model, u);
        //TODO NEED ADD ENCRYPTION HERE
        return new OAuthResponseModel(new UserInfo(u.getId(), u.getAvatarUrl(), u.getName(), u.getUrl(),u.getEmail()), model.getAccess_token());
    }

//    /** 
//     * Description: TODO
//     * @param u
//     * @param repositories
//     * void
//     */
//    private void createOrUpdateUserRepos(User u, List<Repository> repositories,Key userKey) {
//        List<Entity> repoEntities = new ArrayList<Entity>();
//        for(Repository repo: repositories){
//            Entity entity = new Entity("Repository",repo.getId(),userKey);
//            repoEntities.add(entity);
//        }
//        datastore.put(repoEntities);
//    }

    /** 
     * Description: Create/Update user
     * @param model
     * @param u
     * @param key
     * void
     */
    private void createOrUpdateUser(OAuthTokenModel model, User u) {
        Key userKey = KeyFactory.createKey("User", u.getId());
        Entity result = DatastoreUtils.getOneResultByKey(datastore, userKey);
        // User exists
        if (result == null) {
            result = new Entity("User", u.getId());
        }
        // Not exist before, create
        
        updateEntityWithGithubModelAndToken(result, u, model.getAccess_token());
        datastore.put(result);

    }

    /**
     * 
     * Description: Use GihubUserModel and token to update User entity
     * 
     * @param e
     * @param g
     *            void
     */
    private void updateEntityWithGithubModelAndToken(Entity e, User g, String builderToken) {
        e.setProperty("user_id", g.getId());
        e.setProperty("login",g.getLogin());
        e.setProperty("builderToken", builderToken);
        e.setProperty("url", g.getUrl());
        e.setProperty("name", g.getName());
        e.setProperty("avatar_url", g.getAvatarUrl());
        e.setProperty("email", g.getEmail());
    }
    
    public static class OAuthTokenModel {
        private String access_token;
        private String token_type;
        private String provider;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }

    public static class OAuthResponseModel {

        private UserInfo userInfo;
        private String userToken;

        public OAuthResponseModel() {
            super();
        }

        public OAuthResponseModel(UserInfo userInfo, String userToken) {
            super();
            this.userInfo = userInfo;
            this.userToken = userToken;
        }

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            this.userToken = userToken;
        }

        public static class UserInfo {
            private long id;
            private String avatar_url;
            private String name;
            private String url;
            private String email;

            
            public UserInfo(long id, String avatar_url, String name, String url, String email) {
                super();
                this.id = id;
                this.avatar_url = avatar_url;
                this.name = name;
                this.url = url;
                this.email = email;
            }
            

            public UserInfo() {
                super();
            }


            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getAvatar_url() {
                return avatar_url;
            }

            public void setAvatar_url(String avatar_url) {
                this.avatar_url = avatar_url;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

        }
    }
}
