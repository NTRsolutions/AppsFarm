package is.web.beans.offers;

import is.ejb.bl.offerFilter.BlockedOffer;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  


public class BlockedOffersDataModelBean extends ListDataModel<BlockedOffer> implements SelectableDataModel<BlockedOffer> {    
  
    public BlockedOffersDataModelBean(List<BlockedOffer> data) {  
        super(data);  
    }  
      
    @Override  
    public BlockedOffer getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<BlockedOffer> elements = (List<BlockedOffer>) getWrappedData();  
          
        for(BlockedOffer element : elements) {  
            if(element.getRowKey().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(BlockedOffer element) {  
        return element.getRowKey();  
    }  
}  










