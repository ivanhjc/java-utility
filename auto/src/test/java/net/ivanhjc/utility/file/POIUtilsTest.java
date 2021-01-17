package net.ivanhjc.utility.file;

import net.ivanhjc.utility.auto.Coder;
import net.ivanhjc.utility.model.User;
import org.junit.Test;

import java.util.Arrays;

public class POIUtilsTest {

    @Test
    public void insertTableInsertListOfUsers() throws Exception {
        new POIUtils().open()
                .createTable("ID:id,用户名:username,生日:birthday,角色:role,角色.ID:role.id,角色.名称:role.name", Arrays.asList(Coder.sample(User[].class)))
                .saveAs();
    }
}
