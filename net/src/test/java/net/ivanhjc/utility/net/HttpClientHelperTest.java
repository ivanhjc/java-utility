package net.ivanhjc.utility.net;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.net.model.DingDingRobotReq;
import net.ivanhjc.utility.net.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator on 2018/9/28 16:55.
 */
public class HttpClientHelperTest {
    private static Logger logger = LogManager.getLogger(HttpClientHelperTest.class.getName());
    private final static String URL = "http://localhost:8080";

    public static void main(String[] args) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "d29ecb4d598c6f7967b1e54386810a75");
        params.put("name", "陈星");
        params.put("idcard", "530328199804043311");
        params.put("imageId", "8db671390fac47a59f32f17019f87cb2");
        logger.info("REQ: " + new Gson().toJson(params));
        String result = HttpClientHelper.getInstance().post("http://api.chinadatapay.com/communication/personal/2061", params);
        logger.info("RES: " + result);
    }

    @Test
    public void personVerify() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "d29ecb4d598c6f7967b1e54386810a75");
        params.put("name", "陈星");
        params.put("idcard", "530328199804043311");
        params.put("imageId", "8db671390fac47a59f32f17019f87cb2");
        System.out.println("REQ: " + new Gson().toJson(params));
        String result = HttpClientHelper.getInstance().post("http://api.chinadatapay.com/communication/personal/2061", params);
        System.out.println("RES: " + result);

