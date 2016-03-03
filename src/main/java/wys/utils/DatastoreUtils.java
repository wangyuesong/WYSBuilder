
package wys.utils;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.egit.github.core.User;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;

/**  
 * @Project: wysbuilder
 * @Title: DatastoreUtils.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 18, 2016 11:43:38 PM
 * @version V1.0  
 */
public class DatastoreUtils{
    public static List<Entity> getResults(DatastoreService datastore, Key key, Filter keyFilter) {
        Query q = new Query().setFilter(keyFilter);
        return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    }
    
    
    public static String getUserReposCacheKey(long userId){
        return userId + "_repos";
    }
//    public Entity injectPojoToDatastoreEntity(T pojo, Entity entity){
//        Field[] fields = pojo.getClass().get
//        for(Field f : fields){
//            pojo.getClass().getMe
//        }
//        User
//    }
}
