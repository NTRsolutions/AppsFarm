package is.web.beans.appusers;

import is.ejb.dl.entities.EventQueueEntity;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

public class EventQueueTableDataModelBean extends
		ListDataModel<EventQueueEntity> implements
		SelectableDataModel<EventQueueEntity> {

	public EventQueueTableDataModelBean(
			List<EventQueueEntity> data) {
		super(data);
	}

	@Override
	public EventQueueEntity getRowData(String rowKey) {
		List<EventQueueEntity> elements = (List<EventQueueEntity>) getWrappedData();

		for (EventQueueEntity element : elements) {
			if (String.valueOf(element.getId()).equals(rowKey))
				return element;
		}

		return null;
	}

	@Override
	public Object getRowKey(EventQueueEntity element) {
		return String.valueOf(element.getId());
	}
}