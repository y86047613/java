package com.jeefw.controller.sys;

import com.jeefw.core.Constant;
import com.jeefw.core.JavaEEFrameworkBaseController;
import com.jeefw.model.sys.Attachment;
import com.jeefw.model.sys.Authority;
import com.jeefw.model.sys.Recharge;
import com.jeefw.model.sys.Role;
import com.jeefw.model.sys.SysUser;
import com.jeefw.service.sys.AttachmentService;
import com.jeefw.service.sys.AuthorityService;
import com.jeefw.service.sys.RechargeService;
import com.jeefw.service.sys.RoleService;
import com.jeefw.service.sys.SysUserService;
import core.support.ExtJSBaseParameter;
import core.support.JqGridPageView;
import core.support.QueryResult;
import core.util.HighPreciseComputor;
import core.util.JavaEEFrameworkUtils;
import core.util.TestPassword;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.SimpleEmail;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;

@Controller
@RequestMapping({"/sys/sysuser"})
public class SysUserController
  extends JavaEEFrameworkBaseController<SysUser>
  implements Constant
{
  private static final Log log = LogFactory.getLog(SysUserController.class);
  @Resource
  private SysUserService sysUserService;
  @Resource
  private AttachmentService attachmentService;
  @Resource
  private AuthorityService authorityService;
  @Resource
  private RoleService roleService;
  @Resource
  private RechargeService rechargeService;
  private SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  
  @RequestMapping({"/login"})
  public void login(SysUser sysUserModel, HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    Map<String, Object> result = new HashMap();
    SysUser sysUser = (SysUser)this.sysUserService.getByProerties("email", sysUserModel.getEmail());
    if ((sysUser == null) || (sysUser.getStatus().booleanValue()))
    {
      result.put("result", Integer.valueOf(-1));
      writeJSON(response, result);
      return;
    }
    if (!sysUser.getPassword().equals(new Sha256Hash(sysUserModel.getPassword()).toHex()))
    {
      result.put("result", Integer.valueOf(-2));
      writeJSON(response, result);
      return;
    }
    sysUser.setLastLoginTime(new Date());
    this.sysUserService.merge(sysUser);
    Subject subject = SecurityUtils.getSubject();
    subject.login(new UsernamePasswordToken(sysUserModel.getEmail(), sysUserModel.getPassword(), 
      sysUserModel.isRememberMe()));
    Session session = subject.getSession();
    session.setAttribute("SESSION_SYS_USER", sysUser);
    session.setAttribute("ROLE_KEY", ((Role)sysUser.getRoles().iterator().next()).getRoleKey());
    result.put("result", Integer.valueOf(1));
    writeJSON(response, result);
  }
  
  @RequestMapping({"/home"})
  public ModelAndView home(HttpServletRequest request, HttpServletResponse response)
  {
    Subject subject = SecurityUtils.getSubject();
    Session session = subject.getSession();
    if (session.getAttribute("SESSION_SYS_USER") == null) {
      return new ModelAndView();
    }
    SysUser sysUser = (SysUser)session.getAttribute("SESSION_SYS_USER");
    String globalRoleKey = ((Role)sysUser.getRoles().iterator().next()).getRoleKey();
    try
    {
      List<Authority> allMenuList = this.authorityService.queryAllMenuList(globalRoleKey);
      return new ModelAndView("back/index", "authorityList", allMenuList);
    }
    catch (Exception e)
    {
      log.error(e.toString());
    }
    return new ModelAndView();
  }
  
  public boolean isNumeric(String str)
  {
    Pattern pattern = Pattern.compile("[0-9]*");
    Matcher isNum = pattern.matcher(str);
    if (!isNum.matches()) {
      return false;
    }
    return true;
  }
  
  @RequestMapping({"/register"})
  public void register(SysUser sysUserModel, HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    Map<String, Object> result = new HashMap();
    SysUser emailSysUser = (SysUser)this.sysUserService.getByProerties("email", sysUserModel.getEmail());
    if (emailSysUser != null)
    {
      result.put("result", Integer.valueOf(-1));
      writeJSON(response, result);
      return;
    }
    SysUser sysUser = new SysUser();
    boolean bool = isNumeric(String.valueOf(sysUserModel.getParent()));
    if (bool)
    {
      SysUser tuijianUser = (SysUser)this.sysUserService.getByProerties("id", Long.valueOf(sysUserModel.getParent().intValue()));
      if (tuijianUser != null) {
        sysUser.setParent(Integer.valueOf(String.valueOf(tuijianUser.getId())));
      } else {
        sysUser.setParent(Integer.valueOf(0));
      }
    }
    else
    {
      sysUser.setParent(Integer.valueOf(0));
    }
    sysUser.setUserName(sysUserModel.getUserName());
    sysUser.setSex(sysUserModel.getSex());
    sysUser.setScore(Double.valueOf(0.0D));
    sysUser.setTiceng(Double.valueOf(0.0D));
    sysUser.setEmail(sysUserModel.getEmail());
    sysUser.setPhone(sysUserModel.getPhone());
    sysUser.setScore(Double.valueOf(0.0D));
    sysUser.setBirthday(sysUserModel.getBirthday());
    
    sysUser.setPassword(new Sha256Hash(sysUserModel.getPassword()).toHex());
    sysUser.setStatus(Boolean.valueOf(false));
    sysUser.setLastLoginTime(new Date());
    
    Set<Role> roles = new HashSet();
    Role role = (Role)this.roleService.getByProerties("roleKey", "ROLE_USER");
    roles.add(role);
    sysUser.setRoles(roles);
    
    this.sysUserService.persist(sysUser);
    
    Subject subject = SecurityUtils.getSubject();
    subject.login(new UsernamePasswordToken(sysUserModel.getEmail(), sysUserModel.getPassword()));
    Session session = subject.getSession();
    session.setAttribute("SESSION_SYS_USER", sysUser);
    session.setAttribute("ROLE_KEY", ((Role)sysUser.getRoles().iterator().next()).getRoleKey());
    
    result.put("result", Integer.valueOf(1));
    writeJSON(response, result);
  }
  
  @RequestMapping({"/sysuserprofile"})
  public ModelAndView sysuserprofile(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    SysUser sysuser = (SysUser)this.sysUserService.get(((SysUser)request.getSession().getAttribute("SESSION_SYS_USER")).getId());
    SysUser sysUserWithAvatar = this.sysUserService.getSysUserWithAvatar(sysuser);
    return new ModelAndView("back/sysuserprofile", "sysuser", sysUserWithAvatar);
  }
  
  @RequestMapping({"/logout"})
  public void logout(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    SecurityUtils.getSubject().logout();
    response.sendRedirect("/jeefw/login.jsp");
  }
  
  @RequestMapping(value={"/chongzhi"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void chongzhi(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    String email = request.getParameter("email");
    String score = request.getParameter("score");
    SysUser emailSysUser = (SysUser)this.sysUserService.getByProerties("email", email);
    
    Subject subject = SecurityUtils.getSubject();
    Session session = subject.getSession();
    String ROLE = String.valueOf(session.getAttribute("ROLE_KEY"));
    SysUser user = (SysUser)session.getAttribute("SESSION_SYS_USER");
    SysUser sysUser = (SysUser)this.sysUserService.get(user.getId());
    
    Map<String, Object> result = new HashMap();
    if (emailSysUser == null)
    {
      result.put("success", "��������������.");
    }
    else if (("ROLE_RESTRICTED_ADMIN".equals(ROLE)) || ("ROLE_ADMIN".equals(ROLE)))
    {
      double s = HighPreciseComputor.add(emailSysUser.getScore().doubleValue(), Double.valueOf(score).doubleValue());
      emailSysUser.setScore(Double.valueOf(s));
      this.sysUserService.merge(emailSysUser);
      
      Recharge recharge = new Recharge();
      recharge.setA_id(BigInteger.valueOf(sysUser.getId().longValue()));
      recharge.setA_name(sysUser.getEmail());
      recharge.setB_id(BigInteger.valueOf(Long.valueOf(emailSysUser.getId().longValue()).longValue()));
      
      recharge.setB_name(emailSysUser.getEmail());
      recharge.setCreateTime(this.dateFormater.format(new Date()));
      recharge.setScore(BigInteger.valueOf(Long.valueOf(score).longValue()));
      recharge.setType(1);
      recharge.setFunc("����������{������������������������}");
      this.rechargeService.persist(recharge);
      result.put("success", "��������");
    }
    else
    {
      result.put("success", "������������");
    }
    writeJSON(response, result);
  }
  
  @RequestMapping({"/retrievePassword"})
  public void retrievePassword(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    Map<String, Object> result = new HashMap();
    String email = request.getParameter("email");
    SysUser sysUser = (SysUser)this.sysUserService.getByProerties("email", email);
    if ((sysUser == null) || (sysUser.getStatus().booleanValue()))
    {
      result.put("result", Integer.valueOf(-1));
      writeJSON(response, result);
      return;
    }
    SimpleEmail emailUtil = new SimpleEmail();
    emailUtil.setCharset("utf-8");
    emailUtil.setHostName("smtp.163.com");
    String password = TestPassword.getPassWord();
    try
    {
      emailUtil.addTo(email, sysUser.getUserName());
      emailUtil.setAuthenticator(new DefaultAuthenticator("13612875686@163.com", "pxl70658458"));
      emailUtil.setFrom("13612875686@163.com", "��������");
      emailUtil.setSubject("����������������");
      emailUtil.setMsg("��������...��" + sysUser.getUserName() + "������������������" + password);
      emailUtil.send();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      result.put("result", Integer.valueOf(-1));
      writeJSON(response, result);
      return;
    }
    sysUser.setPassword(new Sha256Hash(password).toHex());
    this.sysUserService.merge(sysUser);
    result.put("result", Integer.valueOf(1));
    writeJSON(response, result);
  }
  
  @RequestMapping({"/resetPassword"})
  public void resetPassword(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    String password = request.getParameter("password");
    Long id = ((SysUser)request.getSession().getAttribute("SESSION_SYS_USER")).getId();
    
    this.sysUserService.updateByProperties("id", id, "password", new Sha256Hash(password).toHex());
    Map<String, Object> result = new HashMap();
    result.put("success", Boolean.valueOf(true));
    writeJSON(response, result);
  }
  
  @RequestMapping(value={"/getSysUser"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void getSysUser(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    Integer firstResult = Integer.valueOf(request.getParameter("page"));
    Integer maxResults = Integer.valueOf(request.getParameter("rows"));
    String sortedObject = request.getParameter("sidx");
    String sortedValue = request.getParameter("sord");
    String filters = request.getParameter("filters");
    SysUser sysUser = new SysUser();
    if (StringUtils.isNotBlank(filters))
    {
      JSONObject jsonObject = JSONObject.fromObject(filters);
      JSONArray jsonArray = (JSONArray)jsonObject.get("rules");
      for (int i = 0; i < jsonArray.size(); i++)
      {
        JSONObject result = (JSONObject)jsonArray.get(i);
        if ((result.getString("field").equals("email")) && (result.getString("op").equals("eq"))) {
          sysUser.set$eq_email(result.getString("data"));
        }
        if ((result.getString("field").equals("userName")) && (result.getString("op").equals("cn"))) {
          sysUser.set$like_userName(result.getString("data"));
        }
      }
      if (((String)jsonObject.get("groupOp")).equalsIgnoreCase("OR")) {
        sysUser.setFlag("OR");
      } else {
        sysUser.setFlag("AND");
      }
    }
    sysUser.setFirstResult(Integer.valueOf((firstResult.intValue() - 1) * maxResults.intValue()));
    sysUser.setMaxResults(maxResults);
    Map<String, String> sortedCondition = new HashMap();
    sortedCondition.put(sortedObject, sortedValue);
    sysUser.setSortedConditions(sortedCondition);
    QueryResult<SysUser> queryResult = this.sysUserService.doPaginationQuery(sysUser);
    JqGridPageView<SysUser> sysUserListView = new JqGridPageView();
    sysUserListView.setMaxResults(maxResults.intValue());
    List<SysUser> sysUserCnList = this.sysUserService.querySysUserCnList(queryResult.getResultList());
    sysUserListView.setRows(sysUserCnList);
    sysUserListView.setRecords(queryResult.getTotalCount().longValue());
    writeJSON(response, sysUserListView);
  }
  
  @RequestMapping(value={"/saveSysUser"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void doSave(SysUser entity, HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    ExtJSBaseParameter parameter = entity;
    SysUser sysUser = (SysUser)this.sysUserService.get(entity.getId());
    if ("edit".equals(parameter.getCmd()))
    {
      entity.setPassword(sysUser.getPassword());
      entity.setLastLoginTime(sysUser.getLastLoginTime());
      if (entity.getScore() != sysUser.getScore())
      {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        String ROLE = String.valueOf(session.getAttribute("ROLE_KEY"));
        SysUser user = (SysUser)session.getAttribute("SESSION_SYS_USER");
        if (!"ROLE_ADMIN".equals(ROLE)) {
          "ROLE_RESTRICTED_ADMIN".equals(ROLE);
        }
      }
      this.sysUserService.merge(entity);
    }
    else if ("new".equals(parameter.getCmd()))
    {
      entity.setPassword(new Sha256Hash("123456").toHex());
      this.sysUserService.persist(entity);
    }
    parameter.setSuccess(Boolean.valueOf(true));
    writeJSON(response, parameter);
  }
  
  @RequestMapping(value={"/operateSysUser"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void operateSysUser(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    String oper = request.getParameter("oper");
    String id = request.getParameter("id");
    if (oper.equals("del"))
    {
      String[] ids = id.split(",");
      deleteSysUser(request, response, (Long[])ConvertUtils.convert(ids, Long.class));
    }
    else if (oper.equals("excel"))
    {
      response.setContentType("application/msexcel;charset=UTF-8");
      try
      {
        response.addHeader("Content-Disposition", "attachment;filename=file.xls");
        OutputStream out = response.getOutputStream();
        out.write(URLDecoder.decode(request.getParameter("csvBuffer"), "UTF-8").getBytes());
        out.flush();
        out.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      Map<String, Object> result = new HashMap();
      String userName = request.getParameter("userName");
      String email = request.getParameter("email");
      String score = request.getParameter("score");
      SysUser sysUser = null;
      if (oper.equals("edit")) {
        sysUser = (SysUser)this.sysUserService.get(Long.valueOf(id));
      }
      SysUser emailSysUser = (SysUser)this.sysUserService.getByProerties("email", email);
      if ((StringUtils.isBlank(userName)) || (StringUtils.isBlank(email)))
      {
        response.setStatus(411);
        result.put("message", "����������������");
        writeJSON(response, result);
      }
      else if ((emailSysUser != null) && (oper.equals("add")))
      {
        response.setStatus(409);
        result.put("message", "������������������������");
        writeJSON(response, result);
      }
      else if ((emailSysUser != null) && (!sysUser.getEmail().equalsIgnoreCase(email)) && (oper.equals("edit")))
      {
        response.setStatus(409);
        result.put("message", "������������������������");
        writeJSON(response, result);
      }
      else if (!isBigInteger(score))
      {
        response.setStatus(409);
        result.put("message", "������������");
        writeJSON(response, result);
      }
      else
      {
        SysUser entity = new SysUser();
        entity.setUserName(userName);
        entity.setScore(Double.valueOf(Long.valueOf(score).longValue()));
        entity.setSex(Short.valueOf(request.getParameter("sexCn")));
        entity.setEmail(email);
        entity.setPhone(request.getParameter("phone"));
        if (StringUtils.isNotBlank(request.getParameter("birthday"))) {
          entity.setBirthday(dateFormat.parse(request.getParameter("birthday")));
        }
        entity.setDepartmentKey(request.getParameter("departmentValue"));
        entity.setStatusCn(request.getParameter("statusCn"));
        if (entity.getStatusCn().equals("��")) {
          entity.setStatus(Boolean.valueOf(true));
        } else {
          entity.setStatus(Boolean.valueOf(false));
        }
        Set<Role> roles = new HashSet();
        Role role = (Role)this.roleService.getByProerties("roleKey", request.getParameter("roleCn"));
        roles.add(role);
        entity.setRoles(roles);
        if (oper.equals("edit"))
        {
          entity.setId(Long.valueOf(id));
          entity.setCmd("edit");
          doSave(entity, request, response);
        }
        else if (oper.equals("add"))
        {
          entity.setCmd("new");
          doSave(entity, request, response);
        }
      }
    }
  }
  
  @RequestMapping(value={"/saveSysUserProfile"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void saveSysUserProfile(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    Long sysUserId = ((SysUser)request.getSession().getAttribute("SESSION_SYS_USER")).getId();
    SysUser sysUser = (SysUser)this.sysUserService.get(sysUserId);
    SysUser entity = new SysUser();
    entity.setId(sysUserId);
    entity.setUserName(request.getParameter("userName"));
    entity.setSex(Short.valueOf(request.getParameter("sex")));
    entity.setEmail(request.getParameter("email"));
    entity.setPhone(request.getParameter("phone"));
    if (request.getParameter("birthday") != null) {
      entity.setBirthday(dateFormat.parse(request.getParameter("birthday")));
    }
    entity.setStatus(sysUser.getStatus());
    entity.setPassword(sysUser.getPassword());
    entity.setLastLoginTime(sysUser.getLastLoginTime());
    entity.setDepartmentKey(sysUser.getDepartmentKey());
    entity.setRoles(sysUser.getRoles());
    this.sysUserService.merge(entity);
    Map<String, Object> result = new HashMap();
    result.put("success", Boolean.valueOf(true));
    writeJSON(response, result);
  }
  
  @RequestMapping({"/deleteSysUser"})
  public void deleteSysUser(HttpServletRequest request, HttpServletResponse response, @RequestParam("ids") Long[] ids)
    throws IOException
  {
    if (Arrays.asList(ids).contains(Long.valueOf("1")))
    {
      writeJSON(response, "{success:false,message:'删除项包含超级管理员，超级管理员不能删除'}");
    }
    else
    {
      boolean flag = this.sysUserService.deleteByPK(ids);
      if (flag) {
        writeJSON(response, "{success:true}");
      } else {
        writeJSON(response, "{success:false}");
      }
    }
  }
  
  @RequestMapping(value={"/updateSysUserField"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void updateSysUserField(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    Long id = Long.valueOf(request.getParameter("pk"));
    String name = request.getParameter("name");
    String value = request.getParameter("value");
    if (name.equals("userName")) {
      this.sysUserService.updateByProperties("id", id, "userName", value);
    } else if (name.equals("sex")) {
      this.sysUserService.updateByProperties("id", id, "sex", Short.valueOf(value));
    } else if (name.equals("email")) {
      this.sysUserService.updateByProperties("id", id, "email", value);
    } else if (name.equals("phone")) {
      this.sysUserService.updateByProperties("id", id, "phone", value);
    } else if (name.equals("birthday")) {
      if (value != null) {
        this.sysUserService.updateByProperties("id", id, "birthday", dateFormat.parse(value));
      }
    }
  }
  
  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  
  @RequestMapping(value={"/uploadAttachement"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  public void uploadAttachement(@RequestParam(value="avatar", required=false) MultipartFile file, HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    RequestContext requestContext = new RequestContext(request);
    JSONObject json = new JSONObject();
    if (!file.isEmpty())
    {
      if (file.getSize() > 2097152L) {
        json.put("message", requestContext.getMessage("g_fileTooLarge"));
      } else {
        try
        {
          String originalFilename = file.getOriginalFilename();
          String fileName = sdf.format(new Date()) + JavaEEFrameworkUtils.getRandomString(3) + 
            originalFilename.substring(originalFilename.lastIndexOf("."));
          File filePath = new File(getClass().getClassLoader().getResource("/").getPath().replace(
            "/WEB-INF/classes/", "/static/upload/img/" + DateFormatUtils.format(new Date(), "yyyyMM")));
          if (!filePath.exists()) {
            filePath.mkdirs();
          }
          file.transferTo(new File(filePath.getAbsolutePath() + "\\" + fileName));
          String destinationFilePath = "/static/upload/img/" + DateFormatUtils.format(new Date(), "yyyyMM") + 
            "/" + fileName;
          Long sysUserId = ((SysUser)request.getSession().getAttribute("SESSION_SYS_USER")).getId();
          this.attachmentService.deleteByProperties(new String[] { "type", "typeId" }, 
            new Object[] { Short.valueOf((short) 1), sysUserId });
          Attachment attachment = new Attachment();
          attachment.setFileName(originalFilename);
          attachment.setFilePath(destinationFilePath);
          attachment.setType(Short.valueOf((short)1));
          attachment.setTypeId(sysUserId);
          this.attachmentService.persist(attachment);
          json.put("status", "OK");
          json.put("url", request.getContextPath() + destinationFilePath);
          json.put("message", requestContext.getMessage("g_uploadSuccess"));
        }
        catch (Exception e)
        {
          e.printStackTrace();
          json.put("message", requestContext.getMessage("g_uploadFailure"));
        }
      }
    }
    else {
      json.put("message", requestContext.getMessage("g_uploadNotExists"));
    }
    writeJSON(response, json.toString());
  }
  
  @RequestMapping({"/sysuser"})
  public String sysuser(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/systemmanage/sysuser";
  }
  
  @RequestMapping({"/gameuser"})
  public String gameuser(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    Subject subject = SecurityUtils.getSubject();
    Session session = subject.getSession();
    String ROLE = String.valueOf(session.getAttribute("ROLE_KEY"));
    if (("ROLE_ADMIN".equals(ROLE)) || ("ROLE_RESTRICTED_ADMIN".equals(ROLE))) {
      request.setAttribute("gameRole", Boolean.valueOf(false));
    } else {
      request.setAttribute("gameRole", Boolean.valueOf(true));
    }
    return "back/gameuser/user";
  }
  
  @RequestMapping({"/gamesys"})
  public String gamesys(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/gameuser/gamesys";
  }
  
  @RequestMapping({"/mysell"})
  public String mysell(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/gameuser/mysell";
  }
  
  @RequestMapping({"/gamerecharge"})
  public String gamerecharge(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/gameuser/gamerecharge";
  }
  
  @RequestMapping({"/homepage"})
  public String homepage(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/homepage";
  }
  
  @RequestMapping({"/dict"})
  public String dict(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/systemmanage/dict";
  }
  
  @RequestMapping({"/role"})
  public String role(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/systemmanage/role";
  }
  
  @RequestMapping({"/department"})
  public String department(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/systemmanage/department";
  }
  
  @RequestMapping({"/mail"})
  public String mail(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/systemmanage/mail";
  }
  
  @RequestMapping({"/information"})
  public String information(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/infomanage/information";
  }
  
  @RequestMapping({"/authority"})
  public String authority(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/systemmanage/authority";
  }
  
  @RequestMapping({"/typography"})
  public String typography(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/typography";
  }
  
  @RequestMapping({"/uielements"})
  public String uielements(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/uielements";
  }
  
  @RequestMapping({"/buttonsicon"})
  public String buttonsicon(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/buttonsicon";
  }
  
  @RequestMapping({"/contentslider"})
  public String contentslider(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/contentslider";
  }
  
  @RequestMapping({"/nestablelist"})
  public String nestablelist(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/nestablelist";
  }
  
  @RequestMapping({"/jquerydatatables"})
  public String jquerydatatables(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/jquerydatatables";
  }
  
  @RequestMapping({"/formelements"})
  public String formelements(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/formelements";
  }
  
  @RequestMapping({"/formelements2"})
  public String formelements2(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/formelements2";
  }
  
  @RequestMapping({"/wizardvalidation"})
  public String wizardvalidation(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/wizardvalidation";
  }
  
  @RequestMapping({"/uiwidgets"})
  public String uiwidgets(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/uiwidgets";
  }
  
  @RequestMapping({"/calendar"})
  public String calendar(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/calendar";
  }
  
  @RequestMapping({"/gallery"})
  public String gallery(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/gallery";
  }
  
  @RequestMapping({"/pricingtables"})
  public String pricingtables(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/pricingtables";
  }
  
  @RequestMapping({"/invoice"})
  public String invoice(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/invoice";
  }
  
  @RequestMapping({"/timeline"})
  public String timeline(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/timeline";
  }
  
  @RequestMapping({"/faq"})
  public String faq(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/faq";
  }
  
  @RequestMapping({"/grid"})
  public String grid(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/bootstrapexample/grid";
  }
  
  @RequestMapping({"/charts"})
  public String charts(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/chartandreport/charts";
  }
  
  @RequestMapping({"/callError404"})
  public String callError404(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "redirect:/sys/sysuser/error404";
  }
  
  @RequestMapping({"/error404"})
  public String error404(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/error404";
  }
  
  @RequestMapping({"/callError500"})
  public String callError500(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "redirect:/sys/sysuser/error500";
  }
  
  @RequestMapping({"/error500"})
  public String error500(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/error500";
  }
  
  @RequestMapping({"/callUnauthorized"})
  public String callUnauthorized(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "redirect:/sys/sysuser/unauthorized";
  }
  
  @RequestMapping({"/unauthorized"})
  public String unauthorized(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/unauthorized";
  }
  
  @RequestMapping({"/druid"})
  public String druid(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    return "back/druid";
  }
  
  public boolean isBigInteger(String num)
  {
    Pattern pattern = Pattern.compile("[0-9]*");
    if ((num == null) || ("".equals(num))) {
      return false;
    }
    Matcher isNum = pattern.matcher(num);
    if (isNum.matches()) {
      return true;
    }
    return false;
  }
  
  public static void main(String[] args)
  {
    BigInteger a = new BigInteger("1");
    BigInteger b = new BigInteger("2");
    BigInteger c = a.subtract(b);
    System.out.println(c);
  }
}