//        {"imageId":"1fe59c2a9e6346f486f72f6d5b9e3f78","idcard":"530328199804043311","name":"陈星","key":"d29ecb4d598c6f7967b1e54386810a75"}
//        P5H1V2L51909191824
//        Z9K46DER1909191823
    }

    @Test
    public void post() {
        String url = URL.concat("/page/add");
        String params = "name=test&title=test&content=test";
        logger.info("SEND: ".concat(url).concat("?").concat(params));
        String resp = HttpClientHelper.getInstance().post(url, params);
        logger.info("RESP: ".concat(resp));
    }

    @Test
    public void toURLParamsIncludeAll() throws Exception {
        User user = new User();
        user.setMobile("1234");
        System.out.println(HttpClientHelper.toURLParamsIncludeAll(user));
    }

    @Test
    public void toURLParamsIncludeNonNull() throws Exception {
        User user = new User();
        user.setMobile("1234");
        System.out.println(HttpClientHelper.toURLParamsIncludeNonNull(user));
    }

    @Test
    public void toURLParamsExcludeSeparatorsAndBlanks() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setMobile("  ");
        user.setUsername("Jack");
        System.out.println(HttpClientHelper.toURLParamsExcludeSeparatorsAndBlanks(user, null));
    }

    @Test
    public void getQueryString() {
        /*Map<String, String[]> map = new HashMap<>();
        map.put("person", new String[]{"mark", "lucy"});
        map.put("age", new String[]{"10", "12"});*/
        Map<String, Object> map = new HashMap<>();
        map.put("person", new String[]{"mark", "lucy"});
        map.put("age", 12);
        System.out.println(HttpClientHelper.getQueryString(map));
    }

    @Test
    public void encodeURL() throws UnsupportedEncodingException {
        System.out.println(URLEncoder.encode("https://jiabei.pek3a.qingstor.com/Uploads/c7/11/c711f9810b2f6eefb30dae2f31eb2c574838c900.jpeg?access_key_id=EPUHMPCDGZPADSDBDGZC&expires=1539995890&signature=I3fkdu9R1%2BbYVhYU8O0Hetejhj1QcSEGh4mtghfsGb0%3D", "UTF-8"));
//        System.out.println(UrlEscapers.urlFragmentEscaper().escape("http://apiproxy-uat.ctripqa.com/apiproxy/soa2/14720/json/syncAbnormal2?key=13036&sign=0cd30897e15c4de71d5dfaad90107327&requestTime=1538288245772&vendorOrderId=262142&abnormalCode=WZD201809251042566673&status=0&abnormalType=2&city=深圳&occurTime=2018-09-29 10:44:12&address=高新园&reason=闯红灯&occurAmount=50.0&peccancyAmount=50.0&degree=5"));
//        System.out.println(UriUtils.encodeFragment("http://apiproxy-uat.ctripqa.com/apiproxy/soa2/14720/json/syncAbnormal2?key=13036&sign=0cd30897e15c4de71d5dfaad90107327&requestTime=1538288245772&vendorOrderId=262142&abnormalCode=WZD201809251042566673&status=0&abnormalType=2&city=深圳&occurTime=2018-09-29 10:44:12&address=高新园&reason=闯红灯&occurAmount=50.0&peccancyAmount=50.0&degree=5", "UTF-8"));
    }

    @Test
    public void sendPost() throws FileNotFoundException {
        Gson GSON_NULL = new GsonBuilder().serializeNulls().create();
        DingDingRobotReq req = new DingDingRobotReq();
        req.setMsgtype("text");
        req.setText(new DingDingRobotReq.Text("明天加班"));
//        req.setAt(new DingDingRobotReq.At(Arrays.asList("18688948851"), false));
        String params = GSON_NULL.toJson(req);
        System.out.println(params);
//        https://oapi.dingtalk.com/robot/send?access_token=a7ac0e076244918552df85e729b349663b214fd68ec3cc3b812c6d4e9dba93ad
        String resp = HttpClientHelper.getInstance().postJSON("https://oapi.dingtalk.com/robot/send?access_token=a7ac0e076244918552df85e729b349663b214fd68ec3cc3b812c6d4e9dba93ad", params);
        System.out.println(resp);
    }

    @Test
    public void downloadFile() throws IOException {
        File file = new File("/home/ivanhjc/Downloads/test.jpg");
        HttpClientHelper.getInstance().downloadFile("", file);
        System.out.println(file.exists());
    }

    @Test
    public void toURLFromJSON() throws UnsupportedEncodingException {
        String json = "{\"cityNo\":\"4403\",\"orderType\":\"0\",\"downLat\":\"22.543528\",\"carModelId\":\"1\",\"authToken\":\"2bd5aa8eba674d338b7f848777cca9e5\",\"onAddress\":\"中电迪富大厦\",\"isCall\":\"0\",\"authId\":\"1\",\"downLgt\":\"114.057938\",\"onLat\":\"22.543991\",\"onLgt\":\"114.089629\",\"adCode\":\"440304\",\"applyTime\":\"\",\"downAddress\":\"深圳市政府\",\"reqSource\":\"3\",\"productType\":\"0\"}";
//        gmt_create=2019-10-29%2018:48:33&charset=UTF-8&seller_email=yszhcw_szzc@win-sky.com.cn&subject=%E9%99%B6%E9%99%B6%E5%87%BA%E8%A1%8C&sign=gOiCUuUwjELG5UD1vDYKMV1b7IaNfc0sAftw1JcCFlUNfNcxmSv+7Xk6/mR2q+eB8Q2MPw+mRXV+lJITBQfPUwjVZddxUv1YHLuPJn4+XygSydFdcwU5CTFa+ia2w766L+k238qY7SrMdwixt7qvEjVmx6RNjvk1IYL8vI85WHs7ACOKUm1cTvhJ9DocAMwSzWsN5HBTrZKPnSZSNf1IvwGV+ReNTXdTieBKewLTA5kT26IquSa1KbF/blwfTsEs4VWmcw/hRyg6n7HOXjpuKGe930gdZRHqi5zlJzu2BILKKEMxjrIBNl0pjC2+lzkZUHPvQgxrAmcbGNfxog==&body=%E9%99%B6%E9%99%B6%E5%87%BA%E8%A1%8C%E8%AE%A2%E5%8D%95%E6%94%AF%E4%BB%98&buyer_id=2088412114150335&invont=0.01&notify_id=2019102900222184834050330523180734&fund_bill_list=%5B%7B%22amount%22:%220.01%22,%22fundChannel%22:%22PCREDIT%22%7D%5D&notify_type=trade_status_sync&trade_status=TRADE_SUCCESS&receipt_amount=0.01&app_id=2019101268303724&buyer_pay_amount=0.01&sign_type=RSA2&seller_id=2088631347999463&gmt_payment=2019-10-29%2018:48:33&notify_time=2019-10-29%2018:51:24&version=1.0&out_trade_no=OP2019102918482646497&total_amount=0.01&trade_no=2019102922001450330506688722&auth_app_id=2019101268303724&buyer_logon_id=173****2330&point_amount=0.00
// gmt_create%3D2019-10-29+18%3A48%3A33%26charset%3DUTF-8%26seller_email%3Dyszhcw_szzc%40win-sky.com.cn%26subject%3D%E9%99%B6%E9%99%B6%E5%87%BA%E8%A1%8C%26sign%3DgOiCUuUwjELG5UD1vDYKMV1b7IaNfc0sAftw1JcCFlUNfNcxmSv%2B7Xk6%2FmR2q%2BeB8Q2MPw%2BmRXV%2BlJITBQfPUwjVZddxUv1YHLuPJn4%2BXygSydFdcwU5CTFa%2Bia2w766L%2Bk238qY7SrMdwixt7qvEjVmx6RNjvk1IYL8vI85WHs7ACOKUm1cTvhJ9DocAMwSzWsN5HBTrZKPnSZSNf1IvwGV%2BReNTXdTieBKewLTA5kT26IquSa1KbF%2FblwfTsEs4VWmcw%2FhRyg6n7HOXjpuKGe930gdZRHqi5zlJzu2BILKKEMxjrIBNl0pjC2%2BlzkZUHPvQgxrAmcbGNfxog%3D%3D%26body%3D%E9%99%B6%E9%99%B6%E5%87%BA%E8%A1%8C%E8%AE%A2%E5%8D%95%E6%94%AF%E4%BB%98%26buyer_id%3D2088412114150335%26invont%3D0.01%26notify_id%3D2019102900222184834050330523180734%26fund_bill_list%3D%5B%7B%22amount%22%3A%220.01%22%2C%22fundChannel%22%3A%22PCREDIT%22%7D%5D%26notify_type%3Dtrade_status_sync%26trade_status%3DTRADE_SUCCESS%26receipt_amount%3D0.01%26app_id%3D2019101268303724%26buyer_pay_amount%3D0.01%26sign_type%3DRSA2%26seller_id%3D2088631347999463%26gmt_payment%3D2019-10-29+18%3A48%3A33%26notify_time%3D2019-10-29+18%3A51%3A24%26version%3D1.0%26out_trade_no%3DOP2019102918482646497%26total_amount%3D0.01%26trade_no%3D2019102922001450330506688722%26auth_app_id%3D2019101268303724%26buyer_logon_id%3D173****2330%26point_amount%3D0.00
//        gmt_create=2019-10-29%2018:48:33&charset=UTF-8&seller_email=yszhcw_szzc@win-sky.com.cn&subject=%E9%99%B6%E9%99%B6%E5%87%BA%E8%A1%8C&sign=JJHnyKGISX6W4%2BfBv8Pl9JNHvdpKi715Ur4JxpIyNI8Ky%2BUkVtrkbwIAGpmeMspn9PJVSYwoXV15WEBnuwbybQGxzC5E2QSOq17VBXb502s%2B%2BRaBXL68ZFp9EO1OFLZuKm%2FM2H16wfodzyUYo42LuOgEkoEdhMziTw67UypM1TWKBOycGwVIqTNYN5JU8q4u2VfeltNk4JUeCExnKc63JC%2FDNsWWmsiVZTsKXDc2ra%2BqCyr9wvuIsANlylN%2FHLrJb%2F2yK1mdxgiIiXn0fyKdumGuCBcKKRnTeaCj7DKyD2M7ot2JPTRrNk99ZWrtDWOmNrhac8%2FHc9a2oVvj9qXmbw==&body=%E9%99%B6%E9%99%B6%E5%87%BA%E8%A1%8C%E8%AE%A2%E5%8D%95%E6%94%AF%E4%BB%98&buyer_id=2088412114150335&invoice_amount=0.01&notify_id=2019102900222184834050330523180734&fund_bill_list=%5B%7B%22amount%22:%220.01%22,%22fundChannel%22:%22PCREDIT%22%7D%5D&notify_type=trade_status_sync&trade_status=TRADE_SUCCESS&receipt_amount=0.01&app_id=2019101268303724&buyer_pay_amount=0.01&sign_type=RSA2&seller_id=2088631347999463&gmt_payment=2019-10-29%2018:48:33&notify_time=2019-10-29%2019:02:49&version=1.0&out_trade_no=OP2019102918482646497&total_amount=0.01&trade_no=2019102922001450330506688722&auth_app_id=2019101268303724&buyer_logon_id=173****2330&point_amount=0.00
        System.out.println(HttpClientHelper.toURLFromJSON(json));
    }


}
