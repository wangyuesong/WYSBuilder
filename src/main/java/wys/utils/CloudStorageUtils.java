package wys.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
