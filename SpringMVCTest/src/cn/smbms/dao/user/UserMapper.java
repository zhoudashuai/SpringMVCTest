package cn.smbms.dao.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.smbms.pojo.User;

public interface UserMapper {

	// spring mybatis���ϲ���
	public List<User> getUserList1(User user);

	// ��¼
	public User login(@Param("userCode") String userCode,
			@Param("userPassword") String userPassword);

	// ��ʾ��Ϣ
	public List<User> getUserList(@Param("userName") String queryUserName,
			@Param("userRole") Integer queryUserRole,
			@Param("currentPageNo") Integer currentPageNo,
			@Param("pageSize") Integer pageSize);

	/**
	 * ����������ѯ�û����¼��
	 * 
	 * @param queryUserName
	 * @param queryUserRole
	 * @return
	 */
	public int getUserCount(@Param("userName") String queryUserName,
			@Param("userRole") Integer queryUserRole);

	/**
	 * ����û�
	 * 
	 * @param user
	 * @return
	 */
	public int add(User user);

	/**
	 * ����IDɾ��user
	 * 
	 * @param delId
	 * @return
	 */
	public int deleteUserById(@Param("delId") Integer delId);

	/**
	 * 
	 * ����userCode�����û�
	 */
	public User getuserCode(@Param("userCode") String userCode);

	/**
	 * �޸��û���Ϣ
	 * 
	 * @param user
	 * @return
	 */
	public int modify(User user);

	/**
	 * ����ID����user
	 * 
	 * @param id
	 * @return
	 */
	public User getUserById(@Param("id") String id);
}
