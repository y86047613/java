package com.jeefw.model.sys.param;

import core.support.ExtJSBaseParameter;

public class RoleParameter
  extends ExtJSBaseParameter
{
  private String $eq_roleKey;
  private String $like_roleValue;
  
  public String get$eq_roleKey()
  {
    return this.$eq_roleKey;
  }
  
  public void set$eq_roleKey(String $eq_roleKey)
  {
    this.$eq_roleKey = $eq_roleKey;
  }
  
  public String get$like_roleValue()
  {
    return this.$like_roleValue;
  }
  
  public void set$like_roleValue(String $like_roleValue)
  {
    this.$like_roleValue = $like_roleValue;
  }
}
