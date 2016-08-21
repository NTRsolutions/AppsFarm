package is.web.beans.offerRewardTypes;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;



public class ImageBannerDataModelBean extends ListDataModel<ImageBannerEntity> implements SelectableDataModel<ImageBannerEntity> {

	public ImageBannerDataModelBean(List<ImageBannerEntity> data) {
		super(data);
	}

	@Override
	public ImageBannerEntity getRowData(String rowKey) {
		List<ImageBannerEntity> elements = (List<ImageBannerEntity>) getWrappedData();

		for (ImageBannerEntity element : elements) {
			if (element.getId().equals(rowKey))
				return element;
		}

		return null;
	}

	@Override
	public Object getRowKey(ImageBannerEntity element) {
		return element.getId();
	}

}
