package is.web.beans.system;

import is.ejb.dl.entities.RewardTypeEntity;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;


public class ReferralRewardTableDataModelBean extends
ListDataModel<RewardTypeEntity> implements
SelectableDataModel<RewardTypeEntity> {

public ReferralRewardTableDataModelBean(List<RewardTypeEntity> data) {
super(data);
} 

@Override
public RewardTypeEntity getRowData(String rowKey) {
List<RewardTypeEntity> elements = (List<RewardTypeEntity>) getWrappedData();

for (RewardTypeEntity element : elements) {
	if (String.valueOf(element.getId()).equals(rowKey))
		return element;
}

return null;
}

@Override
public Object getRowKey(RewardTypeEntity element) {
return String.valueOf(element.getId());
}
}