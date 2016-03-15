package wys.viewmodel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Project: wysbuilder
 * @Title: BuildDetailModel.java
 * @Package wys.viewmodel
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 14, 2016 6:19:33 PM
 * @version V1.0
 */
@XmlRootElement
public class BuildDetailModel {
    private String status;
    private String log;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

}
