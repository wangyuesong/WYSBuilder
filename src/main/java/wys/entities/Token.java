
package wys.entities;
/**  
 * @Project: wysbuilder
 * @Title: Token.java
 * @Package wys.entities
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 17, 2016 10:49:35 PM
 * @version V1.0  
 */
public class Token {
    String access_token;
    String scope;
    String token_type;
    
    public String getAccess_token() {
        return access_token;
    }
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getToken_type() {
        return token_type;
    }
    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
    
}
