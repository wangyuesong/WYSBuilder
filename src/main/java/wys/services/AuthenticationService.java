package wys.services;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * @Project: wysbuilder
 * @Title: AuthenticationService.java
 * @Package wys.services
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 17, 2016 12:17:41 AM
 * @version V1.0
 */
public class AuthenticationService {

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public boolean doAuth(String authCredentials) {
        if (null == authCredentials)
        {
            return false;
        }
        final String encodedUserPassword = authCredentials.replaceFirst("Basic" + " ", "");
        String usernameAndPassword = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(
                    encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final StringTokenizer tokenizer = new StringTokenizer(
                usernameAndPassword, ":");
        final String email = tokenizer.nextToken();
        final String password = tokenizer.nextToken();

        Key key = KeyFactory.createKey("User", email);
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key);
        List<Entity> results = getResults(datastore, key, keyFilter);
        if (results.size() == 0)
            return false;
        if (results.get(0).getProperty("password") != password)
            return false;
        return true;
    }

    private List<Entity> getResults(DatastoreService datastore, Key key, Filter keyFilter) {
        Query q = new Query().setFilter(keyFilter);
        return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    }
}
