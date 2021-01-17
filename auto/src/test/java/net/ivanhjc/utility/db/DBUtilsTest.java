package net.ivanhjc.utility.db;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.chore.test.Hwidsmapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DBUtilsTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private DBUtils dbUtils = new DBUtils();

    public DBUtilsTest() throws SQLException, ClassNotFoundException, IOException {
        dbUtils.connect("db.json");
    }

    @Test
    public void query() throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        Map<String, String> resultMap = ImmutableMap.of("ID", "FID", "batchNumber", "fbatchnumber");
        List<Hwidsmapping> list = dbUtils.query(DBUtils.getConnection("dg02"), "SELECT * FROM t_dm_hwidsmapping WHERE fbatchnumber IN ('202101061906', '202101061907')", Hwidsmapping.class, resultMap);
        LOG.info("\n{}", GSON.toJson(list));
    }

    @Test
    public void queryEntity() throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        List<Hwidsmapping> list = dbUtils.query(DBUtils.getConnection("dg02"), "SELECT * FROM t_dm_hwidsmapping WHERE fbatchnumber IN ('202101061906', '202101061907')", "f", Hwidsmapping.class);
        LOG.info("\n{}", GSON.toJson(list));
    }

    @Test
    public void printColumns() throws SQLException {
        LOG.info("\n{}", dbUtils.printColumns(dbUtils.getConnection("dg02"), "stdhcmusera_dm", "t_dm_hwidsmapping"));
    }

    @Test
    public void getResultMap() throws SQLException {
        LOG.info(dbUtils.getResultMap(dbUtils.getConnection("dg02"), "stdhcmusera_dm", "t_dm_hwidsmapping", "f", Hwidsmapping.class));
    }

    @Test
    public void getResultMapFromMetaData() throws SQLException {
        try (PreparedStatement ps = DBUtils.getConnection("dg02").prepareStatement("SELECT * FROM t_dm_hwidsmapping WHERE fbatchnumber IN ('202101061906', '202101061907')")) {
            ResultSet rs = ps.executeQuery();
            LOG.info(GSON.toJson(DBUtils.getResultMap(rs.getMetaData(), Hwidsmapping.class, "f")));
        }
    }

    @Test
    public void toVarName() {
        String format = "%-20s%-20s%s%n";
        String pointer = "->";
        String[] cases = {"w", "W", "we", "We", "Word", "word", "wOrd", "WORD", "word_family", "wordFamily", "WordFamily", "t_word_family", "1011001000", "11010"};
        StringBuilder sb = new StringBuilder();
        for (String str : cases) {
            sb.append(String.format(format, str, pointer, DBUtils.toVarName(str, "t_")));
        }
        LOG.info("\n{}", sb);
    }
}
