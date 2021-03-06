package core.support;

import java.util.List;

public class ListView<E>
{
  private Long totalRecord;
  private List<E> data;
  
  public Long getTotalRecord()
  {
    return this.totalRecord;
  }
  
  public void setTotalRecord(Long totalRecord)
  {
    this.totalRecord = totalRecord;
  }
  
  public List<E> getData()
  {
    return this.data;
  }
  
  public void setData(List<E> data)
  {
    this.data = data;
  }
}
