package is.web.beans.offers;

import is.ejb.dl.entities.OfferWallEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class OfferWallDataModelBean extends ListDataModel<OfferWallEntity> implements SelectableDataModel<OfferWallEntity> {    
  
    public OfferWallDataModelBean(List<OfferWallEntity> data) {  
        super(data);  
    }  
      
    @Override  
    public OfferWallEntity getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<OfferWallEntity> elements = (List<OfferWallEntity>) getWrappedData();  
          
        for(OfferWallEntity element : elements) {  
            if(element.getName().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(OfferWallEntity element) {  
        return element.getName();  
    }  
}  










