package is.web.beans.system;

import is.ejb.dl.entities.RewardTypeEntity;
import is.ejb.dl.entities.SpinnerRewardEntity;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

public class SpinnerRewardTableDataModelBean extends ListDataModel<SpinnerRewardEntity> implements SelectableDataModel<SpinnerRewardEntity> {

	public SpinnerRewardTableDataModelBean(List<SpinnerRewardEntity> data) {
		super(data);
	}

	@Override
	public SpinnerRewardEntity getRowData(String rowKey) {
		List<SpinnerRewardEntity> elements = (List<SpinnerRewardEntity>) getWrappedData();

		for (SpinnerRewardEntity element : elements) {
			if (String.valueOf(element.getId()).equals(rowKey))
				return element;
		}

		return null;
	}

	@Override
	public Object getRowKey(SpinnerRewardEntity element) {
		return String.valueOf(element.getId());
	}
}