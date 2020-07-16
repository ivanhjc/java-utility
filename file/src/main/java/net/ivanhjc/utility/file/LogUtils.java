package net.ivanhjc.utility.file;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ivanhjc.utility.data.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Huang on 2018/4/3 10:37
 */
public class LogUtils {

    /**
     * Given a file containing JSON strings of the request parameters of a request mapping, counts the times of occurrence of certain parameters
     * and outputs the results to the designated directory
     *
     * @param file   the file containing the requests parameters
     * @param outDir the output directory
     * @param params the params to count
     * @throws IOException
     */
    public static void countReqParams(String file, String outDir, String... params) throws IOException {
        Scanner sc = new Scanner(new File(file));
        JsonParser parser = new JsonParser();
        Map<String, Map<String, Integer>> maps = new HashMap<>();
        for (String param : params) {
            maps.put(param, new HashMap<>());
        }
        int skippedLn = 0;
        Map<Integer, String> skippedLines = new HashMap<>();
        while (sc.hasNext()) {
            try {
                skippedLn++;
                String line = sc.nextLine();
                JsonObject reqObj = parser.parse(line).getAsJsonObject();
                for (String param : params) {
                    String paramValue = reqObj.get(param) == null ? null : reqObj.get(param).getAsString();
                    maps.get(param).merge(paramValue, 1, (a, b) -> a + b);
                }
            } catch (Exception e) {
                skippedLines.put(skippedLn, e.getMessage());
            }
        }

        outDir = FileUtils.getDirPath(outDir);
        for (String param : params) {
            FileUtils.writeMapToFile(MapUtils.sortByValue(maps.get(param)), outDir, param + "Count", param, "times of occurrence");
        }
        FileUtils.writeMapToFile(MapUtils.sortByKey(skippedLines), outDir, "skippedLines", "line", "message");
    }

    public static void countParams(String file, String outDir, String... params) throws IOException {
        Scanner sc = new Scanner(new File(file));
        Map<String, Map<String, Integer>> maps = new HashMap<>();
        for (String param : params) {
            maps.put(param, new HashMap<>());
        }
        int skippedLn = 0;
        Map<Integer, String> skippedLines = new HashMap<>();
        while (sc.hasNext()) {
            try {
                skippedLn++;
                String line = sc.nextLine();
                for (String param : params) {
                    String paramValue = StringUtils.substringBetween(line, param + ":", ",");
                    maps.get(param).merge(paramValue, 1, (a, b) -> a + b);
                }
            } catch (Exception e) {
                skippedLines.put(skippedLn, e.getMessage());
            }
        }

        outDir = FileUtils.getDirPath(outDir);
        for (String param : params) {
            FileUtils.writeMapToFile(MapUtils.sortByValue(maps.get(param)), outDir, param + "Count", param, "times of occurrence");
        }
        FileUtils.writeMapToFile(MapUtils.sortByKey(skippedLines), outDir, "skippedLines", "line", "message");
    }
}
