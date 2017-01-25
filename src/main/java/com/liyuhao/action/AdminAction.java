/******************************************************************
 *    
 *    Package:     com.liyuhao.action
 *
 *    Filename:    package-info.java
 *
 *    @author:     阿拉甲
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年1月24日 上午10:35:25
 *
 *    - first revision
 *
 *****************************************************************/
/**
 * 
 * 
 * @ClassName package-info
 * @author 阿拉甲
 * @Date 2017年1月24日 上午10:35:25
 * @version 1.0.0
 */
package com.liyuhao.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import com.alibaba.fastjson.JSON;
import com.liyuhao.utils.MyClient2;
import com.opensymphony.xwork2.ActionSupport;

public class AdminAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(AdminAction.class);

	private Map<String, Object> jsonResult;

	public Map<String, Object> getJsonResult() {
		return jsonResult;
	}

	/**
	 * 用户登录
	 * 
	 * @return
	 * @throws Exception 
	 */
	public String login() throws Exception {
		// 用户名
		String name = ServletActionContext.getRequest().getParameter("name");
		// 用户密码
		String password = ServletActionContext.getRequest().getParameter("password");

		log.info("liyuhao1_1:" + name);
		log.info("liyuhao1_1:" + password);

		// 第三方接口的路径
		String urlPath = "https://localhost:8443/liyuhao1/admin1/login1.action";
		// 传入的参数 封装在map里面
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		map.put("password", password);
		String charset = "utf-8";

		// status = 0 (成功),data = 返回的用户信息,message= ""
		// status =1 或2 (失败),data = "" message =具体的账号还是密码错误信息,返回其他则代表这次请求失败
		// 调用方法 发送请求
		String result = MyClient2.httpPost(urlPath, map, charset);
		log.info("result:" + result);
		if (result != null) {
			jsonResult = JSON.parseObject(result);
			Integer state = (Integer) jsonResult.get("state");
			log.info("state:" + state);
			if (state == 0) {
				// state =0 说明登录成功.
				return "success";
			}
			if (state == 2 || state == 1) {
				// 登录失败
				return "failure";
			}
		}
		return "error";

	}

}
