package is.web.beans.offers;

import is.ejb.dl.entities.AdProviderEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class AdProviderDataModelBean extends ListDataModel<AdProviderEntity> implements SelectableDataModel<AdProviderEntity> {    
  
    public AdProviderDataModelBean(List<AdProviderEntity> data) {  
        super(data);  
    }  
      
    @Override  
    public AdProviderEntity getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<AdProviderEntity> elements = (List<AdProviderEntity>) getWrappedData();  
          
        for(AdProviderEntity element : elements) {  
            if(element.getName().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(AdProviderEntity element) {  
        return element.getName();  
    }  
}  










