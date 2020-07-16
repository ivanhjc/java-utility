package net.ivanhjc.utility.file;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import net.ivanhjc.utility.data.MapUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Huang on 2017/11/27
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

    /**
     * Writes a map of key-value pairs to a file with keys being a column and values being another.
     *
     * @param map      the map to write. To reserve the order of key-value pairs as added, use {@link java.util.LinkedHashMap}
     * @param keyName  name of the header of key column
     * @param valName  name of the header of value column
     * @param outDir   the output directory
     * @param fileName the output file name
     * @param <K>      key type
     * @param <V>      value type
     * @throws IOException
     */
    public static <K, V> void writeMapToFile(Map<K, V> map, String keyName, String valName, String outDir, String fileName) throws IOException {
        writeStringToFile(new File(getDirPath(outDir) + fileName), MapUtils.getTable(map, keyName, valName));
    }

    /**
     * Inserts a given line of text into the designated line in a file
     *
     * @param file    the target file, where the text will be inserted
     * @param lineNum the line number to which the text will be inserted. The first line is number 0.
     * @param content the text to be inserted
     * @throws IOException
     */
    public static void insertLine(File file, int lineNum, String content) throws IOException {
        List<String> lines = org.apache.commons.io.FileUtils.readLines(file);
        lines.add(lineNum, content);
        org.apache.commons.io.FileUtils.writeLines(file, lines);
    }



    /**
     * Create an HTML file according to the title of an article
     *
     * @param title
     */
    public static void createHTML(String title) {
        try {
            Path pagePath = getProjectRoot().resolve("src\\main\\webapp\\page");
            File template = pagePath.resolve("template.html").toFile();
            Document document = Jsoup.parse(template, "UTF-8");
            document.getElementsByTag("title").get(0).appendText(title);
            document.getElementsByTag("article").get(0).appendText(title);
            File newFile = new File(pagePath.toString() + "\\" + title + ".html");
            org.apache.commons.io.FileUtils.writeStringToFile(newFile, document.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Downloads a file from a URL
     *
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public static void download(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

        // Always check HTTP response code first
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    public static void saveFile(String fileUrl, String saveDir, String name, String format) {
        try {
            URL url = new URL(fileUrl);
            File file = new File(String.format("%s%s.%s", saveDir, name, format));
            org.apache.commons.io.FileUtils.copyInputStreamToFile(url.openStream(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insert(String filename, long offset, byte[] content) throws IOException {
        RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
        RandomAccessFile rtemp = new RandomAccessFile(new File(filename + "~"), "rw");
        long fileSize = r.length();
        FileChannel sourceChannel = r.getChannel();
        FileChannel targetChannel = rtemp.getChannel();
        sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
        sourceChannel.truncate(offset);
        r.seek(offset);
        r.write(content);
        long newOffset = r.getFilePointer();
        targetChannel.position(0L);
        sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
        sourceChannel.close();
        targetChannel.close();
    }

    /**
     * Parse YML file
     */
    public static <T> T parseYML(String filepath, Class<T> type) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        T object = yaml.loadAs(new FileInputStream(filepath), type);
        return object;

        /*ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            User user = objectMapper.readValue(new File(filepath), User.class);
            System.out.println(ReflectionToStringBuilder.toString(user, ToStringStyle.MULTI_LINE_STYLE));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
     *
     * @param imgPath path of the image file
     * @return
     */
    public static String toBase64(String imgPath) throws IOException {
        InputStream in = new FileInputStream(imgPath);
        byte[] data = new byte[in.available()];
        in.read(data);
        in.close();
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Converts a Base64 image string to an image file
     *
     * @param base64Str the string to convert
     * @param savePath the file name to be saved
     * @return
     */
    public static void toImage(String base64Str, String savePath) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(base64Str);
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] < 0) {// 调整异常数据
                bytes[i] += 256;
            }
        }
        OutputStream out = new FileOutputStream(savePath);
        out.write(bytes);
        out.flush();
        out.close();
    }

    /**
     * Get the current user's home directory
     */
    public static Path getHomeDir() {
        return Paths.get(System.getProperty("user.home"));
    }

    /**
     * Get the current system's temporary directory
     */
    public static Path getTempDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Get the current build class directory
     */
    public static Path getClassPath() throws URISyntaxException {
        return Paths.get(FileUtils.class.getResource("/").toURI());
    }

    /**
     * Get the current project's root directory
     */
    public static Path getProjectRoot() {
        return Paths.get(System.getProperty("user.dir"));
    }

    /**
     * Get the build target directory
     */
    public static Path getTargetDir() {
        return Paths.get(FileUtils.class.getResource("/").getPath().substring(1)).getParent();
    }

    public static String getDirPath(String path) {
        return path.charAt(path.length() - 1) == '\\' ? path : path.concat("\\");
    }
}
