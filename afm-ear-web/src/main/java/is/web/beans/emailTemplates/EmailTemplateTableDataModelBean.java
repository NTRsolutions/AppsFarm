package is.web.beans.emailTemplates;
import is.ejb.dl.entities.*;

import java.util.List;  

import javax.faces.model.ListDataModel;  

import org.primefaces.model.SelectableDataModel;  



public class EmailTemplateTableDataModelBean extends ListDataModel<EmailTemplateEntity> implements SelectableDataModel<EmailTemplateEntity> {    
  
    public EmailTemplateTableDataModelBean(List<EmailTemplateEntity> data) {  
        super(data);  
    }  
      
    @Override  
    public EmailTemplateEntity getRowData(String rowKey) {  
      
        List<EmailTemplateEntity> elements = (List<EmailTemplateEntity>) getWrappedData();  
          
        for(EmailTemplateEntity element : elements) {  
            if(String.valueOf(element.getId()).equals(rowKey))  
                return element;  
        }  
          
        return null;  
    }  
  
    @Override  
    public Object getRowKey(EmailTemplateEntity element) {  
        return element.getId();  
    }  
}  










