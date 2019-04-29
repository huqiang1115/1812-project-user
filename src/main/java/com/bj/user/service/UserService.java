package com.bj.user.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bj.common.pojo.User;
import com.bj.common.utils.MD5Util;
import com.bj.common.utils.ObjectUtil;
import com.bj.common.utils.UUIDUtil;
import com.bj.common.vo.SysResult;
import com.bj.user.mapper.UserMapper;

import redis.clients.jedis.JedisCluster;

@Service
public class UserService {
	@Autowired
	private UserMapper userMapper;
	public Integer checkUserName(String userName) {
		
		return userMapper.checkExists(userName);
	}
	public void saveUser(User user) {
		//补充一个uuid 的userId
		user.setUserId(UUIDUtil.getUUID());
		//对password加密
		String password=MD5Util.md5(user.getUserPassword());
		user.setUserPassword(password);
		userMapper.doRegist(user);
	}
	@Autowired
	private JedisCluster cluster;
	public String doLogin(User user) {
		//密码调整成加密字符串
		user.setUserPassword(MD5Util
				.md5(user.getUserPassword()));
		//调用持久层验证用户登录合法
		try{
			User existUser=userMapper.login(user);
			if(existUser!=null){//TODO
				//登录合法,实现最多一个用户登录逻辑
				//判断当前用户userId是否有数据返回
				String existsTicket = cluster.get(existUser.getUserId());
				if(StringUtils.isNotEmpty(existsTicket)){
					//说明已经有登录过的人了,删了顶替他
					cluster.del(existsTicket);
				}
				//说明登录合法,生成redis的key值
				String ticket=MD5Util
					.md5("BJ_TICKET"+existUser
							.getUserId()+System
							.currentTimeMillis());
				//记录自己的登录ticket
				cluster.set(existUser.getUserId(), ticket);
				String userJson = ObjectUtil.mapper.writeValueAsString(existUser);
				//将用户数据存储到redis集群
				cluster.set(ticket, userJson);
				//设置超时时间30分钟
				cluster.expire(ticket, 60*3);
				return ticket;
			}else{
				//说明用户名密码不合法
				return "";}
			}catch(Exception e){
				e.printStackTrace();
				return "";
		}
	}
	public SysResult queryTicket(String ticket) {
		//判断超时
		Long lastedTime = cluster.ttl(ticket);
		//判断超时0<latedTime<60*10
		if(lastedTime>0&&lastedTime<60*10){
		//续租操作,延长5分钟,极限逻辑
		//redis集群中对于已经删除的数据做超时没有返回结果的0
			cluster.expire(ticket, (int)(lastedTime+60*5));
		}
		String userJson = cluster.get(ticket);
		
		return SysResult.build(200, "ok", userJson);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
