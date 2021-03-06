package com.inesv.digiccy.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.inesv.digiccy.api.command.CreateUserCommand;
import com.inesv.digiccy.api.command.LoginLogCommand;
import com.inesv.digiccy.api.command.UserCommand;
import com.inesv.digiccy.common.ResponseCode;
import com.inesv.digiccy.dto.InesvUserDto;
import com.inesv.digiccy.dto.InsevSetInfoDto;
import com.inesv.digiccy.dto.LoginLogDto;
import com.inesv.digiccy.query.QuerySetInfo;
import com.inesv.digiccy.query.QueryUserInfo;
import com.inesv.digiccy.sms.SendMsgUtil;
import com.inesv.digiccy.util.MD5;

@Controller
@RequestMapping(value = "/user")
public class UserController {
	@Autowired
	private CommandGateway commandGateway;

	@Autowired
	private QueryUserInfo queryUserInfo;

	/*@Autowired
	private QuerySetInfo querySetInfo;*/

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	SendMsgUtil sendMsgUtil;

	@RequestMapping(value = "wel")
	public ModelAndView wel() {
		return new ModelAndView("wel");
	}

	@RequestMapping(value = "loginPage")
	public @ResponseBody
	Object loginPage() {
		return null;
	}

	/*
	 * @AutoDocMethod(author = DeveloperType.HUANGWEIHANG, createTime =
	 * "2016-12-1", cver = VersionType.V100, name = "测试登录接口", description =
	 * "测试登录接口", model = ModelType.LOGIN, dtoClazz = BaseRes.class, reqParams =
	 * {"tid","uid"},//有参才需要加的 progress = ProgressType.FINISHED)
	 * 
	 * @AutoDocMethodParam(note = "测试id@@用户id", name = "tid@@uid")
	 */
	@RequestMapping(value = "/testindex")
	public ModelAndView testindex(@RequestParam String tid,
			@RequestParam String uid, HttpServletRequest request) {
		// System.out.println(sendMsgUtil.sendMsg("13580127947",
		// 1)+":121211212");
		redisTemplate.opsForValue().set("tid", tid);
		redisTemplate.opsForValue().set("uid", uid);
		String test = redisTemplate.opsForValue().get("uid").toString();
		// System.out.println(test);
		// System.out.println(SecurityContextHolder.getContext().getAuthentication().getCredentials());
		return new ModelAndView("/index");
	}

	/*@RequestMapping(value = "login")
	public @ResponseBody Map<String, Object> login(HttpSession session, HttpServletRequest request,
			HttpServletResponse resp, @RequestParam String username,@RequestParam String password, @RequestParam String ip) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (ip == null || ip.equals("")) {
			map.put("code", ResponseCode.FAIL);
			map.put("msg", "IP地址不能为空");
			return map;
		}
		InesvUserDto user = queryUserInfo.loadUser(username, password);
		if (user == null) {
			map.put("code", ResponseCode.FAIL);
			map.put("msg", "用户名或密码错误！");
		}
		List<InsevSetInfoDto> setInfos = querySetInfo.getSetInfo();
			if (setInfos != null && setInfos.size() > 0) {// 如果没有获取到设置信息则不做校验
				int limitNumbers = setInfos.get(0).getOpertion_number();
				int limitTime = setInfos.get(0).getOpertion_time();
				List<LoginLogDto> info = queryUserInfo
						.getLoginLogInfoLimitTime(user.getUser_no(), limitTime);
				if (info != null && info.size() > 0) {// 限制时间内有
					if (info.get(0).getState() == 2) {// 如果已经被锁上
						System.out.println("用户已经被锁定，请联系管理员解锁");
						map.put("code", ResponseCode.FAIL);
						map.put("msg", ResponseCode.FAIL_DESC);
						return map;
					}
					if (info.size() >= limitNumbers) {// 检测是否超过限制
						// 锁上该用户
						System.out.println("用户被锁定，请联系管理员解锁");
						LoginLogCommand loginLogCommand = new LoginLogCommand(
								user.getUser_no(), 1, "用户被锁定", ip, "", 2,
								new Date());
						commandGateway.send(loginLogCommand);
						map.put("code", ResponseCode.FAIL);
						map.put("msg", ResponseCode.FAIL_DESC);
						return map;
					}
				} else {// 如果该时间段内没日志则查询所有的登录日志看看是否被锁定
					List<LoginLogDto> allInfo = queryUserInfo
							.getLoginLogInfo(user.getUser_no());
					if (allInfo != null && allInfo.size() > 0) {
						if (allInfo.get(0).getState() == 2) {
							System.out.println("用户已经被锁定，请联系管理员解锁");
							map.put("code", ResponseCode.FAIL);
							map.put("msg", ResponseCode.FAIL_DESC);
							return map;
						}
					}
				}
			}
			Long tokenStr = user.getId() + new Date().getTime();
			String token = new MD5().getMD5(String.valueOf(tokenStr));
			redisTemplate.opsForValue().set(token, token, 7, TimeUnit.DAYS);
			map.put("code", ResponseCode.SUCCESS);
			map.put("msg", ResponseCode.SUCCESS_DESC);
			user.setPassword("******");
			user.setDeal_pwd("******");
			map.put("loginUserInfo", user);
			map.put("token", token);
			LoginLogCommand loginLogCommand = new LoginLogCommand(
					user.getUser_no(), 1, "通过用户名登录", ip, "", 1, new Date());
			commandGateway.send(loginLogCommand);
		return map;
	}*/
	@RequestMapping(value = "login")
    public @ResponseBody Map<String,Object> login(HttpSession session,HttpServletRequest request,HttpServletResponse resp,@RequestParam String username,@RequestParam String password,@RequestParam String ip){
      Map<String,Object> map = new HashMap<String,Object>();
        InesvUserDto user = queryUserInfo.loadUser(username,password);
        if(user == null) {
        	map.put("code", ResponseCode.FAIL);
            map.put("desc","用户名或密码错误！");
            return map;
        }
        if(user.getState() != 1) {
        	map.put("code", ResponseCode.FAIL);
            map.put("desc","该用户未启动，请联系管理人员！");
            return map;
        }
        if(ip==null || ip.equals("")){
        	 map.put("code", ResponseCode.FAIL);
             map.put("desc","IP地址不能为空");
             return map;
        }
        if(user!=null){
          /*String tokens=request.getParameter("token");
          redisTemplate.delete(tokens);*/
          Long tokenStr = user.getId()+new Date().getTime();
          String token = new MD5().getMD5(String.valueOf(tokenStr));
          redisTemplate.opsForValue().set(token,token, 7, TimeUnit.DAYS);
          map.put("code", ResponseCode.SUCCESS);
          map.put("msg", ResponseCode.SUCCESS_DESC);
          user.setPassword("******");
          user.setDeal_pwd("******");
          map.put("loginUserInfo", user);
          map.put("token", token);
          LoginLogCommand loginLogCommand = new LoginLogCommand(user.getUser_no(),1,"通过用户名登录",ip,"",1,new Date());
          commandGateway.send(loginLogCommand);
        }else{
          map.put("code", ResponseCode.FAIL);
          map.put("desc", ResponseCode.FAIL_DESC);
        }
        return map;
    }
	

