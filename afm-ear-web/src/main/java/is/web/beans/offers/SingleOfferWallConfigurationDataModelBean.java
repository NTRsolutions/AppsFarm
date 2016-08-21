package is.web.beans.offers;

import is.ejb.bl.offerWall.config.SingleOfferWallConfiguration;
import is.ejb.dl.entities.OfferWallEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class SingleOfferWallConfigurationDataModelBean extends ListDataModel<SingleOfferWallConfiguration> implements SelectableDataModel<SingleOfferWallConfiguration> {    
  
    public SingleOfferWallConfigurationDataModelBean(List<SingleOfferWallConfiguration> data) {  
        super(data);  
    }  
      
    @Override  
    public SingleOfferWallConfiguration getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<SingleOfferWallConfiguration> elements = (List<SingleOfferWallConfiguration>) getWrappedData();  
          
        for(SingleOfferWallConfiguration element : elements) {  
            if(element.getName().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(SingleOfferWallConfiguration element) {  
        return element.getName();  
    }  
}  










