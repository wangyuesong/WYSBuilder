package wys.resource;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import wys.message.JsonResponse;
import wys.message.JsonResponse.ResponseType;
import wys.models.TestModel;

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
@Path("/test")
public class TestResource {

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
    public Response createUser(TestModel test) throws IOException {
        Key key = KeyFactory.createKey("Test", test.getKey());
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
        List<Entity> results = getResults(datastore, key, keyFilter);
        if (results.size() == 0) {
            Entity e = new Entity(KeyFactory.createKey("Test", test.getKey()));
            datastore.put(e);
            return Response.ok(new JsonResponse(ResponseType.SUCCESS, "Create test Success")).build();
        } else {
            return Response.status(Status.BAD_REQUEST).entity(new JsonResponse(ResponseType.FAILED, "Test already taken")).build();
        }
    }
    
    
    
    
    

    private List<Entity> getResults(DatastoreService datastore, Key key, Filter keyFilter) {
        Query q = new Query().setFilter(keyFilter);
        return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    }

}
