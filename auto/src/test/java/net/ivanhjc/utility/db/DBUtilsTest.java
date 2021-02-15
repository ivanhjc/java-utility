package net.ivanhjc.utility.db;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.auto.ColumnInfo;
import net.ivanhjc.utility.data.ListUtils;
import net.ivanhjc.utility.db.model.params.ImportFromFileToDatabasesConfiguration;
import net.ivanhjc.utility.file.POIUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.junit.Test;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DBUtilsTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private DBUtils dbUtils = new DBUtils();

    public DBUtilsTest() throws SQLException, ClassNotFoundException, IOException {
        dbUtils.connect("db.json");
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
        LOG.info("\n{}", DBUtils.queryColumns(DBUtils.getConnection("my_schema"), "my_schema", "t_car"));
    }

    @Test
    public void getResultMap() throws SQLException {
        LOG.info("\n{}", GSON.toJson(dbUtils.getResultMap(DBUtils.getConnection("my_schema"), "my_schema", "t_car", null, CarInfo.class)));
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
        List<ColumnInfo> columns = dbUtils.getColumns(DBUtils.getConnection("my_schema"), "my_schema", "t_car", null);
        new POIUtils()
                .open()
                .createTable(columns.stream()
                                .filter(col -> !ListUtils.contains("driverId", col.getVarName()))
                                .map(col -> col.getName() + ":" + col.getVarName())
                                .collect(Collectors.joining(",")),
                        dbUtils.sampleList(CarInfo.class, 10, columns))
                .saveAs();
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
                                .setFields(Arrays.asList("id", "license", "create_date", "state"))
                                .setStartRow(1)
                })
                .build()
                .importFromFilesToDatabases();
    }

    @Test
    public void sampleList() throws SQLException, InstantiationException, IllegalAccessException {
        LOG.info("\n{}", GSON.toJson(dbUtils.sampleList(CarInfo.class, 5, dbUtils.getColumns(DBUtils.getConnection("my_schema"), "my_schema", "t_car", null))));
    }
}
