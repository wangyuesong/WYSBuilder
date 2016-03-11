
package wys.pojos.hookpayload;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "zen",
    "hook_id",
    "hook",
    "repository",
    "sender"
})
public class GithubWebhookPayload {

    @JsonProperty("zen")
    private String zen;
    @JsonProperty("hook_id")
    private Integer hookId;
    @JsonProperty("hook")
    private Hook hook;
    @JsonProperty("repository")
    private Repository repository;
    @JsonProperty("sender")
    private Sender sender;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The zen
     */
    @JsonProperty("zen")
    public String getZen() {
        return zen;
    }

    /**
     * 
     * @param zen
     *     The zen
     */
    @JsonProperty("zen")
    public void setZen(String zen) {
        this.zen = zen;
    }

    /**
     * 
     * @return
     *     The hookId
     */
    @JsonProperty("hook_id")
    public Integer getHookId() {
        return hookId;
    }

    /**
     * 
     * @param hookId
     *     The hook_id
     */
    @JsonProperty("hook_id")
    public void setHookId(Integer hookId) {
        this.hookId = hookId;
    }

    /**
     * 
     * @return
     *     The hook
     */
    @JsonProperty("hook")
    public Hook getHook() {
        return hook;
    }

    /**
     * 
     * @param hook
     *     The hook
     */
    @JsonProperty("hook")
    public void setHook(Hook hook) {
        this.hook = hook;
    }

    /**
     * 
     * @return
     *     The repository
     */
    @JsonProperty("repository")
    public Repository getRepository() {
        return repository;
    }

    /**
     * 
     * @param repository
     *     The repository
     */
    @JsonProperty("repository")
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * 
     * @return
     *     The sender
     */
    @JsonProperty("sender")
    public Sender getSender() {
        return sender;
    }

    /**
     * 
     * @param sender
     *     The sender
     */
    @JsonProperty("sender")
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
