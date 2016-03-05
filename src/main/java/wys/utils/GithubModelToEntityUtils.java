
package wys.utils;

import org.eclipse.egit.github.core.Repository;

import wys.utils.WebhookUtils.AddhookResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
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
    
    public static void convertAddhookResponseModelToEntity(AddhookResponse r, EmbeddedEntity e){
        e.setProperty("id", r.getId());
        e.setProperty("ping_url", r.getPing_url());
        e.setProperty("updated_at", r.getUpdated_at());
        e.setProperty("test_url", r.getTest_url());
        e.setProperty("name", r.getName());
        e.setProperty("created_at", r.getCreated_at());
        e.setProperty("config_content_type", r.getConfig().getContent_type());
        e.setProperty("config_url", r.getConfig().getUrl());
        e.setProperty("active", r.getActive());
        e.setProperty("url", r.getUrl());
    }

}
