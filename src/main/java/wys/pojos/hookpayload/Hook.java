
package wys.pojos.hookpayload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "url",
    "test_url",
    "ping_url",
    "id",
    "name",
    "active",
    "events",
    "config",
    "last_response",
    "updated_at",
    "created_at"
})
public class Hook {

    @JsonProperty("url")
    private String url;
    @JsonProperty("test_url")
    private String testUrl;
    @JsonProperty("ping_url")
    private String pingUrl;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("active")
    private Boolean active;
    @JsonProperty("events")
    private List<String> events = new ArrayList<String>();
    @JsonProperty("config")
    private Config config;
    @JsonProperty("last_response")
    private LastResponse lastResponse;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The url
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @param url
     *     The url
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * @return
     *     The testUrl
     */
    @JsonProperty("test_url")
    public String getTestUrl() {
        return testUrl;
    }

    /**
     * 
     * @param testUrl
     *     The test_url
     */
    @JsonProperty("test_url")
    public void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }

    /**
     * 
     * @return
     *     The pingUrl
     */
    @JsonProperty("ping_url")
    public String getPingUrl() {
        return pingUrl;
    }

    /**
     * 
     * @param pingUrl
     *     The ping_url
     */
    @JsonProperty("ping_url")
    public void setPingUrl(String pingUrl) {
        this.pingUrl = pingUrl;
    }

    /**
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The active
     */
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    /**
     * 
     * @param active
     *     The active
     */
    @JsonProperty("active")
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * 
     * @return
     *     The events
     */
    @JsonProperty("events")
    public List<String> getEvents() {
        return events;
    }

    /**
     * 
     * @param events
     *     The events
     */
    @JsonProperty("events")
    public void setEvents(List<String> events) {
        this.events = events;
    }

    /**
     * 
     * @return
     *     The config
     */
    @JsonProperty("config")
    public Config getConfig() {
        return config;
    }

    /**
     * 
     * @param config
     *     The config
     */
    @JsonProperty("config")
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * 
     * @return
     *     The lastResponse
     */
    @JsonProperty("last_response")
    public LastResponse getLastResponse() {
        return lastResponse;
    }

    /**
     * 
     * @param lastResponse
     *     The last_response
     */
    @JsonProperty("last_response")
    public void setLastResponse(LastResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    /**
     * 
     * @return
     *     The updatedAt
     */
    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 
     * @param updatedAt
     *     The updated_at
     */
    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 
     * @return
     *     The createdAt
     */
    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * @param createdAt
     *     The created_at
     */
    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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
