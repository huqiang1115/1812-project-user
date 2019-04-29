package com.bj.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bj.common.pojo.User;
import com.bj.common.utils.ObjectUtil;
import com.bj.common.vo.SysResult;
import com.bj.user.service.UserService;

import redis.clients.jedis.JedisCluster;

@RestController
@RequestMapping("user")
public class UserController {
	//用户注册的用户名校验
	/*请求地址	user.bj.com/user/checkUserName/{userName}
	请求方式	GET
	请求参数	路径传参的String userName
	返回数据	存在返回1不可用,不存在返回0表示可用*/
	@Autowired
	private UserService userService;
	@RequestMapping("checkUserName/{userName}")
	public Integer checkUserName(@PathVariable 
			String userName){
		return userService.checkUserName(userName);
	}
	/*
 	请求地址	user.bj.com/user/save
	请求方式	post
	请求参数	User user对象接收(对应前台的传参key=value&key=value结构)
	返回数据	新增成功失败返回1/0
	 */
	@RequestMapping("save")
	public Integer saveUser(User user){
		userService.saveUser(user);
		return 1;
	}

	//用户登录逻辑
	@RequestMapping("login")
	public String doLogin(User user){
		String ticket=userService.doLogin(user);
		return ticket;//登录成功返回值,不成功返回""
	}

	//登录的客户端会携带cookie中有的BJ_TICKET访问接口方法
	@RequestMapping("query/{ticket}")
	public String queryTicket(@PathVariable String ticket,
			String callback){
		//调用业务封装
		SysResult result=userService.queryTicket(ticket);
		try{
			String jsonData = ObjectUtil.mapper.writeValueAsString(result);
			if(callback==null){//说明不需要jsonp格式
				return jsonData;
			}else{
				return callback+"("+jsonData+")";
			}
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}

	}
}


























