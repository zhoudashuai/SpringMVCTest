package cn.smbms.dao.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.smbms.pojo.User;

public interface UserMapper {

	// spring mybatis整合测试
	public List<User> getUserList1(User user);

	// 登录
	public User login(@Param("userCode") String userCode,
			@Param("userPassword") String userPassword);

	// 显示信息
	public List<User> getUserList(@Param("userName") String queryUserName,
			@Param("userRole") Integer queryUserRole,
			@Param("currentPageNo") Integer currentPageNo,
			@Param("pageSize") Integer pageSize);

	/**
	 * 根据条件查询用户表记录数
	 * 
	 * @param queryUserName
	 * @param queryUserRole
	 * @return
	 */
	public int getUserCount(@Param("userName") String queryUserName,
			@Param("userRole") Integer queryUserRole);

	/**
	 * 添加用户
	 * 
	 * @param user
	 * @return
	 */
	public int add(User user);

	/**
	 * 根据ID删除user
	 * 
	 * @param delId
	 * @return
	 */
	public int deleteUserById(@Param("delId") Integer delId);

	/**
	 * 
	 * 根据userCode查找用户
	 */
	public User getuserCode(@Param("userCode") String userCode);

	/**
	 * 修改用户信息
	 * 
	 * @param user
	 * @return
	 */
	public int modify(User user);

	/**
	 * 根据ID查找user
	 * 
	 * @param id
	 * @return
	 */
	public User getUserById(@Param("id") String id);
}
