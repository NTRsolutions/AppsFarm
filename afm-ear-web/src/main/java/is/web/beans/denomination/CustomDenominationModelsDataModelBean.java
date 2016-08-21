package is.web.beans.denomination;

import is.ejb.bl.denominationModels.CustomDenominationModelAssignment;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  


public class CustomDenominationModelsDataModelBean extends ListDataModel<CustomDenominationModelAssignment> implements SelectableDataModel<CustomDenominationModelAssignment> {    
  
    public CustomDenominationModelsDataModelBean(List<CustomDenominationModelAssignment> data) {  
        super(data);  
    }  
      
    @Override  
    public CustomDenominationModelAssignment getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<CustomDenominationModelAssignment> elements = (List<CustomDenominationModelAssignment>) getWrappedData();  
          
        for(CustomDenominationModelAssignment element : elements) {  
            if(element.getRowKey().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(CustomDenominationModelAssignment element) {  
        return element.getRowKey();  
    }  
}  










