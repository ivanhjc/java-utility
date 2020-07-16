package net.ivanhjc.metanote.common.utils.sign;


import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class CommonSignUtils {
    private static final Logger log = LoggerFactory.getLogger(CommonSignUtils.class);

    private static final String CHARSET = "utf-8";
    public static final String key = "jiabai201708";

    /**
     * 构造签名源串 将除“sign”外的所有参数按 key 进行字典升序排列，然后进行 URL 编码得到lpNum%3D%E4%BA%ACQWB1121%26uid%3D2567892221
     * <p>
     * 生成签名值
     * <p>
     * Step4 ： 使用 HMAC-SHA1 加密算法，用私钥对源串加密。
     * <p>
     * Step5 ： 然后将加密后的字符串经过 Base64 编码得到签名值
     * <p>
     * e)最后提交接口的参数为
     * <p>
     * uid=2567892221&lpNum=京QWB1121&sign=FdJkiDYwMj5Aj1UG2RUPc83iokk=
     *
     * @param request
     * @return
     */
    public static String generateSign(Map<String, Object> request) {
        List<String> keys = new ArrayList<>();
        keys.addAll(request.keySet());
        Collections.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (String k : keys) {
            sb.append(k).append("=").append(request.get(k)).append("&");
        }
        sb.replace(sb.length() - 1, sb.length(), "");
        String source = null;
        try {
            source = URLEncoder.encode(sb.toString(), CHARSET);
            System.out.println(source);
        } catch (UnsupportedEncodingException e) {
            log.error("encode parameter error", e);
        } catch (Exception e) {
            log.error("encode have an exception", e);
        }
        byte[] b = HmacSha1Signature.sign(source, key);
        String serverSign = new String(Base64.encode(b));

        return serverSign;
    }

    public static String generateSign(Object object) {
        String source = null;
        try {
            source = URLEncoder.encode(new Gson().toJson(object), CHARSET);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sig = new String(Base64.encode(HmacSha1Signature.sign(source, key)));
        return sig;
    }
}
