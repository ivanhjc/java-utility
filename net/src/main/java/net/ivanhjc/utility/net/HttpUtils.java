package net.ivanhjc.utility.net;

import com.google.common.net.UrlEscapers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class HttpUtils {
    
    private Logger logger = LogManager.getLogger(this.getClass());

    private final int TIMEOUT = 1000 * 15;
    private final int RETRIES = 0;
    private CloseableHttpClient httpClient;
    private static HttpUtils instance = new HttpUtils();
    private static HttpUtils certInstance = null;

    private HttpUtils() {
        httpClient = generatorHttpClient();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (httpClient != null)
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }));
    }

    private HttpUtils(KeyStore keyStore, String mchId) throws Exception {
        httpClient = generatorHttpClient(keyStore, mchId);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }));
    }

    private CloseableHttpClient generatorHttpClient() {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", buildSSLConnectionSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(reg);
        connectionManager.setMaxTotal(500);
        connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setConnectionManager(connectionManager);
        SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
        socketConfigBuilder.setSoKeepAlive(true).setTcpNoDelay(true);
        socketConfigBuilder.setSoTimeout(TIMEOUT);
        SocketConfig socketConfig = socketConfigBuilder.build();
        httpClientBuilder.setDefaultSocketConfig(socketConfig);
        connectionManager.setDefaultSocketConfig(socketConfig);
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(RETRIES, false));
        return httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
    }

    /**
     * 获取证书的CloseableHttpClient
     */
    private CloseableHttpClient generatorHttpClient(KeyStore ks, String mchId) throws Exception {
        BasicHttpClientConnectionManager connManager;
        // 证书
        char[] password = mchId.toCharArray();
        // 实例化密钥库 & 初始化密钥工厂
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password);
        // 创建 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1"}, null, new DefaultHostnameVerifier());
        connManager = new BasicHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory).build(), null, null, null
        );
        return HttpClientBuilder.create().setConnectionManager(connManager).build();
    }

    private SSLConnectionSocketFactory buildSSLConnectionSocketFactory() {
        try {
            return new SSLConnectionSocketFactory(createIgnoreVerifySSL(), (hostname, session) -> true); // 优先绕过安全证书
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error("ssl connection fail", e);
        }
        return SSLConnectionSocketFactory.getSocketFactory();
    }

    private SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    private String send(HttpRequestBase request) {
        CloseableHttpResponse response;
        HttpEntity entity = null;
        String responseContent = null;
        try {
            request.setConfig(RequestConfig.custom()
                    .setSocketTimeout(TIMEOUT)
                    .setConnectTimeout(TIMEOUT)
                    .setConnectionRequestTimeout(TIMEOUT)
                    .build());
            response = httpClient.execute(request);
            if (response != null) {
                entity = response.getEntity();
                responseContent = EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
            }
            return responseContent;
        } catch (Exception e) {
            logger.error("Http request error!", e);
            return null;
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    public static HttpUtils getInstance() {
        return instance;
    }

    public static HttpUtils getCertInstance(KeyStore keyStore, String mchId) throws Exception {
        certInstance = new HttpUtils(keyStore, mchId);
        return certInstance;
    }

    /**
     * Send a GET request
     *
     * @param url the request url with parameters
     * @return response string
     */
    public String get(String url) {
        return send(new HttpGet(url));
    }

    /**
     * Send a GET request
     *
     * @param url    the host address
     * @param params key-value pairs
     * @return response string
     */
    public String get(String url, String params) {
        return send(new HttpGet(url + '?' + params));
    }

    /**
     * Send a GET request
     *
     * @param url    the host address
     * @param params key-value pairs
     * @return response string
     */
    public String get(String url, Map<String, Object> params) {
        return send(new HttpGet(url + '?' + getQueryString(params)));
    }

    /**
     * Send a POST request with only string parameters
     */
    public String post(String url) {
        return send(new HttpPost(url));
    }

    /**
     * Send a POST request with parameters
     *
     * @param url    host address
     * @param params parameters in the form key1=value1&key2=value2
     */
    public String post(String url, String params) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(params, StandardCharsets.UTF_8.name());
        stringEntity.setContentType("application/x-www-form-urlencoded");
        httpPost.setEntity(stringEntity);
        return send(httpPost);
    }

    /**
     * Send a POST request with parameters
     *
     * @param url    host address
     * @param params parameters stored in a map
     */
    public String post(String url, Map<String, Object> params) {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> pairs = new ArrayList<>();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value != null) {
                pairs.add(new BasicNameValuePair(key, value.toString()));
            }
        }

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return send(httpPost);
    }

    /**
     * Send a POST request with parameters in JSON format
     *
     * @param url    host address
     * @param params parameters in JSON format
     */
    public String postJSON(String url, String params) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(params, StandardCharsets.UTF_8);
        stringEntity.setContentType("application/json;charset=UTF-8");
        httpPost.setEntity(stringEntity);
        return send(httpPost);
    }

    /**
     * Send a POST request with parameters in XML format
     *
     * @param url    host address
     * @param params parameters in XML format
     */
    public String postXML(String url, String params) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(params, StandardCharsets.UTF_8);
        stringEntity.setContentType("application/xml;charset=UTF-8");
        httpPost.setEntity(stringEntity);
        return send(httpPost);
    }

    /**
     * Send a post request with parameters and files
     *
     * @param url    request URL
     * @param params parameters, files sent as key-value pairs
     */
    public String postFiles(String url, Map<String, Object> params) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof File)
                builder.addPart(entry.getKey(), new FileBody((File) entry.getValue()));
            else
                builder.addPart(entry.getKey(), new StringBody(entry.getValue().toString(), ContentType.TEXT_PLAIN));
        }
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(builder.build());
        return send(httpPost);
    }

    /**
     * Send a post request with a list of files
     *
     * @param url    request url
     * @param params parameters
     * @param files  list of files
     */
    public String postFiles(String url, Map<String, String> params, List<File> files) {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (String key : params.keySet()) {
            builder.addPart(key, new StringBody(params.get(key), ContentType.TEXT_PLAIN));
        }
        for (File file : files) {
            FileBody fileBody = new FileBody(file);
            builder.addPart("files", fileBody);
        }
        HttpEntity reqEntity = builder.build();
        httpPost.setEntity(reqEntity);
        return send(httpPost);
    }

    /**
     * 双向认证请求
     */
    public String postCert(String apiUrl, String requestXml) throws IOException {
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(RequestConfig.custom()
                .setSocketTimeout(TIMEOUT)
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .build());
        httpPost.addHeader("Connection", "keep-alive");
        httpPost.addHeader("Accept", "*/*");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.addHeader("Host", "api.mch.weixin.qq.com");
        httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.addHeader("Cache-Control", "max-age=0");
        httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
        httpPost.setEntity(new StringEntity(requestXml, "UTF-8"));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        String result = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        return result;
    }

    public void downloadFile(String url, File file) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            fileOutputStream.flush();
        }
    }

    public static <T> String getQueryString(Map<String, T> map) {
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, T>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, T> entry = iterator.next();
            if (entry.getValue() == null) {
                continue;
            }
            StringBuilder value = new StringBuilder();
            if (entry.getValue() instanceof String[]) {
                for (String s : ((String[]) entry.getValue())) {
                    value.append(s).append(",");
                }
                if (value.length() > 0) {
                    value.deleteCharAt(value.length() - 1);
                }
            } else {
                value.append(entry.getValue());
            }

            builder.append(entry.getKey()).append("=").append(value);
            if (iterator.hasNext()) {
                builder.append("&");
            }
        }
        return builder.toString();
    }

    public static String toURLParamsIncludeAll(Object bean) throws Exception {
        Class clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder nameValuePairs = new StringBuilder();
        for (Field field : fields) {
            Object value = clazz.getDeclaredMethod("get".concat(StringUtils.capitalize(field.getName()))).invoke(bean);
            nameValuePairs.append(field.getName()).append("=").append(value == null ? "" : value.toString()).append("&");
        }
        return nameValuePairs.deleteCharAt(nameValuePairs.length() - 1).toString();
    }

    public static String toURLParamsIncludeNonNull(Object bean) throws Exception {
        Class clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder nameValuePairs = new StringBuilder();
        for (Field field : fields) {
            Object value = clazz.getDeclaredMethod("get".concat(StringUtils.capitalize(field.getName()))).invoke(bean);
            if (value == null)
                continue;
            nameValuePairs.append(field.getName()).append("=").append(value.toString()).append("&");
        }
        return nameValuePairs.deleteCharAt(nameValuePairs.length() - 1).toString();
    }

    /**
     * Converts a POJO object to a string of key-value pairs excluding separators and pairs whose values are blank (null, "", or "   ")
     *
     * @param bean the POJO object
     * @param asc  true if the keys are in ascending order, false if the keys are in descending order, and null if the keys don't need to be sorted
     * @return the converted string
     * @throws Exception if the object is not in a standard POJO format
     */
    public static String toURLParamsExcludeSeparatorsAndBlanks(Object bean, Boolean asc) throws Exception {
        Class clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if (asc != null) {
            if (asc)
                Arrays.sort(fields, Comparator.comparing(Field::getName));
            else
                Arrays.sort(fields, (o1, o2) -> o2.getName().compareTo(o1.getName()));
        }
        StringBuilder nameValuePairs = new StringBuilder();
        for (Field field : fields) {
            Object value = clazz.getDeclaredMethod("get".concat(StringUtils.capitalize(field.getName()))).invoke(bean);
            if (value == null || value.toString().trim().isEmpty())
                continue;
            nameValuePairs.append(field.getName()).append(value.toString());
        }
        return nameValuePairs.toString();
    }

    /**
     * Converts a JSON string to an application/x-www-form-urlencoded format string
     */
    public static String toURLFromJSON(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        StringBuilder urlParams = new StringBuilder();
        jsonObject.entrySet().forEach(entry -> urlParams.append(entry.getKey()).append("=").append(entry.getValue().getAsString()).append("&"));
        urlParams.deleteCharAt(urlParams.length() - 1);
        return UrlEscapers.urlPathSegmentEscaper().escape(urlParams.toString());
    }

    /**
     * Returns a map converted from the default Map&lt;String, String[]> returned from the default request.getParameterMap() method, where the values of
     * the parameters are stored as an object if they only have one element.
     */
    public static Map<String, Object> getParameterMap(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> result.put(key, value.length == 1 ? value[0] : value));
        return result;
    }

    public static String getRemoteHost(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }

    public static Map<String, Object> requestToMap(HttpServletRequest request) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<String, String[]> map = request.getParameterMap();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String[] value = map.get(key);
            if (value.length == 1)
                paramMap.put(key, value[0]);
            else
                paramMap.put(key, value);
        }
        return paramMap;
    }

    public static String decodeUrl(String url) throws UnsupportedEncodingException {
        String url2 = URLDecoder.decode(url, "UTF-8");
        while (url2.contains("%")) {
            url2 = URLDecoder.decode(url2, "UTF-8");
        }
        return url2;
    }

    public static void downloadViaMultipart(String url, String saveDir) {
        long t1 = System.currentTimeMillis();
        try (InputStream in = new URL(url).openConnection().getInputStream()) {
            long t2 = System.currentTimeMillis();
            System.out.println("uploadWX openStream time: " + ((t2 - t1) / 1e3) + "s");
            Path fileItemPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("tmpt.jpg");
            DiskFileItem fileItem = new DiskFileItem("file", "image", false, fileItemPath.getFileName().toString(), (int) 1e6, fileItemPath.getParent().toFile());
            try (OutputStream out = fileItem.getOutputStream()) {
                IOUtils.copy(in, out);
            }
            long t3 = System.currentTimeMillis();
            System.out.println("uploadWX copy time: " + ((t3 - t2) / 1e3) + "s");

            /*CommonsMultipartFile multipartFile = new CommonsMultipartFile(fileItem);
            multipartFile.transferTo(new File(saveDir));*/

            System.out.println("uploadWX total time: " + ((System.currentTimeMillis() - t1) / 1e3) + "s");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
