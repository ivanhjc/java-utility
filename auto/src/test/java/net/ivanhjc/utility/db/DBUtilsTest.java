package net.ivanhjc.utility.db;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.auto.ColumnInfo;
import net.ivanhjc.utility.db.model.params.ImportFromFileToDatabasesConfiguration;
import net.ivanhjc.utility.db.model.params.TableInfo;
import net.ivanhjc.utility.file.POIUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.junit.Test;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DBUtilsTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private DBUtils dbUtils;
    private TableInfo tableInfo;

    public DBUtilsTest() throws SQLException, ClassNotFoundException, IOException {
        dbUtils = new DBUtils();
        dbUtils.connect("db.json");
        tableInfo = new TableInfo("my_schema", "my_schema", "t_car");
    }

    @Test
    public void query() throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        List<CarInfo> list = dbUtils.query(
                DBUtils.getConnection("my_schema"),
                "select * from t_car",
                CarInfo.class,
                ImmutableMap.of("ID", "id", "license", "license", "createDate", "create_date"));
        LOG.info("\n{}", GSON.toJson(list));
    }

    @Test
    public void queryEntity() throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        List<CarInfo> list = dbUtils.query(DBUtils.getConnection("my_schema"), "select * from t_car", null, CarInfo.class);
        LOG.info("\n{}", GSON.toJson(list));
    }

    @Test
    public void queryAsFormattedTable() throws SQLException {
        String list = dbUtils.query(DBUtils.getConnection("my_schema"), "select * from t_car");
        LOG.info("\n{}", list);
    }

    @Test
    public void queryColumns() throws SQLException {
        LOG.info("\n{}", DBUtils.queryColumns(tableInfo));
    }

    @Test
    public void getResultMap() throws SQLException {
        LOG.info("\n{}", GSON.toJson(dbUtils.getResultMap(tableInfo, CarInfo.class)));
    }

    @Test
    public void getResultMapFromMetaData() throws SQLException {
        try (PreparedStatement ps = DBUtils.getConnection("my_schema").prepareStatement("SELECT * FROM t_car")) {
            ResultSet rs = ps.executeQuery();
            LOG.info("\n{}", GSON.toJson(DBUtils.getResultMap(rs.getMetaData(), CarInfo.class, null)));
        }
    }

    @Test
    public void toVarName() {
        String[] cases = {"w", "W", "we", "We", "Word", "word", "wOrd", "WORD", "word_family", "wordFamily", "WordFamily", "t_word_family", "1011001000", "11010"};
        StringBuilder sb = new StringBuilder();
        for (String str : cases) {
            sb.append(String.format("%-15s%-5s%s%n", str, "->", DBUtils.toVarName(str, "t_")));
        }
        LOG.info("\n{}", sb);
    }

    @Test
    public void generateTestImportFile() throws Exception {
        LOG.info("Generating test file...");
        List<String> excludeColumns = Arrays.asList("driver_id");
        List<ColumnInfo> columns = dbUtils.getColumns(tableInfo)
                .stream()
                .filter(col -> {
                    if (col.getName().equals("license")) {
                        col.setMultiLang(true);
                    }
                    return !excludeColumns.contains(col.getName());
                })
                .collect(Collectors.toList());
        String headers = columns.stream()
                .map(col -> col.getName() + ":" + col.getVarName())
                .collect(Collectors.joining(","));
        long time1 = System.currentTimeMillis();
        new POIUtils().open()
                .createTable(headers, dbUtils.generateList(CarInfo.class, 10, columns))
                .saveAs();
        LOG.info("Time elapsed: {}ms", System.currentTimeMillis() - time1);
        LOG.info("Done!");
    }

    @Test
    public void importFromFilesToDatabase() throws SQLException, IOException {
        ZipSecureFile.setMinInflateRatio(0);
        new DataMigrateEngine.Builder()
                .setImportFromFileToDatabasesConfigurations(new ImportFromFileToDatabasesConfiguration[]{
                        new ImportFromFileToDatabasesConfiguration()
                                .setSource("D:\\projects\\java-utility\\auto\\target\\output_20210215232310.xlsx.Sheet1")
                                .setTargets("my_schema.my_schema.t_car,my_schema.my_schema_second.t_car")
//                                .setFields(Arrays.asList("id", "license", "create_date", "state"))
//                                .setStartRow(1)
                })
                .build()
                .importFromFilesToDatabases();
    }

    @Test
    public void generateList() throws SQLException, InstantiationException, IllegalAccessException {
        LOG.info("\n{}", GSON.toJson(dbUtils.generateList(CarInfo.class, 5, dbUtils.getColumns(tableInfo))));
    }

    /**
     * Generate specified number of rows of data in a table
     */
    @Test
    public void generateData() throws InterruptedException, SQLException {
        LOG.info("Start generating data...");
        long startTime = System.currentTimeMillis();
        int rows = 10000000;
        int threadNum = 4;
        int threadRows = rows / threadNum;
        Thread[] threads = new Thread[threadNum];
        List<ColumnInfo> columns = dbUtils.getColumns(tableInfo);
        columns.forEach(col -> {
            if ("state".equals(col.getName())) {
                col.setRandomGenerator(() -> RandomUtils.nextInt(0, 5));
            } else if ("license".equals(col.getName())) {
                col.setMultiLang(true);
            }
        });
        AtomicInteger count = new AtomicInteger(0);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    long time = System.currentTimeMillis();
                    LOG.info("Thread start");
                    DBUtils dbUtils = new DBUtils();
                    dbUtils.connect("db.json");
                    dbUtils.generateData(tableInfo, columns, threadRows);
                    LOG.info("Thread end. Time elapsed: {}ms", System.currentTimeMillis() - time);
                } catch (Exception e) {
                    LOG.error(e, e);
                }
                count.incrementAndGet();
            });
        }
        Arrays.stream(threads).forEach(Thread::start);
        while (count.get() < threadNum) {
            Thread.sleep(500);
        }
        LOG.info("Finish! Time elapsed: {}ms", System.currentTimeMillis() - startTime);
    }

    @Test
    public void randomSQLValue() throws SQLException {
        List<ColumnInfo> columns = dbUtils.getColumns(tableInfo);
        columns.forEach(col -> {
            if ("state".equals(col.getName())) {
                col.setRandomGenerator(() -> RandomUtils.nextInt(0, 5));
            }
        });
        for (int i = 0; i < 10; i++) {
            columns.forEach(col -> System.out.print(col.randomSQLValue() + ", "));
            System.out.println();
        }
    }

    public static void main(String[] args) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(f.parse("2015-02-31 19:03:47").getTime());
        System.out.println(f.parse("2005-02-29 01:27:52").getTime());
        System.out.println(f.format(new Date(1425380627000L)));
        System.out.println(f.format(new Date(1109611672000L)));
    }
}
