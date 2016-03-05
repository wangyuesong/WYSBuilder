
package wys.utils;

import org.eclipse.egit.github.core.Repository;

import com.google.appengine.api.datastore.Entity;

/**  
 * @Project: wysbuilder
 * @Title: GithubModelToEntityUtils.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 4, 2016 11:19:51 PM
 * @version V1.0  
 */
public class GithubModelToEntityUtils {
    /**
     * Description: Convert GithubAPIModel to entity. Used when save.
     * 
     * @param r
     * @param e
     *            void
     */
    public static void convertRepoModelToEntity(Repository r, Entity e) {
        e.setProperty("repo_id", r.getId());
        e.setProperty("name", r.getName());
        e.setProperty("description", r.getDescription());
        e.setProperty("git_url", r.getGitUrl());
        e.setProperty("homepage", r.getHomepage());
        e.setProperty("master_branch", r.getMasterBranch());
        e.setProperty("created_at", r.getCreatedAt());
        e.setProperty("updated_at", r.getUpdatedAt());
    }

}
