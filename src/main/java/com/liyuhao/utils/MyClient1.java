package com.liyuhao.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.liyuhao.action.AdminAction;

/**
 * 用于处理单向ssl请求
 * 该类为自己封装的httpclient工具类提供了调用post请求的方法便于代码的复用
 * 
 * @ClassName MyClient1
 * @author 阿拉甲
 * @Date 2017年1月25日 上午9:29:54
 * @version 1.0.0
 */
public abstract class MyClient1 implements Serializable {
	private static final Logger log = Logger.getLogger(AdminAction.class);
	private static final long serialVersionUID = 1L;
	private static Properties prop;
	static {
		prop = new Properties();
		try {
			prop.load(MyClient1.class.getClassLoader().getResourceAsStream("key.properties"));
			log.info("keyStore:" + prop.getProperty("keyStore"));
			log.info("clientCA:" + prop.getProperty("clientCA"));
			log.info("password:" + prop.getProperty("password"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 加载密钥库，自动信任服务端证书，并生成可信任的CloseableHttpClient
	 * 
	 * @return
	 * @throws Exception
	 */
	private static CloseableHttpClient getTrustHttpClient() throws Exception {

		FileInputStream inputStream = null;
		try {
			// 一般默认为：KeyStore keyStore = KeyStore.getInstance("PKCS12");
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			// 防止未关闭，所以初始化
			inputStream = new FileInputStream(new File(prop.getProperty("keyStore")));
			// 加载密钥库
			trustStore.load(inputStream, prop.getProperty("password").toCharArray());
			SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
					.build();
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext);
			CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
					.build();
			return httpclient;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 
	 * 利用httpclient发送post请求
	 * 
	 * @param urlPath    请求的路径
	 * @param map        封装请求参数到map             
	 * @param charset    字符编码
	 * @return   result  一个response实体内容字符串
	 * @throws Exception
	 */
	public static String httpPost(String urlPath, Map<String, String> map, String charset) throws Exception {
		// 获取受证书认证的httpclient
		CloseableHttpClient httpClient = MyClient1.getTrustHttpClient();
		// 封装的结果集
		String result = null;
		try {
			// 创建http请求(post请求)
			HttpPost httpPost = new HttpPost(urlPath);
			// 设置参数
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			// 迭代map拿到参数传递给 list
			if (!map.isEmpty()) {
				Set<String> keySet = map.keySet();
				for (String key : keySet) {
					// 传递给list
					list.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			if (!list.isEmpty()) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
				httpPost.setEntity(entity);
			}
			log.info("executing request" + httpPost.getRequestLine());
			CloseableHttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
			return result;
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}
	}
}
