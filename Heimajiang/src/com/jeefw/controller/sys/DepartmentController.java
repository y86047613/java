package com.jeefw.controller.sys;

import com.jeefw.core.Constant;
import com.jeefw.core.JavaEEFrameworkBaseController;
import com.jeefw.model.sys.Department;
import com.jeefw.service.sys.DepartmentService;
import core.support.ExtJSBaseParameter;
import core.support.JqGridPageView;
import core.support.QueryResult;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping({"/sys/department"})
public class DepartmentController
  extends JavaEEFrameworkBaseController<Department>
  implements Constant
{
  @Resource
  private DepartmentService departmentService;
  
  @RequestMapping(value={"/getDepartment"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void getDepartment(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    Integer firstResult = Integer.valueOf(request.getParameter("page"));
    Integer maxResults = Integer.valueOf(request.getParameter("rows"));
    String sortedObject = request.getParameter("sidx");
    String sortedValue = request.getParameter("sord");
    String filters = request.getParameter("filters");
    Department department = new Department();
    if (StringUtils.isNotBlank(filters))
    {
      JSONObject jsonObject = JSONObject.fromObject(filters);
      JSONArray jsonArray = (JSONArray)jsonObject.get("rules");
      for (int i = 0; i < jsonArray.size(); i++)
      {
        JSONObject result = (JSONObject)jsonArray.get(i);
        if ((result.getString("field").equals("departmentKey")) && (result.getString("op").equals("eq"))) {
          department.set$eq_departmentKey(result.getString("data"));
        }
        if ((result.getString("field").equals("departmentValue")) && (result.getString("op").equals("cn"))) {
          department.set$like_departmentValue(result.getString("data"));
        }
      }
      if (((String)jsonObject.get("groupOp")).equalsIgnoreCase("OR")) {
        department.setFlag("OR");
      } else {
        department.setFlag("AND");
      }
    }
    department.setFirstResult(Integer.valueOf((firstResult.intValue() - 1) * maxResults.intValue()));
    department.setMaxResults(maxResults);
    Map<String, String> sortedCondition = new HashMap();
    sortedCondition.put(sortedObject, sortedValue);
    department.setSortedConditions(sortedCondition);
    QueryResult<Department> queryResult = this.departmentService.doPaginationQuery(department);
    JqGridPageView<Department> departmentListView = new JqGridPageView();
    departmentListView.setMaxResults(maxResults.intValue());
    List<Department> departmentCnList = this.departmentService.queryDepartmentCnList(queryResult.getResultList());
    departmentListView.setRows(departmentCnList);
    departmentListView.setRecords(queryResult.getTotalCount().longValue());
    writeJSON(response, departmentListView);
  }
  
  @RequestMapping(value={"/saveDepartment"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void doSave(Department entity, HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    ExtJSBaseParameter parameter = entity;
    if ("edit".equals(parameter.getCmd())) {
      this.departmentService.merge(entity);
    } else if ("new".equals(parameter.getCmd())) {
      this.departmentService.persist(entity);
    }
    parameter.setSuccess(Boolean.valueOf(true));
    writeJSON(response, parameter);
  }
  
  @RequestMapping(value={"/operateDepartment"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void operateDepartment(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    String oper = request.getParameter("oper");
    String id = request.getParameter("id");
    if (oper.equals("del"))
    {
      String[] ids = id.split(",");
      deleteDepartment(request, response, (Long[])ConvertUtils.convert(ids, Long.class));
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
      String departmentKey = request.getParameter("departmentKey");
      String departmentValue = request.getParameter("departmentValue");
      String parentDepartmentkey = request.getParameter("parentDepartmentValue");
      String description = request.getParameter("description");
      Department department = null;
      if (oper.equals("edit")) {
        department = (Department)this.departmentService.get(Long.valueOf(id));
      }
      Department departmentKeyDepartment = (Department)this.departmentService.getByProerties("departmentKey", departmentKey);
      Department parentDepartmentkeyDepartment = (Department)this.departmentService.getByProerties("departmentKey", parentDepartmentkey);
      if ((StringUtils.isBlank(departmentKey)) || (StringUtils.isBlank(departmentValue)))
      {
        response.setStatus(411);
        result.put("message", "请填写部门编码和部门名称");
        writeJSON(response, result);
      }
      else if ((departmentKeyDepartment != null) && (oper.equals("add")))
      {
        response.setStatus(409);
        result.put("message", "此部门编码已存在，请重新输入");
        writeJSON(response, result);
      }
      else if ((departmentKeyDepartment != null) && (!department.getDepartmentKey().equalsIgnoreCase(departmentKey)) && (oper.equals("edit")))
      {
        response.setStatus(409);
        result.put("message", "此部门已存在，请重新输入");
        writeJSON(response, result);
      }
      else if ((StringUtils.isNotBlank(parentDepartmentkey)) && (parentDepartmentkeyDepartment == null))
      {
        response.setStatus(409);
        result.put("message", "上级部门编码输入有误，请重新输入");
        writeJSON(response, result);
      }
      else if ((StringUtils.isNotBlank(parentDepartmentkey)) && (parentDepartmentkey.equals(departmentKey)))
      {
        response.setStatus(409);
        result.put("message", "不能选择自己作为上级部门，请重新输入");
        writeJSON(response, result);
      }
      else
      {
        Department entity = new Department();
        entity.setDepartmentKey(departmentKey);
        entity.setDepartmentValue(departmentValue);
        entity.setParentDepartmentkey(parentDepartmentkey);
        entity.setDescription(description);
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
  
  @RequestMapping({"/deleteDepartment"})
  public void deleteDepartment(HttpServletRequest request, HttpServletResponse response, @RequestParam("ids") Long[] ids)
    throws IOException
  {
    boolean flag = this.departmentService.deleteByPK(ids);
    if (flag) {
      writeJSON(response, "{success:true}");
    } else {
      writeJSON(response, "{success:false}");
    }
  }
  
  @RequestMapping(value={"/getDepartmentSelectList"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void getDepartmentSelectList(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    List<Department> departmentList = this.departmentService.doQueryAll();
    StringBuilder builder = new StringBuilder();
    builder.append("<select>");
    for (int i = 0; i < departmentList.size(); i++) {
      builder.append("<option value='" + ((Department)departmentList.get(i)).getDepartmentKey() + "'>" + ((Department)departmentList.get(i)).getDepartmentValue() + "</option>");
    }
    builder.append("</select>");
    writeJSON(response, builder.toString());
  }
  
  @RequestMapping(value={"/getDepartmentSelectNoSelfList"}, method={org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
  public void getDepartmentSelectNoSelfList(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    String departmentKey = request.getParameter("departmentKey") == null ? "" : request.getParameter("departmentKey");
    List<Department> departmentList = this.departmentService.doQueryAll();
    StringBuilder builder = new StringBuilder();
    builder.append("<select><option value=''></option>");
    for (int i = 0; i < departmentList.size(); i++) {
      if (!departmentKey.equals(((Department)departmentList.get(i)).getDepartmentKey())) {
        builder.append("<option value='" + ((Department)departmentList.get(i)).getDepartmentKey() + "'>" + ((Department)departmentList.get(i)).getDepartmentValue() + "</option>");
      }
    }
    builder.append("</select>");
    writeJSON(response, builder.toString());
  }
}
