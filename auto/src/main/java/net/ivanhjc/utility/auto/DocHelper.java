package net.ivanhjc.utility.auto;

import net.ivanhjc.utility.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * filename doesn't include suffix
 *
 * @author Ivan Huang on 2018/4/28 15:10.
 */
public class DocHelper {

    private static final Path ROOT = FileUtils.getProjectRoot().resolve("doc/ivans-notes/docs");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * @param dir   name of the directory containing the file to create under docs
     * @param title title of the article and name of the file to create
     * @throws IOException
     */
    public void newFile(String dir, String title) throws IOException {
        File file = ROOT.resolve(dir).resolve(title + ".md").toFile();
        String data = String.format("" +
                "# %1$s\n\n" +
                "<hr>\n\n" +
                "This page was created on %2$s.\n", title, DATE_FORMAT.format(new Date()));
        org.apache.commons.io.FileUtils.writeStringToFile(file, data);
    }

    /**
     * @param dir      name of the directory containing the file to publish under docs
     * @param title    title of the article to publish
     * @param pagePath the display path of the article to publish
     * @throws IOException
     */
    public void publish(String dir, String title, String pagePath) throws IOException {
        String content = String.format("%s | %s | [%s](%s.md) ", DATE_FORMAT.format(new Date()), pagePath, title, dir + "/" + title);
        FileUtils.insertLine(ROOT.resolve("archive.md").toFile(), 4, content);
    }

    public static void main(String[] args) throws IOException {
        String pageTitle = "Fedora";
        String pageDir = "linux";
        String pagePath = "linux/Fedora.ad";
        DocHelper helper = new DocHelper();
        helper.newFile(pageDir, pageTitle);
        helper.publish(pageDir, pageTitle, pagePath);
    }
}
