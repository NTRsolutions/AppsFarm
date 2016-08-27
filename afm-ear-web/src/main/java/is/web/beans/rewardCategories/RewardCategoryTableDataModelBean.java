package is.web.beans.rewardCategories;
import is.ejb.dl.entities.*;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  



public class RewardCategoryTableDataModelBean extends ListDataModel<RewardCategoryEntity> implements SelectableDataModel<RewardCategoryEntity> {    
  
    public RewardCategoryTableDataModelBean(List<RewardCategoryEntity> data) {  
        super(data);  
    }  
      
    @Override  
    public RewardCategoryEntity getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<RewardCategoryEntity> elements = (List<RewardCategoryEntity>) getWrappedData();  
          
        for(RewardCategoryEntity element : elements) {  
            if(String.valueOf(element.getId()).equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(RewardCategoryEntity element) {  
        return element.getId();  
    }  
}  










