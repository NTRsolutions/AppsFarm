package is.web.beans.users;

import is.ejb.dl.entities.UserEntity;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  

public class UsersDataModelBean extends ListDataModel<UserEntity> implements SelectableDataModel<UserEntity> {    
  
    public UsersDataModelBean(List<UserEntity> data) {  
        super(data);  
    }  
    
    @Override  
    public UserEntity getRowData(String rowKey) {  
        //In a real app, a more efficient way like a query by rowKey should be implemented to deal with huge data  
          
        List<UserEntity> elements = (List<UserEntity>) getWrappedData();  
          
        for(UserEntity element : elements) {  
            if(element.getName().equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(UserEntity element) {  
        return element.getName();  
    }  
}  










