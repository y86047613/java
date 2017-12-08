package com.jeefw.service.sys.impl;

import com.jeefw.app.bean.UpdateUserPwdRequestBean;
import com.jeefw.dao.sys.AttachmentDao;
import com.jeefw.dao.sys.DepartmentDao;
import com.jeefw.dao.sys.SysUserDao;
import com.jeefw.model.sys.Attachment;
import com.jeefw.model.sys.Department;
import com.jeefw.model.sys.SysUser;
import com.jeefw.service.sys.SysUserService;
import core.service.BaseService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl
  extends BaseService<SysUser>
  implements SysUserService
{
  private SysUserDao sysUserDao;
  @Resource
  private AttachmentDao attachmentDao;
  @Resource
  private DepartmentDao departmentDao;
  
  @Resource
  public void setSysUserDao(SysUserDao sysUserDao)
  {
    this.sysUserDao = sysUserDao;
    this.dao = sysUserDao;
  }
  
  public List<SysUser> querySysUserCnList(List<SysUser> resultList)
  {
    List<SysUser> sysUserList = new ArrayList();
    for (SysUser entity : resultList)
    {
      SysUser sysUser = new SysUser();
      sysUser.setId(entity.getId());
      sysUser.setUserName(entity.getUserName());
      sysUser.setSex(entity.getSex());
      if (entity.getSex().shortValue() == 1) {
        sysUser.setSexCn("��");
      } else if (entity.getSex().shortValue() == 2) {
        sysUser.setSexCn("��");
      }
      sysUser.setEmail(entity.getEmail());
      sysUser.setPhone(entity.getPhone());
      sysUser.setBirthday(entity.getBirthday());
      sysUser.setDepartmentKey(entity.getDepartmentKey());
      if (StringUtils.isNotBlank(sysUser.getDepartmentKey()))
      {
        Department department = (Department)this.departmentDao.getByProerties("departmentKey", sysUser.getDepartmentKey());
        sysUser.setDepartmentValue(department == null ? null : department.getDepartmentValue());
      }
      sysUser.setPassword(entity.getPassword());
      sysUser.setRoleCn(this.sysUserDao.getRoleValueBySysUserId(entity.getId()));
      if (entity.getStatus().booleanValue()) {
        sysUser.setStatusCn("��");
      } else {
        sysUser.setStatusCn("��");
      }
      sysUser.setLastLoginTime(entity.getLastLoginTime());
      sysUser.setScore(entity.getScore());
      sysUserList.add(sysUser);
    }
    return sysUserList;
  }
  
  public SysUser getSysUserWithAvatar(SysUser sysuser)
  {
    SysUser entity = new SysUser();
    entity.setId(sysuser.getId());
    entity.setUserName(sysuser.getUserName());
    entity.setSex(sysuser.getSex());
    entity.setEmail(sysuser.getEmail());
    entity.setPhone(sysuser.getPhone());
    entity.setBirthday(sysuser.getBirthday());
    entity.setPassword(sysuser.getPassword());
    entity.setStatus(sysuser.getStatus());
    entity.setLastLoginTime(sysuser.getLastLoginTime());
    Attachment attachment = (Attachment)this.attachmentDao.getByProerties(new String[] { "type", "typeId" }, new Object[] { Short.valueOf(1), sysuser.getId() });
    if (attachment != null) {
      entity.setFilePath(attachment.getFilePath());
    } else {
      entity.setFilePath("/static/assets/avatars/profile-pic.jpg");
    }
    return entity;
  }
  
  public String updateSysUser(UpdateUserPwdRequestBean brb)
  {
    brb.setPassword(new Sha256Hash(brb.getPassword()).toHex());
    this.sysUserDao.updateByProperties("userName", brb.getUsername(), "password", brb.getPassword());
    return "success";
  }
}