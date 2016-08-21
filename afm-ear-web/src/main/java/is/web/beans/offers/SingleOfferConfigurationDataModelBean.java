package is.web.beans.offers;

import is.ejb.bl.offerWall.content.Offer;
import is.ejb.dl.entities.OfferWallEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class SingleOfferConfigurationDataModelBean extends ListDataModel<Offer> implements SelectableDataModel<Offer> {    
  
    public SingleOfferConfigurationDataModelBean(List<Offer> data) {  
        super(data);  
    }  
      
    @Override  
    public Offer getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<Offer> elements = (List<Offer>) getWrappedData();  
          
        for(Offer element : elements) {  
            if(element.getTitle().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(Offer element) {  
        return element.getTitle();  
    }  
}  










