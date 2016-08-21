package is.web.beans.system;

import is.ejb.bl.offerFilter.CurrencyCode;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class CurrencyTableDataModelBean extends ListDataModel<CurrencyCode> implements SelectableDataModel<CurrencyCode> {    
  
    public CurrencyTableDataModelBean(List<CurrencyCode> data) {  
        super(data);  
    }  
      
    @Override  
    public CurrencyCode getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<CurrencyCode> elements = (List<CurrencyCode>) getWrappedData();  
          
        for(CurrencyCode element : elements) {  
            if(element.getCode().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(CurrencyCode element) {  
        return element.getCode();  
    }  
}  










