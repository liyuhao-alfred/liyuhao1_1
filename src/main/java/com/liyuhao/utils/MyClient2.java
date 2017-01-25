/******************************************************************
 *    
 *    其中：
 *    loadKeyMaterial()重载方法是加载客户端证书用的
 *    loadTrustMaterial()重载方法是加载服务器端相关信息用的(我们就是使用 loadTrustMaterial(TrustStrategy trustStrategy) 
 *    方法自己实现了一个信任策略，不对服务器端的证书进行校验)，在生成HttpClient的时候，指定相应的 SSLSocketFactory，
 *    之后，使用这个HttpClient发送的GET请求和POST请求就自动地附加上了证书信息
 *    如果我们只需要忽略掉对服务器端证书的验证，而不需要发送客户端证书信息,在构建SSLContext的时候，
 *    只需要 loadTrustMaterial() 不需要 loadKeyMaterial()
 *
 *    @author:     阿拉甲
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年1月25日 上午10:05:59
 *
 *    - first revision
 *
 *****************************************************************/
package com.liyuhao.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.liyuhao.action.AdminAction;

/**
 * 用于处理双向ssl请求
 * 该类为自己封装的httpclient工具类提供了调用post请求的方法便于代码的复用
 * 
 * @ClassName MyClient2
 * @author 阿拉甲
 * @Date 2017年1月25日 上午9:52:20
 * @version 1.0.0
 */
public abstract class MyClient2 implements Serializable {
	private static final Logger log = Logger.getLogger(AdminAction.class);
	private static final long serialVersionUID = 1L;
	private static Properties prop;
	static {
		prop = new Properties();
		try {
			prop.load(MyClient2.class.getClassLoader().getResourceAsStream("key.properties"));
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
		// 初始化客户端证书输入流
		FileInputStream inputStreamC = null;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			// 防止未关闭，所以初始化
			inputStreamC = new FileInputStream(new File(prop.getProperty("clientCA")));
			keyStore.load(inputStreamC, prop.getProperty("password").toCharArray());
			SSLContext sslcontext = SSLContexts.custom()
					// 忽略掉对服务器端证书的校验
					/*
					 * .loadTrustMaterial(new TrustStrategy() { public boolean
					 * isTrusted(X509Certificate[] chain, String authType)
					 * throws CertificateException { return true; } })
					 */

					// 加载服务端提供的密钥库(如果服务器提供密钥库的话就不用忽略对服务器端证书的校验了)
					.loadTrustMaterial(new File(prop.getProperty("keyStore")),
							prop.getProperty("password").toCharArray(), new TrustSelfSignedStrategy())
					// 加载客户端证书
					.loadKeyMaterial(keyStore, prop.getProperty("password").toCharArray()).build();
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext,
					new String[] { "TLSv1" }, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
					.build();
			return httpclient;

		} finally {
			if (inputStreamC != null) {
				try {
					inputStreamC.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 利用httpclient发送post请求
	 * 
	 * @param urlPath    请求的路径
	 * @param map        封装请求参数到map             
	 * @param charset    字符编码
	 * @return   result  一个response实体内容字符串
	 * @throws Exception 谁调用谁处理 
	 */

	public static String httpPost(String urlPath, Map<String, String> map, String charset) throws Exception {
		// 获取受证书认证的httpclient
		CloseableHttpClient httpClient = MyClient2.getTrustHttpClient();
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
