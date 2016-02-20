package wys.user;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import wys.models.GithubUserModel;
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
    
    public final static String CLIENT_ID = "2dec25a28baf921db035";
    public final static String CLIENT_SECRET ="ef560b8ceed05ae3ecf4a8872877871e91871991";
    public final static String REDIRECT_URI = "http://127.0.0.1:8080/rest/auth/github";
    public final static String GITHUB_API_ENDPOINT = "https://api.github.com/";
    Client client = ClientBuilder.newClient();
    String accessTokenUrl = "https://github.com/login/oauth/access_token";
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    
    @GET
    @Path("github")
    public void loginGithub(@Context HttpServletRequest request, @Context HttpServletResponse currentResponse) {
        
        String code = request.getParameter("code");
        Response response = 
             client.target(accessTokenUrl).queryParam("client_id",CLIENT_ID).
             queryParam("client_secret", CLIENT_SECRET).
             queryParam("code", code).
             queryParam("redirect_uri",REDIRECT_URI).request("text/plain").accept("application/json").get();
        System.out.println(response.getEntity());
        wys.entities.Token t = response.readEntity(wys.entities.Token.class);
        
   
        GithubUserModel u = client.target(GITHUB_API_ENDPOINT + "/user").request("text/plain").
                accept(MediaType.APPLICATION_JSON).
                header("Authorization", String.format("Bearer %s", t.getAccess_token()))
                .get(GithubUserModel.class);
        
        
        Key key = KeyFactory.createKey("User", u.getId());
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
        List<Entity> results = DatastoreUtils.getResults(datastore, key, keyFilter);
        //User exists 
        if(results.size() != 0){
            Entity result = results.get(0);
            updateEntityWithGithubModelAndToken(result,u,t.getAccess_token());
            datastore.put(result);
        }
        //Not exist before, create
        else{
            Entity entity = new Entity("User", u.getId());
            updateEntityWithGithubModelAndToken(entity, u, t.getAccess_token());
            datastore.put(entity);
        }
        currentResponse.sendRedirect("");
    }
    /** 
     * 
     * Description: Use GihubUserModel and token to update User entity
     * @param e
     * @param g
     * void
     */
    private void updateEntityWithGithubModelAndToken(Entity e, GithubUserModel g, String builderToken){
        e.setProperty("login", g.getLogin());
        e.setProperty("builderToken", builderToken);
        e.setProperty("url", g.getUrl());
        e.setProperty("name", g.getName());
        e.setProperty("avatar_url", g.getAvatar_url());
    }
}
