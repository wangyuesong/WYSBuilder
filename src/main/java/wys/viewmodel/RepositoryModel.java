package wys.viewmodel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @Project: wysbuilder
 * @Title: RepoResource.java
 * @Package wys.resource
 * @Description: Repository Model
 * @author YuesongWang
 * @date Mar 2, 2016 1:38:19 AM
 * @version V1.0
 */
@XmlRootElement
public class RepositoryModel {
    private long repo_id;
    private String name;
    private String description;
    private String git_url;
    private String html_url;
    private String homepage;
    private String master_branch;
    private String created_at;
    private String updated_at;
    private boolean is_hooked;
    

    
    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public boolean isIs_hooked() {
        return is_hooked;
    }

    public void setIs_hooked(boolean is_hooked) {
        this.is_hooked = is_hooked;
    }

    public long getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(long repo_id) {
        this.repo_id = repo_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGit_url() {
        return git_url;
    }

    public void setGit_url(String git_url) {
        this.git_url = git_url;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getMaster_branch() {
        return master_branch;
    }

    public void setMaster_branch(String master_branch) {
        this.master_branch = master_branch;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

}