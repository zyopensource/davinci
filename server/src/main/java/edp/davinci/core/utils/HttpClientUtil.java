package edp.davinci.core.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @ClassName:     HttpClientUtils.java
 * @Description:   TODO
 * @author         Somnus
 * @version        V1.0
 * @Date           2016年10月13日 下午3:40:17
 */
public class HttpClientUtil {

    private transient static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * @param url
     * @param param
     * @return
     * @throws HttpHostConnectException
     * 				连接不可用
     * @throws IOException
     * 				网络传输出错
     */
    public static String doJsonPost(String url, Map<String,String> param) throws HttpHostConnectException, IOException{
        Validate.notNull(url, "url is required.");
        Validate.notEmpty(param);
        return doJsonPost(url,JSON.toJSONString(param));
    }

    /**
     * @param url
     * @param json
     * @return
     * @throws HttpHostConnectException
     * 				连接不可用
     * @throws IOException
     * 				网络传输出错
     */
    public static String doJsonPost(String url, String json) throws HttpHostConnectException, IOException{
        Validate.notNull(url, "url is required.");
        Validate.notNull(json, "json is required.");
        //创建HttpClient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resultString = "";
        CloseableHttpResponse httpResponse = null;
        try {
            // 创建HttpPost对象
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(json,ContentType.APPLICATION_JSON));

            // 开始执行http请求
            long startTime = System.currentTimeMillis();
            httpResponse = httpclient.execute(httpPost);
            long endTime = System.currentTimeMillis();

            // 获得响应状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.info("statusCode:" + statusCode);
            logger.info("调用API 花费时间(单位：毫秒)：" + (endTime - startTime));

            // 取出应答字符串
            HttpEntity httpEntity = httpResponse.getEntity();
            resultString = EntityUtils.toString(httpEntity,Charset.forName("UTF-8"));

//            // 判断返回状态是否为200
//            if (statusCode != HttpStatus.SC_OK) {
//                throw new HttpStatusException(String.format("\n\tStatus:%s\n\tError Message:%s", statusCode,resultString));
//            }
        } finally{
            if(httpResponse != null){
                httpResponse.close();
            }
            httpclient.close();
        }
        return resultString;
    }


    public static String doJsonPut(String url, Map<String,String> param) throws HttpHostConnectException, IOException{
        Validate.notNull(url, "url is required.");
//        Validate.notEmpty(param);
        return doJsonPut(url, JSON.toJSONString(param));
    }

    public static String doJsonPut(String url, String json) throws HttpHostConnectException, IOException{
        Validate.notNull(url, "url is required.");
        Validate.notNull(json, "json is required.");
        //创建HttpClient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resultString = "";
        CloseableHttpResponse httpResponse = null;
        try {
            // 创建HttpPost对象

            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(json,ContentType.APPLICATION_JSON));

            // 开始执行http请求
            long startTime = System.currentTimeMillis();
            httpResponse = httpclient.execute(httpPut);
            long endTime = System.currentTimeMillis();

            // 获得响应状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.info("statusCode:" + statusCode);
            logger.info("调用API 花费时间(单位：毫秒)：" + (endTime - startTime));

            // 取出应答字符串
            HttpEntity httpEntity = httpResponse.getEntity();
            resultString = EntityUtils.toString(httpEntity,Charset.forName("UTF-8"));

        } finally{
            if(httpResponse != null){
                httpResponse.close();
            }
            httpclient.close();
        }
        return resultString;
    }

}
