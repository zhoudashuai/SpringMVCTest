package cn.smbms.service.provider;

import java.util.List;

import cn.smbms.pojo.Provider;

public interface ProviderService {
	/**
	 * 增加供应商
	 * 
	 * @param provider
	 * @return
	 */
	public boolean add(Provider provider);

	/**
	 * 通过供应商名称、编码获取供应商列表-模糊查询-providerList
	 * 
	 * @param proName
	 * @return
	 */
	public List<Provider> getProviderList(String proName, String proCode,
			int currentPageNo, int pageSize);

	/**
	 * 通过proId删除Provider
	 * 
	 * @param delId
	 * @return
	 */
	public int deleteProviderById(String delId);

	/**
	 * 通过proId获取Provider
	 * 
	 * @param id
	 * @return
	 */
	public Provider getProviderById(String id);

	/**
	 * 修改用户信息
	 * 
	 * @param user
	 * @return
	 */
	public boolean modify(Provider provider);

	/**
	 * 根据条件查询供应商表记录数
	 * 
	 * @param
	 * @param
	 * @return
	 */
	public int getProviderCount(String queryProName, String queryProCode);

	public List<Provider> getProviderList();
}
