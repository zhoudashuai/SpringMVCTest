package cn.smbms.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Provider;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/bill")
public class BillController {
	private Logger logger = Logger.getLogger(BillController.class);
	@Resource
	private BillService billService;

	@Resource
	private ProviderService proService;

	@RequestMapping("/billlist")
	public String getUserList(
			HttpSession session,
			Model model,
			@RequestParam(value = "queryProductName", required = false) String queryProductName,
			@RequestParam(value = "queryProviderId", required = false) String queryProviderId,
			@RequestParam(value = "pageIndex", required = false) String pageIndex) {
		logger.info("getUserList--->>queryProductName" + queryProductName);
		logger.info("getUserList--->>queryProviderId" + queryProviderId);
		logger.info("getUserList--->>pageIndex" + pageIndex);
		int _queryProviderId = 0;
		List<Bill> billList = null;
		List<Provider> providerList = null;
		// 设置页面容量
		int pageSize = Constants.pageSize;
		// 当前页码
		int currentPageNo = 1;

		if (queryProductName == null) {
			queryProductName = "";
		}
		if (queryProviderId != null && !queryProviderId.equals("")) {
			_queryProviderId = Integer.parseInt(queryProviderId);
		}
		if (pageIndex != null) {
			try {
				currentPageNo = Integer.valueOf(pageIndex);
			} catch (Exception e) {
				return "redirect:/user/syserror";
			}
		}
		// 总数量(表)
		int totalCount = billService.getBillCountByProviderId(queryProductName,
				_queryProviderId);
		// 总页数
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
		providerList = proService.getProviderList();
		billList = billService.getBillList(queryProductName, _queryProviderId,
				currentPageNo, pageSize);
		model.addAttribute("billList", billList);
		session.setAttribute("providerList", providerList);
		model.addAttribute("queryProductName", queryProductName);
		model.addAttribute("queryProviderId", _queryProviderId);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);

		return "billlist";
	}

	// 跳转到订单添加页面
	@RequestMapping(value = "/billadd", method = RequestMethod.GET)
	public String billadd(@ModelAttribute("bill") Bill bill, Model model) {
		return "billadd";
	}
}
