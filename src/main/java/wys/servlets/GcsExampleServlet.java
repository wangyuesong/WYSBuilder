package wys.servlets;

import java.io.IOException;
import java.nio.channels.Channels;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wys.utils.CloudStorageUtils;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

/**
 * @Project: wysbuilder
 * @Title: GcsExampleServlet.java
 * @Package wys.servlets
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 11, 2016 11:57:17 PM
 * @version V1.0
 */
public class GcsExampleServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)

            .build());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GcsFilename fileName = getFileName(req);
        GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, CloudStorageUtils.BUFFER_SIZE);
        CloudStorageUtils.copy(Channels.newInputStream(readChannel), resp.getOutputStream());

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GcsOutputChannel outputChannel =
                gcsService.createOrReplace(getFileName(req), GcsFileOptions.getDefaultInstance());
        CloudStorageUtils.copy(req.getInputStream(), Channels.newOutputStream(outputChannel));
    }

    private GcsFilename getFileName(HttpServletRequest req) {
        String[] splits = req.getRequestURI().split("/", 4);
        if (!splits[0].equals("") || !splits[1].equals("gcs")) {
            throw new IllegalArgumentException("The URL is not formed as expected. " +
                    "Expecting /gcs/<bucket>/<object>");
        }
        return new GcsFilename(splits[2], splits[3]);
    }

}
