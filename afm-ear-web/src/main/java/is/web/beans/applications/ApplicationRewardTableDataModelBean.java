package is.web.beans.applications;
import is.ejb.dl.entities.*;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  



public class ApplicationRewardTableDataModelBean extends ListDataModel<ApplicationRewardEntity> implements SelectableDataModel<ApplicationRewardEntity> {    
  
    public ApplicationRewardTableDataModelBean(List<ApplicationRewardEntity> data) {  
        super(data);  
    }  
      
    @Override  
    public ApplicationRewardEntity getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<ApplicationRewardEntity> elements = (List<ApplicationRewardEntity>) getWrappedData();  
          
        for(ApplicationRewardEntity element : elements) {  
            if(String.valueOf(element.getId()).equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(ApplicationRewardEntity element) {  
        return element.getId();  
    }  
}  










