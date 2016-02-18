package wys.models;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Project: wysbuilder
 * @Title: TestModel.java
 * @Package wys.models
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 17, 2016 1:51:38 PM
 * @version V1.0
 */
@XmlRootElement
public class TestModel {
    String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
