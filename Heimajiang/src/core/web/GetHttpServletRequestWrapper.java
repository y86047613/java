package core.web;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class GetHttpServletRequestWrapper
  extends HttpServletRequestWrapper
{
  private String charset = "UTF-8";
  
  public GetHttpServletRequestWrapper(HttpServletRequest request)
  {
    super(request);
  }
  
  public GetHttpServletRequestWrapper(HttpServletRequest request, String charset)
  {
    super(request);
    this.charset = charset;
  }
  
  public String getParameter(String name)
  {
    String value = super.getParameter(name);
    value = value == null ? null : convert(value);
    return value;
  }
  
  public String convert(String target)
  {
    try
    {
      return new String(target.trim().getBytes("ISO-8859-1"), this.charset);
    }
    catch (UnsupportedEncodingException e) {}
    return target;
  }
}
