
package wys.message;
/**  
 * @Project: wysbuilder
 * @Title: Response.java
 * @Package wys.message
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 12, 2016 5:52:03 PM
 * @version V1.0  
 */
public class JsonResponse {
    public enum ResponseType{
        SUCCESS,
        FAILED
    }
   
    private ResponseType type;
    private String message;
    
    public ResponseType getType() {
        return type;
    }
    public void setType(ResponseType type) {
        this.type = type;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public JsonResponse(ResponseType type, String message) {
        super();
        this.type = type;
        this.message = message;
    }
    
    
}
