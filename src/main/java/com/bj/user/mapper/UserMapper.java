package com.bj.user.mapper;

import com.bj.common.pojo.User;

public interface UserMapper {

	int checkExists(String userName);

	void doRegist(User user);

	User login(User user);

}
