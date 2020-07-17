package net.ivanhjc.utility.auto;

import com.google.common.collect.ImmutableMap;
import com.sun.codemodel.JCodeModel;
import net.ivanhjc.utility.auto.enums.ColumnsOption;
import net.ivanhjc.utility.auto.enums.CreateType;
import net.ivanhjc.utility.auto.enums.SnippetType;
import net.ivanhjc.utility.data.StringUtils;
import net.ivanhjc.utility.file.POIUtils;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Ivan Huang on 2018/2/27
 */
public class CoderTest {

    private Coder coder;

    public CoderTest() throws SQLException, ClassNotFoundException {
        coder = new Coder("coder-hail.properties");
        coder.CONFIG.dropPrefix(true)
                .overwrite(true)
                .mapUnderscoreToCamelCase(false)
                .handleTimeByDAO(true, null)
                .showComment(true)
                .includeDateRange(false);
//        coder.init("t_suborder_settings");
    }

    @Test
    public void loadProperties() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Constructor<Config> con = Config.class.getDeclaredConstructor();
        con.setAccessible(true);
        Config o = con.newInstance();
        Field[] fields = Config.class.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        List<String> list = new ArrayList<>();
        for (Field f : fields) {
            f.setAccessible(true);
            if (Modifier.isFinal(f.getModifiers())) {
                list.add(f.getName() + "," + f.get(o));
            }
        }
        System.out.println(StringUtils.formatTable(list, 2, ",", null, 2));
    }

    @Test
    public void snippetInsertOne() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetInsertOne(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetInsertNonNull() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetInsertNonNull(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetInsertList() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetInsertList(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetInsertListNonNull() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetInsertListNonNull(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetSelectOne() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetSelectOne(snippetType)));
    }

    @Test
    public void snippetSelectOneByKey() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetSelectOneByKey(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetSelectOneCustom() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetSelectOneCustom(ColumnsOption.ALL, snippetType, 0)));
    }

    @Test
    public void snippetSelectList() {
//        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetSelectList(ColumnsOption.ALL, snippetType, null)));
        ColumnConfigs configs = new ColumnConfigs();
//        configs.setColPrefix("to1.");
        configs.setTestFieldListStrOption(ColumnsOption.INCLUDE.setFilter("order_id"));
        System.out.println(coder.snippetSelectList(ColumnsOption.ALL, SnippetType.MAPPER, configs));
    }

    @Test
    public void snippetSelectListCustom() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetSelectListCustom(ColumnsOption.ALL, ColumnsOption.ALL, snippetType, null)));
    }

    @Test
    public void snippetUpdateOne() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetUpdateOne(snippetType)));
    }

    @Test
    public void snippetUpdateOneByKey() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetUpdateOneByKey(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetUpdateList() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetUpdateList(snippetType)));
    }

    @Test
    public void snippetInsertOrUpdateOne() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetInsertOrUpdateOne(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetInsertOrUpdateList() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetInsertOrUpdateList(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetDeleteOneById() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetDeleteOneByPrimaryKey(snippetType)));
    }

    @Test
    public void snippetDeleteOneByKey() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetDeleteOneByKey(ColumnsOption.ALL, snippetType)));
    }

    @Test
    public void snippetDeleteList() {
        Arrays.asList(SnippetType.values()).forEach(snippetType -> System.out.println(snippetType.name() + ":\n\n" + coder.snippetDeleteList(snippetType)));
    }

    @Test
    public void snippetTypeAliases() {
        System.out.println(coder.snippetTypeAliases("t_page_file"));
    }

    @Test
    public void createBean() throws Exception {
        coder.createBean();
    }

    @Test
    public void createDao() throws Exception {
        coder.createDao(SnippetType.BASE_DAO);
    }

    @Test
    public void createMapper() throws Exception {
        coder.createMapper();
    }

    @Test
    public void createService() throws Exception {
        coder.createService();
    }

    @Test
    public void createServiceImpl() throws Exception {
        coder.createServiceImpl();
    }

    @Test
    public void createController() throws Exception {
        coder.createController();
    }

    @Test
    public void createEnums() throws Exception {
        coder.init("t_order_prices_rel");
        coder.createEnums("rel_type");
    }

    @Test
    public void create() throws Exception {
        coder.create("t_customer", CreateType.BEAN, CreateType.MAPPER);
    }

    @Test
    public void snippetGetInstance() {
//        System.out.println(Coder.snippetGetInstance(User.class, "bean"));
    }

    @Test
    public void snippetBeanSetters() {
//        System.out.println(Coder.snippetBeanSetters(User.class, "bean"));
    }

    @Test
    public void sample() throws Exception {
        /*System.out.println(Coder.sample(String.class));
        System.out.println(Coder.sample(Integer.class));
        System.out.println(StringUtils.GSON_NULL.toJson(Coder.sample(User.class)));
        System.out.println(StringUtils.GSON_NULL.toJson(Coder.sample(Car.class)));
        System.out.println(StringUtils.GSON_NULL.toJson(Coder.sample("/home/ivanhjc/Projects/car-hailing/car-hailing-model/target/classes/", "com.winsky.carhailing.model.bo.RiskSetBO")));
        */
//        System.out.println(StringUtils.GSON_NULL.toJson(Coder.sample(User[].class)));
//        System.out.println(StringUtils.GSON_NULL.toJson(Coder.sample(int[].class)));
        System.out.println(StringUtils.GSON_NULL.toJson(Coder.sample(Map.class)));
    }

    // TODO: 5/21/19 Load class problem, Bean class problem
    @Test
    public void exportBluetoothPerformance() throws Exception {
//        2019-05-15 11:42:58.573 [XNIO-2 task-15] INFO  com.winsky.lease.eoms.app.handler.LoginInterceptor - /carApi/carControl {"engineNo":"5201277","opType":"1","state":"0","time":"6.519","workOrderId":"321161","userId":"290","carId":"676","order":"FLASHERS","token":"6024c69ce62b425db5a16dc53f5ae822"}, {"User-Agent":"okhttp/3.3.1","Connection":"close","OSTYPE":"Android","PHONETYPE":"XiaomiMI MAX 2","Host":"h5-web","APPVER":"2.0.5","Accept-Encoding":"gzip","Content-Length":"0","NETSTATE":"NETWORK_WIFI","OSVER":"Android7.1.1"
        POIUtils poiUtils = new POIUtils();
        poiUtils.open();

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        poiUtils.setBeanPackage("sample");
        map.put("ID", "LogData.carId");
        map.put("车牌", "LogData.license");
        map.put("车型", "LogData.model");
        map.put("蓝牙控制时间", "LogData.activateTime");
        map.put("蓝牙控制结果", "LogData.state");
        map.put("蓝牙响应时长(s)", "LogData.respTime");
        map.put("控制类型", "LogData.order");
        map.put("终端类型", "LogData.osType");
       /* List<LogData> data = new ArrayList<>();
        StringBuilder carIds = new StringBuilder();
        Scanner sc = new Scanner(new File(System.getProperty("user.home").concat("/Downloads/result")));
        while (sc.hasNext()) {
            String line = sc.nextLine();
            String[] jsons = org.apache.commons.lang3.StringUtils.substringsBetween(line, "{", "}");
            JsonObject jo = new JsonParser().parse("{" + jsons[0] + "}").getAsJsonObject();
            JsonObject jo2 = new JsonParser().parse("{" + jsons[1] + "}").getAsJsonObject();
            LogData logData = new LogData("", "", line.substring(0, 23), jo.get("state").getAsString().equals("0") ? "成功" : "失败", jo.get("time").getAsString(), jo.get("order").getAsString(), jo.get("carId").getAsLong(), jo2.get("OSTYPE").getAsString());
            data.add(logData);
            carIds.append(logData.getCarId()).append(",");
        }
        carIds.deleteCharAt(carIds.length() - 1);
        List<CarInfo> cars = new Coder().selectList("SELECT tc.id, tc.number, tcm.name FROM t_car tc LEFT JOIN t_car_model tcm ON tcm.id = tc.model_id WHERE tc.id IN (" + carIds + ")", CarInfo.class, ImmutableMap.of("id", "id", "license", "number", "model", "name"));
        System.out.println("lineNum: " + data.size());
        System.out.println("carIds: " + carIds);
        System.out.println("carNum:" + cars.size());
        for (LogData logData : data) {
            for (CarInfo car : cars) {
                if (logData.getCarId().intValue() == car.id.intValue()) {
                    logData.setLicense(car.license);
                    logData.setModel(car.model);
                    break;
                }
            }
        }

        poiUtils.insertTable(map, data);
        poiUtils.save();*/
//        coder.getConnection();
    }

    public static class CarInfo {
        public BigInteger id;
        public String license;
        public String model;
    }

    /*public static class LogData {
        private String license, model, activateTime, state, respTime, order;
        private Long carId;

        public LogData(String license, String model, String activateTime, String state, String respTime, String order, Long carId) {
            this.license = license;
            this.model = model;
            this.activateTime = activateTime;
            this.state = state;
            this.respTime = respTime;
            this.order = order;
            this.carId = carId;
        }

        public String getLicense() {
            return license;
        }

        public String getModel() {
            return model;
        }

        public String getActivateTime() {
            return activateTime;
        }

        public String getState() {
            return state;
        }

        public String getRespTime() {
            return respTime;
        }

        public String getOrder() {
            return order;
        }

        public Long getCarId() {
            return carId;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public void setActivateTime(String activateTime) {
            this.activateTime = activateTime;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setRespTime(String respTime) {
            this.respTime = respTime;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public void setCarId(Long carId) {
            this.carId = carId;
        }
    }*/

    @Test
    public void selectList() throws SQLException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        List<CarInfo> cars = new Coder().selectList("SELECT tc.id, tc.number, tcm.name FROM t_car tc LEFT JOIN t_car_model tcm ON tcm.id = tc.model_id WHERE tc.id IN (1,2,3)", CarInfo.class, ImmutableMap.of("id", "id", "license", "number", "model", "name"));
        System.out.println(StringUtils.GSON_NULL.toJson(cars));
    }

    public static void main(String[] args) throws Exception {
//        loadClass(LogData.class);
//        long time1 = System.nanoTime();
//        long time2 = System.currentTimeMillis();
//        System.out.println(time1);
//        System.out.println(System.nanoTime() - time1);
//        System.out.println(System.currentTimeMillis() - time2);
        File file = new File("/home/ivanhjc/Downloads/test.org");
        System.out.println(file.getName());
    }

    public static void loadClass(Class type) throws ClassNotFoundException, MalformedURLException {
        System.out.println(type.getName());
        URL url = new File("target/test-classes").toURI().toURL();
        System.out.println(url);
        ClassLoader cl = new URLClassLoader(new URL[]{url});
        Class c = cl.loadClass(type.getName());
        System.out.println(c);
        System.out.println(c.getProtectionDomain().getCodeSource().getLocation().getFile());
    }

    @Test
    public void importFromFileToDatabase() throws IOException, SQLException, ClassNotFoundException {
        Coder.importFromFileToDatabase("/home/ivanhjc/Downloads/用户管理-新增用户20190606.xlsx.Sheet1", "p_user",
                new String[]{"", "name", "realName", "password", "phone", "", "roleId[p_role.name->id]",
                        "organize[p_company.companyName->id]", "state[0:停用,1:启用]","createDate[now()]"}, "coder-ps.properties");
    }

    @Test
    public void formatResultSet() throws Exception {
        Connection connection = new Coder().getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM t_page");
        ResultSet rs = ps.executeQuery();
        System.out.println(StringUtils.formatTable(rs));
        rs.close();
        ps.close();
        connection.close();
    }

    @Test
    public void jsonToPojo() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        File file = new File("F:\\projects\\onenote\\src\\main\\resources\\schema\\WeChatUserInfo.json");
        URL source = file.toURI().toURL();
        GenerationConfig config = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() { // set config option by overriding method
                return true;
            }
        };

        SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
        mapper.generate(codeModel, "WeChatUserInfo", "wechat", source);

        codeModel.build(new File("F:\\projects\\onenote\\src\\main\\resources\\schema"));
    }
}
