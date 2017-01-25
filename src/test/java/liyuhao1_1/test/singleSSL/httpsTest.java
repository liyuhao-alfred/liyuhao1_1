/******************************************************************
 *    
 *    Package:     logindemo.https.test
 *
 *    Filename:    package-info.java
 *
 *    @author:     阿拉甲
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年1月24日 下午4:09:26
 *
 *    - first revision
 *
 *****************************************************************/
/**
 * 
 * 
 * @ClassName package-info
 * @author 阿拉甲
 * @Date 2017年1月24日 下午4:09:26
 * @version 1.0.0
 */
package liyuhao1_1.test.singleSSL;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.liyuhao.utils.MyClient1;

public class httpsTest {

	/**
	 * 单向ssl,当服务器配置双向时候，失效
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// 第三方接口的路径
		String urlPath = "https://localhost:8443/liyuhao1/admin1/login1.action";
		// 传入的参数 封装在map里面
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "1");
		map.put("password", "1");
		String charset = "utf-8";
		String result = MyClient1.httpPost(urlPath, map, charset);
		System.out.println(result);
		if (result != null) {
			Map<String, Object> jsonResult = JSON.parseObject(result);
			Integer state = (Integer) jsonResult.get("state");
			System.out.println(state);
		}
	}
}