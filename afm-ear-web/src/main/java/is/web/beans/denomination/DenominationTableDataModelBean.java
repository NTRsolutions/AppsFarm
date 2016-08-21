package is.web.beans.denomination;

import is.ejb.bl.denominationModels.DenominationModelRow;
import is.ejb.dl.entities.OfferWallEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class DenominationTableDataModelBean extends ListDataModel<DenominationModelRow> implements SelectableDataModel<DenominationModelRow> {    
  
    public DenominationTableDataModelBean(List<DenominationModelRow> data) {  
        super(data);  
    }  
      
    @Override  
    public DenominationModelRow getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<DenominationModelRow> elements = (List<DenominationModelRow>) getWrappedData();  
          
        for(DenominationModelRow element : elements) {  
            if(element.getName().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(DenominationModelRow element) {  
        return element.getName();  
    }  
}  










