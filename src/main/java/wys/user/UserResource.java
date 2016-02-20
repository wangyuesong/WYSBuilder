package wys.user;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import wys.message.JsonResponse;
import wys.message.JsonResponse.ResponseType;
import wys.models.UserAuthModel;
import wys.utils.DatastoreUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * @Project: BuilderBackend
 * @Title: UserResource.java
 * @Package wys.user
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 11, 2016 12:48:40 AM
 * @version V1.0
 */
@Path("/user")
public class UserResource {

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // @POST
    // @Produces(MediaType.APPLICATION_JSON)
    // @Consumes(MediaType.APPLICATION_JSON)
    // public UserServiceInfo getUserServiceInfo(){
    // UserService userService = UserServiceFactory.getUserService();
    // User u = userService.getCurrentUser();
    // if(u == null){
    // return new UserServiceInfo(u, userService.createLoginURL("/"), "");
    // }
    // else{
    // return new UserServiceInfo(u, "", userService.createLogoutURL("/"));
    // }
    // }
    //

    /**
     * 
     * Description: POST createUser(regModel)
     * 
     * @param regModel
     * @return
     * @throws IOException
     *             JsonResponse
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonResponse createUser(UserAuthModel regModel) throws IOException {
        Key key = KeyFactory.createKey("User", regModel.getEmail());
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
        List<Entity> results = DatastoreUtils.getResults(datastore, key, keyFilter);
        if (results.size() == 0) {
            Entity e = new Entity(KeyFactory.createKey("User", regModel.getEmail()));
            e.setProperty("password", regModel.getPassword());
            datastore.put(e);
            return new JsonResponse(ResponseType.SUCCESS, "Registration Success");
        } else {
            return new JsonResponse(ResponseType.FAILED, "Username already taken");
        }
    }
    
    
    
    /**
     * 
     * Description: doLogin
     * @param regModel
     * @return
     * @throws IOException
     * JsonResponse
     */
    @Path("/login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserAuthModel regModel) throws IOException {
        Key key = KeyFactory.createKey("User", regModel.getEmail());
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
        List<Entity> results = DatastoreUtils.getResults(datastore, key, keyFilter);
        if (results.size() == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonResponse(ResponseType.FAILED, "No user found")).build();
        }else if (!results.get(0).getProperty("password").equals(regModel.getPassword())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonResponse(ResponseType.FAILED, "Incorrect Password")).build();
        }else {
            return Response.ok(new JsonResponse(ResponseType.SUCCESS, "LoginSuccess")).build();
        }
    }
    
    

  

}
