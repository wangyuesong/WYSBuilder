package wys.viewmodel;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Project: wysbuilder
 * @Title: BranchModel.java
 * @Package wys.viewmodel
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 13, 2016 8:39:12 PM
 * @version V1.0
 */
@XmlRootElement
public class BranchModel {
    private String branch_name;
    private List<BuildModel> builds;

    public String getBranch_name() {
        return branch_name;
    }

    public void setBranch_name(String branch_name) {
        this.branch_name = branch_name;
    }

    public List<BuildModel> getBuilds() {
        return builds;
    }

    public void setBuilds(List<BuildModel> builds) {
        this.builds = builds;
    }

}
