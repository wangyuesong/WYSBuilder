package wys.resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * @Project: BuilderBackend
 * @Title: UserResource.java
 * @Package wys.user
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 11, 2016 12:48:40 AM
 * @version V1.0
 */
@Path("/users")
public class UsersResource {

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    
    @Path("{userId}/repos")
    public RepoResource getRepoResource(@Context HttpServletRequest r, @PathParam("userId") int userId){
        System.out.println(r.getMethod()+ " " +r.getPathInfo());
        return new RepoResource(userId);
    }
    
    @GET
    public void Test(){
        System.out.println("Hello");
    }
    
   
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
//
//    /**
//     * 
//     * Description: POST createUser(regModel)
//     * 
//     * @param regModel
//     * @return
//     * @throws IOException
//     *             JsonResponse
//     */
//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public JsonResponse createUser(UserAuthModel regModel) throws IOException {
//        Key key = KeyFactory.createKey("User", regModel.getEmail());
//        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
//        List<Entity> results = DatastoreUtils.getResults(datastore, key, keyFilter);
//        if (results.size() == 0) {
//            Entity e = new Entity(KeyFactory.createKey("User", regModel.getEmail()));
//            e.setProperty("password", regModel.getPassword());
//            datastore.put(e);
//            return new JsonResponse(ResponseType.SUCCESS, "Registration Success");
//        } else {
//            return new JsonResponse(ResponseType.FAILED, "Username already taken");
//        }
//    }
//    
//    
//    
//    /**
//     * 
//     * Description: doLogin
//     * @param regModel
//     * @return
//     * @throws IOException
//     * JsonResponse
//     */
//    @Path("/login")
//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response login(UserAuthModel regModel) throws IOException {
//        Key key = KeyFactory.createKey("User", regModel.getEmail());
//        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
//        List<Entity> results = DatastoreUtils.getResults(datastore, key, keyFilter);
//        if (results.size() == 0) {
//            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonResponse(ResponseType.FAILED, "No user found")).build();
//        }else if (!results.get(0).getProperty("password").equals(regModel.getPassword())) {
//            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonResponse(ResponseType.FAILED, "Incorrect Password")).build();
//        }else {
//            return Response.ok(new JsonResponse(ResponseType.SUCCESS, "LoginSuccess")).build();
//        }
//    }
//    
    

  

}
