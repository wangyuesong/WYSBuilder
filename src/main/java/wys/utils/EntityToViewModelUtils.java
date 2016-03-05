
package wys.utils;

import wys.resource.UsersResource.RepositoryModel;

import com.google.appengine.api.datastore.Entity;

/**  
 * @Project: wysbuilder
 * @Title: EntityToViewModelUtils.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 4, 2016 11:16:58 PM
 * @version V1.0  
 */
public class EntityToViewModelUtils {
    /**
     * 
     * Description: Convert Entity to Model. Used when show
     * 
     * @param e
     * @param r
     *            void
     */
    public static void convertEntityToRepoModel(Entity e, RepositoryModel r) {
        r.setRepo_id((Long) e.getProperty("repo_id"));
        r.setName((String) e.getProperty("name"));
        r.setDescription((String) e.getProperty("description"));
        r.setGit_url((String) e.getProperty("git_url"));
        r.setHomepage((String) e.getProperty("homepage"));
        r.setMaster_branch((String) e.getProperty("master_branch"));
        r.setCreated_at(e.getProperty("created_at") == null ? null : e.getProperty("created_at").toString());
        r.setUpdated_at(e.getProperty("updated_at") == null ? null : e.getProperty("updated_at").toString());
    }

}
