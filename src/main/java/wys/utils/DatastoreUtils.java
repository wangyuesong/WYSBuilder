
package wys.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

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
    /**
     * 
     * Description: Get result from Datastore according to key. Return null if entry does not exists.
     * @param datastore
     * @param key
     * @return
     * Entity
     */
    public static Entity getOneResultByKey(DatastoreService datastore, Key key) {
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
        Query q = new Query().setFilter(keyFilter);
        List<Entity> results =  datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
        if(results == null || results.size() == 0)
            return null;
        else
            return results.get(0);
    }
    
    
    public static String getUserReposCacheKey(long userId){
        return userId + "_repos";
    }
    
    public static String getUserOneRepoCacheKey(long userId, long repoId){
        return userId + "_" + repoId + "_repo";
    }
    
    public static Repository getRepositoryByUserIdAndRepoId(RepositoryService repositoryService, long repoId) throws IOException{
        List<Repository> repos = repositoryService.getRepositories();
        for(Repository r : repos){
            if(r.getId() == repoId)
               return r;
        }
       return null;
    }
//    public Entity injectPojoToDatastoreEntity(T pojo, Entity entity){
//        Field[] fields = pojo.getClass().get
//        for(Field f : fields){
//            pojo.getClass().getMe
//        }
//        User
//    }
}
