package net.ivanhjc.utility.file;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Ivan Huang on 2018/3/21
 */
public class POIUtilsTest {

    @Test
    public void insertTable() throws Exception {
        POIUtils pu = new POIUtils();
        pu.open();
        pu.setBeanPackage("sample");
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("ID", "User.id");
        map.put("Username", "User.username");
        map.put("Birthday", "User.birthday");
        map.put("Roles", "User.roles");
        map.put("Roles:Id", "User$Role.id");
        map.put("Roles:Name", "User$Role.name");
        map.put("Roles:Privileges", "User$Role.privilege");
        map.put("Roles:Privileges:id", "User$Privilege.id");
        map.put("Roles:Privileges:name", "User$Privilege.name");
        map.put("Roles:Privilege", "User$Role.privilege");
        map.put("Roles:Privilege:id", "User$Privilege.id");
        map.put("Roles:Privilege:name", "User$Privilege.name");
        map.put("Role", "User.role");
        map.put("Role:Id", "User$Role.id");
        map.put("Role:Name", "User$Role.name");

        /*map.put("id", "id");
        map.put("username", "username");
        map.put("birthday", "birthday");
        map.put("roles", "roles");
        map.put(".Id", ".id");
        map.put("roleName", ".name");
        map.put("role", "role");
        map.put("roleId2", ".id");
        map.put("roleName2", ".name");*/

        /*List<User> data = new ArrayList<>();
        User user = Coder.sample(User.class);
        data.add(user);
        data.add(user);
        pu.insertTable(map, data);
        pu.save();*/
    }

    @Test
    public void appendValuesToSheet() throws IOException, InvalidFormatException {
        String dirPath = "C:\\Users\\Administrator\\Desktop\\temp";
        String fileType = ".xlsx";
        File dir1 = new File(dirPath);
        File[] files = dir1.listFiles((dir, name) -> name.toLowerCase().endsWith(fileType));
        Object[][] values = {{"A2", "B2", "C2"}, {"A3", "B3", "C3", "D3"}};
        POIUtils poiUtils = new POIUtils();
        assert files != null;
        for (File file : files) {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
//            poiUtils.open(workbook);
//            poiUtils.open("Sheet1");
            poiUtils.appendValuesToSheet(values);
            poiUtils.saveAs(dirPath, file.getName().substring(0, file.getName().length() - fileType.length()).concat("_out").concat(fileType));
            poiUtils.save();
            poiUtils.closeToDiscard();
        }
    }
}
