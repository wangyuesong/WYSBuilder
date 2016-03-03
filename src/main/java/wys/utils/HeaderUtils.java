
package wys.utils;

import java.util.List;

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
 * @Title: HeaderUtils.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 1, 2016 10:56:45 PM
 * @version V1.0  
 */
public class HeaderUtils {
    /**
     * Check if oauthToken match user id
     * Description: TODO
     * @param headerToken
     * @param id
     * @return
     * boolean
     */
    static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    public static boolean checkHeader(String headerToken, long id){
        if(headerToken == null || headerToken == "")
            return false;
        Key userKey = KeyFactory.createKey("User", id);
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, userKey);
        List<Entity> results = DatastoreUtils.getResults(datastore, userKey, keyFilter);
        if(results.size() == 0){
            return false;
        }
        if(!results.get(0).getProperty("builderToken").equals(headerToken)){
            return false;
        }
        return true;
    }
}
