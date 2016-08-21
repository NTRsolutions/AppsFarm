package is.web.beans.system.external;

import is.ejb.dl.entities.ExternalServerAddressEntity;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

public class ExternalServerAddressTableDataModelBean extends ListDataModel<ExternalServerAddressEntity> implements SelectableDataModel<ExternalServerAddressEntity> {

	public ExternalServerAddressTableDataModelBean (List<ExternalServerAddressEntity> data) {
		super(data);
	}

	@Override
	public ExternalServerAddressEntity getRowData(String rowKey) {
		List<ExternalServerAddressEntity> elements = (List<ExternalServerAddressEntity>) getWrappedData();

		for (ExternalServerAddressEntity element : elements) {
			if (String.valueOf(element.getId()).equals(rowKey))
				return element;
		}

		return null;
	}

	@Override
	public Object getRowKey(ExternalServerAddressEntity element) {
		return String.valueOf(element.getId());
	}

}
