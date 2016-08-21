package is.web.beans.offerRewardTypes;

import is.ejb.dl.entities.RewardTypeEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  


public class RewardTypeDataModelBean extends ListDataModel<RewardTypeEntity> implements SelectableDataModel<RewardTypeEntity> {    
  
    public RewardTypeDataModelBean(List<RewardTypeEntity> data) {  
        super(data);  
    }  
      
    @Override  
    public RewardTypeEntity getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<RewardTypeEntity> elements = (List<RewardTypeEntity>) getWrappedData();  
          
        for(RewardTypeEntity element : elements) {  
            if(element.getName().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(RewardTypeEntity element) {  
        return element.getId();  
    }

}  










