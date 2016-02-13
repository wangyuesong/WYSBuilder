
package wys.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

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
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UserServiceInfo getUserServiceInfo(){
        UserService userService = UserServiceFactory.getUserService();
        User u = userService.getCurrentUser();
        if(u == null){
            return new UserServiceInfo(u, userService.createLoginURL("/"), "");
        }
        else{
            return new UserServiceInfo(u, "", userService.createLogoutURL("/"));
        }
    }
}
