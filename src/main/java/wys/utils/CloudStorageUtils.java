package wys.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;

/**
 * @Project: wysbuilder
 * @Title: CloudStorageUtils.java
 * @Package wys.utils
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 11, 2016 11:53:37 PM
 * @version V1.0
 */
public class CloudStorageUtils {
    public static final int BUFFER_SIZE = 2 * 1024 * 1024;
    private static Storage storageService;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String BUCKET_NAME = "cs263project-yuesongwang.appspot.com";

    public static void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }

    private static Storage getService() throws IOException, GeneralSecurityException {
        if (null == storageService) {
            GoogleCredential credential = GoogleCredential.getApplicationDefault();
            // Depending on the environment that provides the default credentials (e.g. Compute Engine,
            // App Engine), the credentials may require us to specify the scopes we need explicitly.
            // Check for this case, and inject the Cloud Storage scope if required.
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(StorageScopes.all());
            }
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            storageService = new Storage.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName("cs263project-yuesongwang").build();
        }
        return storageService;
    }

    public static List<StorageObject> listBucket(String bucketName)
            throws IOException, GeneralSecurityException {
        Storage client = getService();
        Storage.Objects.List listRequest = client.objects().list(bucketName);

        List<StorageObject> results = new ArrayList<StorageObject>();
        Objects objects;

        // Iterate through each page of results, and add them to our results list.
        do {
            objects = listRequest.execute();
            // Add the items in this page of results to the list we'll return.
            results.addAll(objects.getItems());

            // Get the next page, in the next iteration of this loop.
            listRequest.setPageToken(objects.getNextPageToken());
        } while (null != objects.getNextPageToken());

        return results;
    }

    public static void uploadStream(
            String objectPath, String contentType, InputStream stream)
            throws IOException, GeneralSecurityException {
        InputStreamContent contentStream = new InputStreamContent(contentType, stream);
        StorageObject objectMetadata = new StorageObject()
                // Set the destination object name
                .setName(objectPath)
                // Set the access control list to publicly read-only
                .setAcl(Arrays.asList(
                        new ObjectAccessControl().setEntity("allUsers").setRole("READER")));
        // Do the insert
        Storage client = getService();
        Storage.Objects.Insert insertRequest = client.objects().insert(
                BUCKET_NAME, objectMetadata, contentStream);

        insertRequest.execute();
    }

    public static void uploadStream(
            GCSObjectPath objectPath, String contentType, InputStream stream)
            throws IOException, GeneralSecurityException {
        uploadStream(objectPath.toString(), contentType, stream);
    }

    /**
     * 
     * Description: Get a object on specified path in the bucket, need to make sure it's not null before use
     * 
     * @param objectName
     * @return
     * @throws IOException
     *             ByteArrayOutputStream
     */
    public static ByteArrayOutputStream getObject(String objectPath) throws IOException {
        Storage.Objects.Get getObject = storageService.objects().get(BUCKET_NAME, objectPath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // If you're not in AppEngine, download the whole thing in one request, if possible.
        getObject.getMediaHttpDownloader().setDirectDownloadEnabled(false);
        getObject.executeMediaAndDownloadTo(out);
        return out;
    }

    public static ByteArrayOutputStream getObject(GCSObjectPath objectPath) throws IOException {
        return getObject(objectPath.toString());
    }

    public static boolean isObjectExist(String objectPath) throws IOException, GeneralSecurityException {
        List<StorageObject> objects = CloudStorageUtils.listBucket(CloudStorageUtils.BUCKET_NAME);
        for (StorageObject object : objects) {
            if (objectPath.equals(object.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isObjectExist(GCSObjectPath objectPath) throws IOException, GeneralSecurityException {
        return isObjectExist(objectPath.toString());
    }

    public static Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
        Storage client = getService();

        Storage.Buckets.Get bucketRequest = client.buckets().get(bucketName);
        // Fetch the full set of the bucket's properties (e.g. include the ACLs in the response)
        bucketRequest.setProjection("full");
        return bucketRequest.execute();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        String test = "holyshit";
        // System.out.println(isObjectExist("holy/ss/0x5e_wechat-deleted-friends.html"));
        // CloudStorageUtils.uploadStream("holy/shit", "text/plain", new ByteArrayInputStream(test.getBytes()));
        // System.out.println(new String(getObject("holy/shit").toByteArray()));
    }

    public static class GCSObjectPath {
        private String userLogin;
        private String repoName;
        private String buildName;

        public GCSObjectPath(String userLogin, String repoName, String buildName) {
            super();
            this.userLogin = userLogin;
            this.repoName = repoName;
            this.buildName = buildName;
        }

        public String getUserLogin() {
            return userLogin;
        }

        public void setUserLogin(String userLogin) {
            this.userLogin = userLogin;
        }

        public String getRepoName() {
            return repoName;
        }

        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        public String getBuildName() {
            return buildName;
        }

        public void setBuildName(String buildName) {
            this.buildName = buildName;
        }

        @Override
        public String toString() {
            return userLogin + "/" + repoName + "/" + buildName;
        }

        /**
         * 
         */
        public GCSObjectPath() {
            // TODO Auto-generated constructor stub
        }

    }
}