	@RequestMapping(value = "logout")
	public @ResponseBody
	Map<String, Object> logout(HttpSession session, HttpServletRequest request,
			HttpServletResponse resp, String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			redisTemplate.delete(token);
			map.put("code", ResponseCode.SUCCESS);
			map.put("msg", ResponseCode.SUCCESS_DESC);
		} catch (Exception e) {
			e.printStackTrace();
			map.put("code", ResponseCode.FAIL_LOGOUT);
			map.put("msg", ResponseCode.FAIL_LOGOUT_DESC);
		}
		return map;
	}

	@RequestMapping(value = "getLoginLogInfo", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> getLoginLogInfo(Integer userNo) {
		Map<String, Object> map = new HashMap<>();
		List<LoginLogDto> list = queryUserInfo.getLoginLogInfo(userNo);
		if (list == null || list.size() == 0) {
			map.put("code", ResponseCode.FAIL_USER_IP_LOG);
			map.put("desc", ResponseCode.FAIL_USER_IP_LOG_DESC);
		} else {
			map.put("data", list);
			map.put("code", ResponseCode.SUCCESS);
			map.put("desc", ResponseCode.SUCCESS_DESC);
		}
		return map;
	}

	@RequestMapping(value = "create", method = RequestMethod.POST)
	public void create(String username, String password) {
		CreateUserCommand command = new CreateUserCommand(2L, username,
				password);
		commandGateway.send(command);
	}

	/*
	 * 测试
	 */
	@RequestMapping(value = "getUsers")
	@ResponseBody
	public Map<String, Object> getUsers() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<InesvUserDto> user = queryUserInfo.getAllUsers();
		resultMap.put("data", user);
		return resultMap;
	}

	/*
	 * 测试
	 */
	@RequestMapping(value = "updateUsers")
	@ResponseBody
	public Map<String, Object> updateUsers(Long id, int user_no) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		UserCommand UserCommand = new UserCommand(id, "123456789", user_no,
				"1", "1", "1", "1", "1", "1", "1", 0, "updateUsers");
		commandGateway.send(UserCommand);
		resultMap.put("data", UserCommand);
		return resultMap;
	}

	/*
	 * 测试
	 */
	@RequestMapping(value = "addUsers")
	@ResponseBody
	public Map<String, Object> addUsers(InesvUserDto userDto) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		UserCommand userCommand = new UserCommand(userDto.getId(), "123456789",
				userDto.getUser_no(), "1", "1", "1", "1", "1", "1", "1", 0,
				"addUsers");
		commandGateway.send(userCommand);
		resultMap.put("data", userCommand);
		return resultMap;
	}

	/*
	 * 测试
	 */
	@RequestMapping(value = "deleteUsers")
	public @ResponseBody
	Map<String, Object> deleteUsers(InesvUserDto userDto) {
		Map<String, Object> resultMap = new HashMap<>();
		UserCommand userCommand = new UserCommand(userDto.getUser_no(),
				"deleteUsers");
		commandGateway.send(userCommand);
		resultMap.put("data", userCommand);
		return resultMap;
	}

	public static void main(String[] args) {
		System.out.println(new Date().getTime());
	}

}
