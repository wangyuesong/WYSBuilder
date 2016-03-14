package wys.viewmodel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Project: wysbuilder
 * @Title: BuildModel.java
 * @Package wys.viewmodel
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 12, 2016 4:19:49 AM
 * @version V1.0
 */
@XmlRootElement
public class BuildModel {
    private String name;
    private String branch;
    private String jenkins_log_url;
    private String gcs_log_path;
    private String commit_message;
    private String commiter;
    private String commit_time;
    private String commit_url;
    private String status;
    private String duration;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getJenkins_log_url() {
        return jenkins_log_url;
    }

    public void setJenkins_log_url(String jenkins_log_url) {
        this.jenkins_log_url = jenkins_log_url;
    }

    public String getGcs_log_path() {
        return gcs_log_path;
    }

    public void setGcs_log_path(String gcs_log_path) {
        this.gcs_log_path = gcs_log_path;
    }

    public String getCommit_message() {
        return commit_message;
    }

    public void setCommit_message(String commit_message) {
        this.commit_message = commit_message;
    }

    public String getCommiter() {
        return commiter;
    }

    public void setCommiter(String commiter) {
        this.commiter = commiter;
    }

    public String getCommit_time() {
        return commit_time;
    }

    public void setCommit_time(String commit_time) {
        this.commit_time = commit_time;
    }

    public String getCommit_url() {
        return commit_url;
    }

    public void setCommit_url(String commit_url) {
        this.commit_url = commit_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
    

}
