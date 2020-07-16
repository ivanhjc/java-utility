package net.ivanhjc.utility.data;

import org.junit.Test;

import java.sql.*;

/**
 * @author Administrator on 2018/7/25 10:07.
 */
public class DBTest {

    @Test
    public void select() {
        try {
//            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/playground?useUnicode=true&amp;charcterEncoding=utf-8", "root", "sql3306");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:D:\\Documents\\Downloads\\test\\info");
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM file WHERE password LIKE '%^%'");
            ResultSet rs = ps.executeQuery();
            System.out.println(StringUtils.formatTable(rs));
            rs.close();
            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
