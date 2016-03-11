
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
    "id",
    "distinct",
    "message",
    "timestamp",
    "url",
    "author",
    "committer",
    "added",
    "removed",
    "modified"
})
public class HeadCommit {

    @JsonProperty("id")
    private String id;
    @JsonProperty("distinct")
    private Boolean distinct;
    @JsonProperty("message")
    private String message;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("url")
    private String url;
    @JsonProperty("author")
    private Author_ author;
    @JsonProperty("committer")
    private Committer_ committer;
    @JsonProperty("added")
    private List<String> added = new ArrayList<String>();
    @JsonProperty("removed")
    private List<Object> removed = new ArrayList<Object>();
    @JsonProperty("modified")
    private List<Object> modified = new ArrayList<Object>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The distinct
     */
    @JsonProperty("distinct")
    public Boolean getDistinct() {
        return distinct;
    }

    /**
     * 
     * @param distinct
     *     The distinct
     */
    @JsonProperty("distinct")
    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * 
     * @return
     *     The message
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     *     The message
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 
     * @return
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * @param timestamp
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

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
     *     The author
     */
    @JsonProperty("author")
    public Author_ getAuthor() {
        return author;
    }

    /**
     * 
     * @param author
     *     The author
     */
    @JsonProperty("author")
    public void setAuthor(Author_ author) {
        this.author = author;
    }

    /**
     * 
     * @return
     *     The committer
     */
    @JsonProperty("committer")
    public Committer_ getCommitter() {
        return committer;
    }

    /**
     * 
     * @param committer
     *     The committer
     */
    @JsonProperty("committer")
    public void setCommitter(Committer_ committer) {
        this.committer = committer;
    }

    /**
     * 
     * @return
     *     The added
     */
    @JsonProperty("added")
    public List<String> getAdded() {
        return added;
    }

    /**
     * 
     * @param added
     *     The added
     */
    @JsonProperty("added")
    public void setAdded(List<String> added) {
        this.added = added;
    }

    /**
     * 
     * @return
     *     The removed
     */
    @JsonProperty("removed")
    public List<Object> getRemoved() {
        return removed;
    }

    /**
     * 
     * @param removed
     *     The removed
     */
    @JsonProperty("removed")
    public void setRemoved(List<Object> removed) {
        this.removed = removed;
    }

    /**
     * 
     * @return
     *     The modified
     */
    @JsonProperty("modified")
    public List<Object> getModified() {
        return modified;
    }

    /**
     * 
     * @param modified
     *     The modified
     */
    @JsonProperty("modified")
    public void setModified(List<Object> modified) {
        this.modified = modified;
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
