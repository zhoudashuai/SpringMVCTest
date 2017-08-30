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
			// 转发
			return "redirect:/user/main";
			// response.sendRedirect
		} else {
			// 页面跳转(login.jsp)带出提示信息--转发
			request.setAttribute("error", "用户名或密码不正确");
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
		// 清除session
		session.removeAttribute(Constants.USER_SESSION);
		return "login";
	}

	// 使用注解标注异常
	@RequestMapping(value = "/exlogin", method = RequestMethod.GET)
	public String exLogin(@RequestParam String userCode,
			@RequestParam String userPassword) {
		logger.debug("exLogin=================");
		User user = userService.login(userCode, userPassword);
		if (null == user) {
			throw new RuntimeException("用户名或者密码不正确！");
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
		// 设置页面容量
		int pageSize = Constants.pageSize;
		// 当前页码
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
		// 总数量(表)
		int totalCount = userService
				.getUserCount(queryUserName, _queryUserRole);
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

	// 跳转到useradd界面
	@RequestMapping(value = "/useradd", method = RequestMethod.GET)
	public String addUser(@ModelAttribute("user") User user, Model model) {// 第二种用注解入参
		// 第一种User对象的入参
		// model.addAttribute("user",user);
		/*
		 * List<Role> roleList = null; roleList = roleService.getRoleList();
		 * model.addAttribute(roleList);
		 */
		return "useradd";
	}

	// 多文件上传
	@RequestMapping(value = "/addsave", method = RequestMethod.POST)
	public String addUserSave(
			/* @Valid */User user, /* BindingResult bindingResult, */
			HttpSession session,
			@RequestParam(value = "attachs", required = false) MultipartFile[] attachs,
			HttpServletRequest request) { // JSR 303验证标签的绑定操作

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
				String oldFileName = attach.getOriginalFilename();// 原文件名
				logger.debug("uploadFile oldFileName=========" + oldFileName);
				String prefix = FilenameUtils.getExtension(oldFileName);// 原文件名后缀
				logger.debug("uploadFile prefix===========" + prefix);
				int filesize = 5000000;
				logger.debug("uploadFile size==========" + attach.getSize());
				if (attach.getSize() > filesize) {// 上传不得超过指定大小的文件
					request.setAttribute(errorInfo, "* 上传大小不得超过5000kb");
					flag = false;
				} else if (prefix.equalsIgnoreCase("jpg")
						|| prefix.equalsIgnoreCase("png")
						|| prefix.equalsIgnoreCase("jpeg")
						|| prefix.equalsIgnoreCase("pneg")) { // 上传图片格式不正确
					String fileName = System.currentTimeMillis()
							+ RandomUtils.nextInt(1000000) + "_Personal.jpg";
					logger.debug("new fileName========" + attach.getName());
					File targetFile = new File(path, fileName);
					if (!targetFile.exists()) {
						targetFile.mkdirs();
					} // 保存
					try {
						attach.transferTo(targetFile);
					} catch (Exception e) {
						e.printStackTrace();
						request.setAttribute(errorInfo, "*上传失败！");
						flag = false;
					}
					if (i == 0) {
						// File.separator分隔符
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
					request.setAttribute(errorInfo, "*上传图片不正确");
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

	// 添加保存 单文件上传
	/*
	 * @RequestMapping(value = "/addsave", method = RequestMethod.POST) public
	 * String addUserSave(
	 * 
	 * @Valid User user, BindingResult bindingResult,
	 * 
	 * HttpSession session,
	 * 
	 * @RequestParam(value = "a_idPicPath", required = false) MultipartFile
	 * attach, HttpServletRequest request) { // jsr 303验证标签的绑定操作
	 * 
	 * if (bindingResult.hasErrors()) {
	 * logger.debug("add user validated has error============="); return
	 * "user/useradd"; }
	 * 
	 * String idPicPath = null; if (!attach.isEmpty()) { String path =
	 * request.getSession().getServletContext() .getRealPath("statics" +
	 * File.separator + "uploadfiles"); logger.debug("uploadFile  path========="
	 * + path); String oldFileName = attach.getOriginalFilename();// 原文件名
	 * logger.debug("uploadFile oldFileName=========" + oldFileName); String
	 * prefix = FilenameUtils.getExtension(oldFileName);// 原文件名后缀
	 * logger.debug("uploadFile prefix===========" + prefix); int filesize =
	 * 5000000; logger.debug("uploadFile size==========" + attach.getSize()); if
	 * (attach.getSize() > filesize) {// 上传不得超过指定大小的文件
	 * request.setAttribute("uploadFileError", "* 上传大小不得超过5000kb"); return
	 * "useradd"; } else if (prefix.equalsIgnoreCase("jpg") ||
	 * prefix.equalsIgnoreCase("png") || prefix.equalsIgnoreCase("jpeg") ||
	 * prefix.equalsIgnoreCase("pneg")) { // 上传图片格式不正确 String fileName =
	 * System.currentTimeMillis() + RandomUtils.nextInt(1000000) +
	 * "_Personal.jpg"; logger.debug("new fileName========" + attach.getName());
	 * File targetFile = new File(path, fileName); if (!targetFile.exists()) {
	 * targetFile.mkdirs(); } // 保存 try { attach.transferTo(targetFile); } catch
	 * (Exception e) { e.printStackTrace();
	 * request.setAttribute("uploadFileError", "*上传失败！"); return "useradd"; } //
	 * File.separator分隔符 idPicPath = path + File.separator + fileName; } else {
	 * request.setAttribute("uploadFileError", "*上传图片不正确"); return "useradd"; }
	 * }
	 * 
	 * user.setCreatedBy(((User) session.getAttribute(Constants.USER_SESSION))
	 * .getId()); user.setCreationDate(new Date());
	 * user.setIdPicPath(idPicPath); if (userService.add(user)) { return
	 * "redirect:/user/userlist"; } return "useradd"; }
	 */

	// 根据userid跳转修改页面
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

	// 修改页面保存
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

	// 用Rest风格查看
	/*
	 * @RequestMapping(value = "/view/{id}", method = RequestMethod.GET) public
	 * String view(@PathVariable String id, Model model) {
	 * logger.debug("view id================" + id); User user =
	 * userService.getUserById(id); model.addAttribute(user);
	 * 
	 * return "userview"; }
	 */

	// 判断userCode是否重复
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

	// 异步查看 produces解决中文乱码
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

	// 跳转修改界面
	@RequestMapping(value = "/pwdmodify", method = RequestMethod.GET)
	public String getPassword(@ModelAttribute("user") User user, Model model,
			HttpSession session) {
		user.setId(((User) session.getAttribute(Constants.USER_SESSION))
				.getId());
		model.addAttribute(user);
		return "pwdmodify";
	}

	// 修改后保存
	@RequestMapping(value = "/pwdSave", method = RequestMethod.POST)
	public String pwdSave(@RequestParam Integer id,
			@RequestParam String newpassword) {
		if (userService.updatePwd(id, newpassword)) {
			return "redirect:/user/login";
		}
		return "pwdSave";
	}

	// 旧密码验证
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
