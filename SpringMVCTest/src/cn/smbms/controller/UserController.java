package cn.smbms.controller;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {
	private Logger logger = Logger.getLogger(UserController.class);

	@Resource
	private UserService userService;
	@Resource
	private RoleService roleService;

	@RequestMapping("/login")
	public String login() {
		logger.debug("UserController welcome smbms===========");
		return "login";
	}

	@RequestMapping(value = "/dologin", method = RequestMethod.POST)
	public String doLogin(@RequestParam String userCode,
			@RequestParam String userPassword, HttpServletRequest request,
			HttpSession session) {
		logger.debug("dologin==================");
		User user = userService.login(userCode, userPassword);
		if (null != user) {
			session.setAttribute(Constants.USER_SESSION, user);
			// ת��
			return "redirect:/user/main";
			// response.sendRedirect
		} else {
			// ҳ����ת(login.jsp)������ʾ��Ϣ--ת��
			request.setAttribute("error", "�û��������벻��ȷ");
			return "login";
		}
	}

	@RequestMapping("/main")
	public String main(HttpSession session) {
		if (session.getAttribute(Constants.USER_SESSION) == null) {
			return "redirect:/user/login";
		}
		return "frame";
	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		// ���session
		session.removeAttribute(Constants.USER_SESSION);
		return "login";
	}

	// ʹ��ע���ע�쳣
	@RequestMapping(value = "/exlogin", method = RequestMethod.GET)
	public String exLogin(@RequestParam String userCode,
			@RequestParam String userPassword) {
		logger.debug("exLogin=================");
		User user = userService.login(userCode, userPassword);
		if (null == user) {
			throw new RuntimeException("�û����������벻��ȷ��");
		}
		return "redirect:/user/main";
	}

	/*
	 * @ExceptionHandler(value = { RuntimeException.class }) public String
	 * handlerException(RuntimeException e, HttpServletRequest req) {
	 * req.setAttribute("e", e); return "error"; }
	 */
	@RequestMapping("/userlist")
	public String getUserList(
			HttpSession session,
			Model model,
			@RequestParam(value = "queryname", required = false) String queryUserName,
			@RequestParam(value = "queryUserRole", required = false) String queryUserRole,
			@RequestParam(value = "pageIndex", required = false) String pageIndex) {
		logger.info("getUserList--->>queryUserName" + queryUserName);
		logger.info("getUserList--->>queryUserRole" + queryUserRole);
		logger.info("getUserList--->>pageIndex" + pageIndex);
		int _queryUserRole = 0;
		List<User> userList = null;
		List<Role> roleList = null;
		// ����ҳ������
		int pageSize = Constants.pageSize;
		// ��ǰҳ��
		int currentPageNo = 1;

		if (queryUserName == null) {
			queryUserName = "";
		}
		if (queryUserRole != null && !queryUserRole.equals("")) {
			_queryUserRole = Integer.parseInt(queryUserRole);
		}
		if (pageIndex != null) {
			try {
				currentPageNo = Integer.valueOf(pageIndex);
			} catch (Exception e) {
				return "redirect:/user/syserror";
			}
		}
		// ������(��)
		int totalCount = userService
				.getUserCount(queryUserName, _queryUserRole);
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
		roleList = roleService.getRoleList();
		userList = userService.getUserList(queryUserName, _queryUserRole,
				currentPageNo, pageSize);
		model.addAttribute("userList", userList);
		session.setAttribute("roleList", roleList);
		model.addAttribute("queryUserName", queryUserName);
		model.addAttribute("queryUserRole", queryUserRole);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);

		return "userlist";
	}

	@RequestMapping("/syserror")
	public String sysError() {
		return "syserror";
	}

	// ��ת��useradd����
	@RequestMapping(value = "/useradd", method = RequestMethod.GET)
	public String addUser(@ModelAttribute("user") User user, Model model) {// �ڶ�����ע�����
		// ��һ��User��������
		// model.addAttribute("user",user);
		/*
		 * List<Role> roleList = null; roleList = roleService.getRoleList();
		 * model.addAttribute(roleList);
		 */
		return "useradd";
	}

	// ���ļ��ϴ�
	@RequestMapping(value = "/addsave", method = RequestMethod.POST)
	public String addUserSave(
			/* @Valid */User user, /* BindingResult bindingResult, */
			HttpSession session,
			@RequestParam(value = "attachs", required = false) MultipartFile[] attachs,
			HttpServletRequest request) { // JSR 303��֤��ǩ�İ󶨲���

		/*
		 * if (bindingResult.hasErrors()) {
		 * logger.debug("add user validated has error============="); return
		 * "user/useradd"; }
		 */

		String idPicPath = null;
		String workPicPath = null;
		String errorInfo = null;
		boolean flag = true;
		String path = request.getSession().getServletContext()
				.getRealPath("statics" + File.separator + "uploadfiles");
		logger.debug("uploadFile  path=========" + path);
		for (int i = 0; i < attachs.length; i++) {
			MultipartFile attach = attachs[i];
			if (!attach.isEmpty()) {
				if (i == 0) {
					errorInfo = "uploadFileError";
				} else if (i == 1) {
					errorInfo = "uploadWpError";
				}
				String oldFileName = attach.getOriginalFilename();// ԭ�ļ���
				logger.debug("uploadFile oldFileName=========" + oldFileName);
				String prefix = FilenameUtils.getExtension(oldFileName);// ԭ�ļ�����׺
				logger.debug("uploadFile prefix===========" + prefix);
				int filesize = 5000000;
				logger.debug("uploadFile size==========" + attach.getSize());
				if (attach.getSize() > filesize) {// �ϴ����ó���ָ����С���ļ�
					request.setAttribute(errorInfo, "* �ϴ���С���ó���5000kb");
					flag = false;
				} else if (prefix.equalsIgnoreCase("jpg")
						|| prefix.equalsIgnoreCase("png")
						|| prefix.equalsIgnoreCase("jpeg")
						|| prefix.equalsIgnoreCase("pneg")) { // �ϴ�ͼƬ��ʽ����ȷ
					String fileName = System.currentTimeMillis()
							+ RandomUtils.nextInt(1000000) + "_Personal.jpg";
					logger.debug("new fileName========" + attach.getName());
					File targetFile = new File(path, fileName);
					if (!targetFile.exists()) {
						targetFile.mkdirs();
					} // ����
					try {
						attach.transferTo(targetFile);
					} catch (Exception e) {
						e.printStackTrace();
						request.setAttribute(errorInfo, "*�ϴ�ʧ�ܣ�");
						flag = false;
					}
					if (i == 0) {
						// File.separator�ָ���
						idPicPath = File.separator + "statics" + File.separator
								+ "uploadfiles" + File.separator + fileName;
					} else if (i == 1) {
						workPicPath = File.separator + "statics"
								+ File.separator + "uploadfiles"
								+ File.separator + fileName;
					}
					logger.debug("idPicPath" + idPicPath);
					logger.debug("workPicPath" + workPicPath);
				} else {
					request.setAttribute(errorInfo, "*�ϴ�ͼƬ����ȷ");
					flag = false;
				}
			}
		}
		if (flag) {
			user.setCreatedBy(((User) session
					.getAttribute(Constants.USER_SESSION)).getId());
			user.setCreationDate(new Date());
			user.setIdPicPath(idPicPath);
			user.setWorkPicPath(workPicPath);
			if (userService.add(user)) {
				return "redirect:/user/userlist";
			}
		}
		return "useradd";
	}

	// ��ӱ��� ���ļ��ϴ�
	/*
	 * @RequestMapping(value = "/addsave", method = RequestMethod.POST) public
	 * String addUserSave(
	 * 
	 * @Valid User user, BindingResult bindingResult,
	 * 
	 * HttpSession session,
	 * 
	 * @RequestParam(value = "a_idPicPath", required = false) MultipartFile
	 * attach, HttpServletRequest request) { // jsr 303��֤��ǩ�İ󶨲���
	 * 
	 * if (bindingResult.hasErrors()) {
	 * logger.debug("add user validated has error============="); return
	 * "user/useradd"; }
	 * 
	 * String idPicPath = null; if (!attach.isEmpty()) { String path =
	 * request.getSession().getServletContext() .getRealPath("statics" +
	 * File.separator + "uploadfiles"); logger.debug("uploadFile  path========="
	 * + path); String oldFileName = attach.getOriginalFilename();// ԭ�ļ���
	 * logger.debug("uploadFile oldFileName=========" + oldFileName); String
	 * prefix = FilenameUtils.getExtension(oldFileName);// ԭ�ļ�����׺
	 * logger.debug("uploadFile prefix===========" + prefix); int filesize =
	 * 5000000; logger.debug("uploadFile size==========" + attach.getSize()); if
	 * (attach.getSize() > filesize) {// �ϴ����ó���ָ����С���ļ�
	 * request.setAttribute("uploadFileError", "* �ϴ���С���ó���5000kb"); return
	 * "useradd"; } else if (prefix.equalsIgnoreCase("jpg") ||
	 * prefix.equalsIgnoreCase("png") || prefix.equalsIgnoreCase("jpeg") ||
	 * prefix.equalsIgnoreCase("pneg")) { // �ϴ�ͼƬ��ʽ����ȷ String fileName =
	 * System.currentTimeMillis() + RandomUtils.nextInt(1000000) +
	 * "_Personal.jpg"; logger.debug("new fileName========" + attach.getName());
	 * File targetFile = new File(path, fileName); if (!targetFile.exists()) {
	 * targetFile.mkdirs(); } // ���� try { attach.transferTo(targetFile); } catch
	 * (Exception e) { e.printStackTrace();
	 * request.setAttribute("uploadFileError", "*�ϴ�ʧ�ܣ�"); return "useradd"; } //
	 * File.separator�ָ��� idPicPath = path + File.separator + fileName; } else {
	 * request.setAttribute("uploadFileError", "*�ϴ�ͼƬ����ȷ"); return "useradd"; }
	 * }
	 * 
	 * user.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION))
	 * .getId()); user.setCreationDate(new Date());
	 * user.setIdPicPath(idPicPath); if (userService.add(user)) { return
	 * "redirect:/user/userlist"; } return "useradd"; }
	 */

	// ����userid��ת�޸�ҳ��
	@RequestMapping(value = "/usermodify", method = RequestMethod.GET)
	public String getUserById(@RequestParam String uid, Model model) {
		logger.debug("getUserById uid=============" + uid);
		/*
		 * List<Role> roleList = null; roleList = roleService.getRoleList();
		 * model.addAttribute(roleList);
		 */
		User user = userService.getUserById(uid);
		model.addAttribute(user);
		return "usermodify";
	}

	// �޸�ҳ�汣��
	@RequestMapping(value = "/usermodifysave", method = RequestMethod.POST)
	public String modifyUserSave(User user, HttpSession session) {
		logger.debug("==================" + user);
		user.setModifyBy(((User) session.getAttribute(Constants.USER_SESSION))
				.getId());
		user.setModifyDate(new Date());
		if (userService.modify(user)) {
			return "redirect:/user/userlist";
		}
		return "usermodify";
	}

	// ��Rest���鿴
	/*
	 * @RequestMapping(value = "/view/{id}", method = RequestMethod.GET) public
	 * String view(@PathVariable String id, Model model) {
	 * logger.debug("view id================" + id); User user =
	 * userService.getUserById(id); model.addAttribute(user);
	 * 
	 * return "userview"; }
	 */

	// �ж�userCode�Ƿ��ظ�
	@RequestMapping("/ucexist")
	@ResponseBody
	public Object userCodeIsExit(@RequestParam String userCode) {
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if (StringUtils.isNullOrEmpty(userCode)) {
			resultMap.put("userCode", "exist");
		} else {
			User user = userService.selectUserCodeExist(userCode);
			if (null != user) {
				resultMap.put("userCode", "exist");
			} else {
				resultMap.put("userCode", "noexist");
			}
		}
		return JSONArray.toJSON(resultMap);
	}

	// �첽�鿴 produces�����������
	@RequestMapping(value = "/view", method = RequestMethod.GET)
	@ResponseBody
	public User view(String id) {
		User user = new User();
		try {
			user = userService.getUserById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	@RequestMapping(value = "/deluser", method = RequestMethod.GET)
	@ResponseBody
	public HashMap<String, String> deluser(@RequestParam String uid) {
		HashMap<String, String> map = new HashMap<String, String>();

		if (StringUtils.isNullOrEmpty(uid)) {
			map.put("delResult", "notexist");
		} else if (userService.deleteUserById(Integer.parseInt(uid))) {
			map.put("delResult", "true");
		} else {
			map.put("delResult", "false");
		}
		return map;
	}

	// ��ת�޸Ľ���
	@RequestMapping(value = "/pwdmodify", method = RequestMethod.GET)
	public String getPassword(@ModelAttribute("user") User user, Model model,
			HttpSession session) {
		user.setId(((User) session.getAttribute(Constants.USER_SESSION))
				.getId());
		model.addAttribute(user);
		return "pwdmodify";
	}

	// �޸ĺ󱣴�
	@RequestMapping(value = "/pwdSave", method = RequestMethod.POST)
	public String pwdSave(@RequestParam Integer id,
			@RequestParam String newpassword) {
		if (userService.updatePwd(id, newpassword)) {
			return "redirect:/user/login";
		}
		return "pwdSave";
	}

	// ��������֤
	@RequestMapping(value = "/pwdmodif")
	@ResponseBody
	public HashMap<String, String> getPwdByUserId(String oldpassword,
			HttpSession session) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (((User) session.getAttribute(Constants.USER_SESSION))
				.getUserPassword().equals(oldpassword)) {
			map.put("result", "true");
		} else if (((User) (session.getAttribute(Constants.USER_SESSION)))
				.getUserPassword() == null) {
			map.put("result", "sessionerror");
		} else if (StringUtils.isNullOrEmpty(oldpassword)) {
			map.put("result", "error");
		} else {
			map.put("result", "false");
		}
		return map;
	}
}
