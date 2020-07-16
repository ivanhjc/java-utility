package net.ivanhjc.utility.file;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Ivan Huang on 2018/4/3 13:25
 */
public class LogUtilsTest {

    @Test
    public void countReqParams() throws IOException, URISyntaxException {
        LogUtils.countReqParams(FileUtils.getProjectRoot().resolve("doc/ivans-notes/docs/files/params1").toString(),
                FileUtils.getClassPath().resolve("output").toString(), "UserID", "OrderID", "CarLicense");
    }

    @Test
    public void countParams() throws IOException, URISyntaxException {
        LogUtils.countParams(FileUtils.getProjectRoot().resolve("net/ivanhjc/metanote/main/webapp/doc/kyles_notes/files/params2").toString(),
                FileUtils.getClassPath().resolve("output").toString(), "orderId");
    }

    @Test
    public void combineFiles() throws IOException, URISyntaxException {
        List<String> file1 = IOUtils.readLines(new FileReader(FileUtils.getProjectRoot().resolve("net/ivanhjc/metanote/main/webapp/doc/kyles_notes/files/orderIdCount.txt").toString()));
        List<String> file2 = IOUtils.readLines(new FileReader(FileUtils.getProjectRoot().resolve("net/ivanhjc/metanote/main/webapp/doc/kyles_notes/files/orderInfo.txt").toString()));
        StringBuilder builder = new StringBuilder();

        for (String line1 : file1) {
            for (String line2 : file2) {
                if (StringUtils.substringBefore(line1, " ").equals(StringUtils.substringBefore(line2, ","))) {
                    String[] strs = line2.split(",");
                    builder.append(line1).append(String.format("%-20s%-20s%-20s%n", strs[1], strs[2], strs[3]));
                    break;
                }
            }
        }

        FileUtils.writeStringToFile(FileUtils.getClassPath().resolve("output/bind").toFile(), builder.toString());
    }
}
