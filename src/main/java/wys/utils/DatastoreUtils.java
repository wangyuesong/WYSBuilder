
package wys.utils;

import java.util.List;

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
public class DatastoreUtils {
    public static List<Entity> getResults(DatastoreService datastore, Key key, Filter keyFilter) {
        Query q = new Query().setFilter(keyFilter);
        return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    }
}
