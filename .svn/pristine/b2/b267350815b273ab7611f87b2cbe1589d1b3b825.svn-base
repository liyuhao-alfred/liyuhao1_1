/* 登录界面JS脚本程序 */

$(function() {
	// 为登录按钮绑定单击事件
	$('#login').click(loginAction);
	// $('#regist').click(registAction);
	// $('#password').blur(checkPassword);
	// $('#name').blur(checkName);

});

// 登录按钮的动作
function loginAction() {

	console.log('login click!');
	// 收集用户名和密码数据
	var name = $('#name').val();
	var password = $('#password').val();
	// 验证用户名和密码
	var pass = checkName() + checkPassword();
	if (pass != 2) {
		// return;
	}
	var paramter = {
		'name' : name,
		'password' : password
	};
	// 发送Ajax请求
	$.ajax({
		url : 'user/login.do',
		data : paramter,
		dataType : 'json',
		type : 'POST',
		success : function(result) {
			console.log(result);
			if (result.state == 0) {
				// 登录成功进行跳转
				console.log(result.data.name);
				// 存到cookie
				setCookie("userName", result.data.name);
				location.href = 'edit.html';
				return;
			}
			if (result.state == 1) {
				console.log(result.message);
				$('#name_msg').html(result.message).show();
				return;
			}
			if (result.state == 2) {
				console.log(result.message);
				$('#pwd_msg').html(result.message).show();
			}

		},
		error : function() {
			// 服务器异常或登录失败 先做简单处理
			alert('账号或密码错误');
		}
	});
}

// 检查用户名是否规范
function checkName() {
	console.log("checkName");
	var name = $('#name').val();
	console.log(name);
	if (name.length < 5 || name.length > 16) {
		$('#name_msg').html("用户名不规范").show();
		return false;
	}
	$('#name_msg').hide();
	return true;
}
// 检查密码长度
function checkPassword() {
	console.log("checkPassword");
	var pwd = $('#password').val();
	console.log(pwd);
	if (pwd.length < 5 || pwd.length > 16) {
		$('#pwd_msg').html("密码错误").show();
		return false;
	}
	$('#pwd_msg').hide();
	return true;
}
