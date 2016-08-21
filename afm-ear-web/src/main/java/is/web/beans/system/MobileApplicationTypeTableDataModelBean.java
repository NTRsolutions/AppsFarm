package is.web.beans.system;

import is.ejb.dl.entities.MobileApplicationTypeEntity;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

public class MobileApplicationTypeTableDataModelBean extends
		ListDataModel<MobileApplicationTypeEntity> implements
		SelectableDataModel<MobileApplicationTypeEntity> {

	public MobileApplicationTypeTableDataModelBean(
			List<MobileApplicationTypeEntity> data) {
		super(data);
	}

	@Override
	public MobileApplicationTypeEntity getRowData(String rowKey) {
		List<MobileApplicationTypeEntity> elements = (List<MobileApplicationTypeEntity>) getWrappedData();

		for (MobileApplicationTypeEntity element : elements) {
			if (String.valueOf(element.getId()).equals(rowKey))
				return element;
		}

		return null;
	}

	@Override
	public Object getRowKey(MobileApplicationTypeEntity element) {
		return String.valueOf(element.getId());
	}
}