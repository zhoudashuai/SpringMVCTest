package cn.smbms.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/pro")
public class ProController {
	private Logger logger = Logger.getLogger(ProController.class);

	@Resource
	private ProviderService proService;

	@RequestMapping("/providerlist")
	public String getProList(
			Model model,
			@RequestParam(value = "queryProName", required = false) String queryProName,
			@RequestParam(value = "queryProCode", required = false) String queryProCode,
			@RequestParam(value = "pageIndex", required = false) String pageIndex) {
		logger.info("getUserList--->>queryProName" + queryProName);
		logger.info("getUserList--->>queryProCode" + queryProCode);
		logger.info("getUserList--->>pageIndex" + pageIndex);
		List<Provider> providerList = null;
		// ����ҳ������
		int pageSize = Constants.pageSize;
		// ��ǰҳ��
		int currentPageNo = 1;

		if (queryProName == null) {
			queryProName = "";
		}
		if (pageIndex != null) {
			try {
				currentPageNo = Integer.valueOf(pageIndex);
			} catch (Exception e) {
				return "redirect:/user/syserror";
			}
		}
		// ������(��)
		int totalCount = proService
				.getProviderCount(queryProName, queryProCode);

		// ��ҳ��
		PageSupport pages = new PageSupport();
		pages.setCurrentPageNo(currentPageNo);
		pages.setPageSize(pageSize);
		pages.setTotalCount(totalCount);
		int totalPageCount = pages.getTotalPageCount();
		if (currentPageNo < 1) {
			currentPageNo = 1;
		} else if (currentPageNo > totalPageCount) {
			currentPageNo = totalPageCount;
		}
		providerList = proService.getProviderList(queryProName, queryProCode,
				currentPageNo, pageSize);
		model.addAttribute("providerList", providerList);
		model.addAttribute("queryProName", queryProName);
		model.addAttribute("queryProCode", queryProCode);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);
		return "providerlist";
	}

	// ��ת����Ӧ�̵����ҳ��
	@RequestMapping(value = "/provideradd", method = RequestMethod.GET)
	public String addprovider(@ModelAttribute("provider") Provider provider,
			Model model) {
		return "provideradd";
	}

	// ���湩Ӧ�����
	@RequestMapping(value = "/providersave", method = RequestMethod.POST)
	public String addprovidersave(Provider provider, HttpSession session) {
		provider.setCreatedBy(((User) session
				.getAttribute(Constants.USER_SESSION)).getId());
		provider.setCreationDate(new Date());
		if (proService.add(provider)) {
			return "redirect:/pro/providerlist";
		}
		return "provideradd";
	}

	// ��ת����Ӧ���޸Ľ���
	@RequestMapping(value = "/providermodif", method = RequestMethod.GET)
	public String getProviderById(@RequestParam String proid, Model model) {
		Provider provider = proService.getProviderById(proid);
		model.addAttribute(provider);
		return "providermodify";
	}

	// ��Ӧ���޸ı���
	@RequestMapping(value = "/providermodifysave", method = RequestMethod.POST)
	public String providermodifysave(Provider provider, HttpSession session) {
		provider.setModifyBy(((User) session
				.getAttribute(Constants.USER_SESSION)).getId());
		provider.setModifyDate(new Date());
		if (proService.modify(provider)) {
			return "redirect:/pro/providerlist";
		}
		return "providermodify";
	}

	// �鿴
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String view(@PathVariable String id, Model model) {
		Provider provider = proService.getProviderById(id);
		model.addAttribute(provider);
		return "providerview";
	}

}
