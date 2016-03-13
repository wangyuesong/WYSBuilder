package wys.utils;

import wys.viewmodel.BuildModel;
import wys.viewmodel.RepositoryModel;

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
     * Description: Convert GithubEntity to Model. Used when show
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
        r.setHtml_url((String) e.getProperty("html_url"));
        r.setHomepage((String) e.getProperty("homepage"));
        r.setMaster_branch((String) e.getProperty("master_branch"));
        r.setCreated_at(e.getProperty("created_at") == null ? null : e.getProperty("created_at").toString());
        r.setUpdated_at(e.getProperty("updated_at") == null ? null : e.getProperty("updated_at").toString());
        r.setIs_hooked(e.getProperty("hook") == null ? false : true);
    }

    public static void convertEntityToBuildModel(Entity e, BuildModel b) {
        b.setName((String) e.getProperty("name"));
        b.setBranch((String) e.getProperty("branch"));
        b.setJenkins_log_url((String) e.getProperty("jenkins_log_url"));
        b.setCommit_message((String) e.getProperty("commit_message"));
        b.setCommiter((String) e.getProperty("commiter"));
        b.setCommit_time((String) e.getProperty("commit_time"));
        b.setCommit_url((String) e.getProperty("commit_url"));
        b.setStatus((String) e.getProperty("status"));
        b.setDuration((String) e.getProperty("duration"));
    }
}
