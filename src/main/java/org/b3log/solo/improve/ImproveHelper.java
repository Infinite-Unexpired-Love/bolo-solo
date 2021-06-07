package org.b3log.solo.improve;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.b3log.latke.Latkes;
import org.b3log.latke.servlet.RequestContext;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * 感谢您参与「Bolo 用户体验改进计划」
 * 这里是用户体验改进计划的信息收集类
 */
public class ImproveHelper {

    // 提交反馈服务器地址
    private static String helperHost = "http://localhost:4399/Log";

    /**
     * 上传站点详情相关代码
     * 线程安全，当存储足够条目才会上传一次
     * @param context 请求数据
     */
    public static synchronized void uploadSiteStatistics(final RequestContext context) {
        JSONObject statisticsObject = new JSONObject();
        statisticsObject.put("category", "statistics");

        JSONObject statisticsDataObject = new JSONObject();
        HttpServletRequest request = context.getRequest();

        /**
         * 隐私信息说明
         * serverHost：博客的服务端地址
         * requestURL：访问者访问的URL地址
         * ClientIP：访问者的IP地址，去敏处理
         */
        statisticsDataObject.put("serverHost", Latkes.getStaticServePath());
        statisticsDataObject.put("requestURL", request.getRequestURI());
        String clientIP;
        try {
            clientIP = request.getRemoteHost();
            String[] ipSplit = clientIP.split("\\.");
            clientIP = ipSplit[0] + "." + ipSplit[1] + "." + ipSplit[2] + ".*";
        } catch (Exception e) {
            clientIP = request.getRemoteHost();
        }
        statisticsDataObject.put("clientIP", clientIP);

        statisticsObject.put("data", statisticsDataObject);

        CloseableHttpClient uploadSiteStatisticsHttpClient = createSSLClientDefault();
        HttpPost httpPost = new HttpPost(helperHost);
        String params = statisticsObject.toString();
        StringEntity httpEntity = new StringEntity(params, "utf-8");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(httpEntity);

        try {
            CloseableHttpResponse response = uploadSiteStatisticsHttpClient.execute(httpPost);
            response.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * 设置可访问HTTPS
     *
     * @return null
     */
    public static CloseableHttpClient createSSLClientDefault() {
        try {
            //信任所有
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }
}