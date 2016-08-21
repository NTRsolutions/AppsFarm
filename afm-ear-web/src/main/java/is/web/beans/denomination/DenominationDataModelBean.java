package is.web.beans.denomination;

import is.ejb.dl.entities.DenominationModelEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class DenominationDataModelBean extends ListDataModel<DenominationModelEntity> implements SelectableDataModel<DenominationModelEntity> {    
  
    public DenominationDataModelBean(List<DenominationModelEntity> data) {  
        super(data);  
    }  
      
    @Override  
    public DenominationModelEntity getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<DenominationModelEntity> elements = (List<DenominationModelEntity>) getWrappedData();  
          
        for(DenominationModelEntity element : elements) {  
            if(element.getName().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(DenominationModelEntity element) {  
        return element.getName();  
    }  
}  










