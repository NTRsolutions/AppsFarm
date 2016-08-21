package is.web.beans.system;

import is.ejb.dl.entities.WalletPayoutCarrierEntity;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

public class WalletSettingsTableDataModelBean extends
		ListDataModel<WalletPayoutCarrierEntity> implements
		SelectableDataModel<WalletPayoutCarrierEntity> {

	public WalletSettingsTableDataModelBean(
			List<WalletPayoutCarrierEntity> data) {
		super(data);
	}

	@Override
	public WalletPayoutCarrierEntity getRowData(String rowKey) {
	List<WalletPayoutCarrierEntity> elements = (List<WalletPayoutCarrierEntity>) getWrappedData();  
        
        for(WalletPayoutCarrierEntity element : elements) {  
            if(String.valueOf(element.getId()).equals(rowKey))  
                return element;  
        }  
          
        return null;  
	}

	@Override
	public Object getRowKey(WalletPayoutCarrierEntity element) {
		return String.valueOf(element.getId());
	}
}
