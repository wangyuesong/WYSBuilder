package wys.user;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Project: wysbuilder
 * @Title: UserLoginModel.java
 * @Package wys.user
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 12, 2016 5:39:07 PM
 * @version V1.0
 */
@XmlRootElement
public class UserRegistrationModel {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
