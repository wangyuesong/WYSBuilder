package wys.resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

/**
 * @Project: wysbuilder
 * @Title: Test.java
 * @Package wys.resource
 * @Description: TODO
 * @author YuesongWang
 * @date Mar 14, 2016 7:54:45 PM
 * @version V1.0
 */
public class Test {

    /**
     * Description: TODO
     * 
     * @param args
     *            void
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Calendar test = DatatypeConverter.parseDateTime("2016-03-14T08:42:22+00:00");
        System.out.println(test.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        System.out.println(sdf.format(test.getTime()));
    }

}
