package net.ivanhjc.utility.file;

import net.ivanhjc.utility.data.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Huang on 2018/3/9
 */
public class FileUtilsTest {

    @Test
    public void writeMapToFile() throws IOException {
        Map<String, Object> data1 = new LinkedHashMap<>();
        data1.put("key1", "val1");
        data1.put("key2", 2);
        data1.put("key3", '5');
        data1.put("key4", 3.3);
        data1.put("key5", new Date());
        FileUtils.writeMapToFile(data1, "key", "value", "C:\\Users\\Administrator\\Desktop\\tmp", "map.txt");
    }

    @Test
    public void insertLine() throws IOException {
        FileUtils.insertLine(new File("C:\\Users\\Administrator\\Desktop\\tmp\\map.txt"), 3, "Hello World!");
    }

    @Test
    public void windowsNotepad() throws IOException {
        File file = new File("C:\\Users\\Administrator\\Desktop\\新建文件夹\\test.txt");
        String s = FileUtils.readFileToString(file);
        System.out.println(StringUtils.toCodePoints(s));
        org.apache.commons.io.FileUtils.writeStringToFile(file, "\ntest\ntest2\n\rtest3\r\ntest4\rtest5", true);
    }

    @Test
    public void parseYML() throws IOException {
        String filePath = "java/Test.dm";
        String pagePath = "Programming/Java";
        String textToInsert = pagePath.concat("/").concat(filePath);
        insertHierarchy("F:\\projects\\onenote\\temp\\mkdocs.yml", textToInsert);

    }

    public void insertHierarchy(String fileToInsert, String textToInsert) throws IOException {
        int firstRow = 2;
        File file = new File(fileToInsert);
        List<String> lines = org.apache.commons.io.FileUtils.readLines(file);
        String firstLine = lines.get(firstRow);
        int firstCol = StringUtils.indexOfFirstNonBlankChar(firstLine);
        String[] paths = textToInsert.split("/");
        String prefix = "- ";
        String suffix = ":";
        int indent = 2;
        int j = 0;
        outer: for (int i = firstRow; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineIndent = firstCol + indent * j;
            String test = line.substring(lineIndent + prefix.length(), line.length() - suffix.length());
            if (test.equals(paths[j])) {
                j++;
                if (j == paths.length - 1) {
                    String lineToInsert = org.apache.commons.lang3.StringUtils.repeat(" ", firstCol + indent * j) + prefix + paths[j];
                    for (i = i + 1; i < lines.size(); i++) {
                        line = lines.get(i);
                        if (StringUtils.indexOfFirstNonBlankChar(line) == lineIndent) {
                            lines.add(i, lineToInsert);
                            break outer;
                        } else if (i == lines.size() - 1) {
                            lines.add(lineToInsert);
                        }
                    }
                }
            } else if (i == lines.size() - 1) {
                for (; j < paths.length; j++) {
                    String lineToInsert = org.apache.commons.lang3.StringUtils.repeat(" ", firstCol + indent * j) + prefix + paths[j];
                    lines.add(lineToInsert);
                }
            }
        }
        org.apache.commons.io.FileUtils.writeLines(file, lines);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            for (; i < 10; i++) {
                System.out.println(i);
            }
        }
    }

    /*@Test
    public void publishIvansNotes() throws IOException {
        String title = "Fedora", filePath = "linux", pagePath = "Platform/Linux";
        Path doc = PathUtils.getProjectRoot().resolve("doc/ivans-notes/docs");
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        File docFile = doc.resolve(filePath).resolve(title + ".md").toFile();
        String data = String.format("" +
                "# %1$s\n\n" +
                "<hr>\n\n" +
                "This page was created on %2$s.\n", title, DATE_FORMAT.format(new Date()));
        FileUtils.writeStringToFile(docFile, data);

        String content = String.format("%s | %s | [%s](%s.md) ", DATE_FORMAT.format(new Date()), pagePath, title, filePath + "/" + title);
        FileUtils.insertLine(doc.resolve("archive.md").toFile(), 4, content);

        File configFile = doc.getParent().resolve("mkdocs.yml").toFile();
        List<String> lines = FileUtils.readLines(configFile);
        String[] paths = pagePath.split("/");
        int j = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.substring(2 * (j + 1), line.length() - 1).equals(paths[j]))
                continue;
            j++;
            if (j >= paths.length) {
                while (lines.get(++i).indexOf("-") == lines.get(i + 1).indexOf("-")) {
                    if (i == lines.size() - 2)
                        break;
                }
                lines.add(i + 1, lines.get(i).substring(0, lines.get(i).indexOf('-') + 1).concat(" ").concat(filePath));
                break;
            }
        }
        FileUtils.writeLines(docFile, lines);
    }*/

    @Test
    public void toBase64() throws IOException {
        System.out.println(FileUtils.toBase64("D:\\Documents\\Downloads\\pics\\a-neutron-star-pours-its-heart-out2.jpg"));
    }

    @Test
    public void toImage() throws IOException {
        FileUtils.toImage(org.apache.commons.io.FileUtils.readFileToString(new File("F:\\projects\\onenote\\temp\\temp.txt")), "D:\\Documents\\Downloads\\pics\\ljadf.jpg");
    }

    @Test
    public void getHomeDir() {
        System.out.println(FileUtils.getHomeDir());
    }

    @Test
    public void getTempDir() {
        System.out.println(FileUtils.getTempDir());
    }

    @Test
    public void getClassPath() throws URISyntaxException {
        System.out.println(FileUtils.getClassPath());
    }

    @Test
    public void getProjectRoot() {
        System.out.println(FileUtils.getProjectRoot());
    }

    @Test
    public void getTargetDir() {
        System.out.println(FileUtils.getTargetDir());
    }
}
