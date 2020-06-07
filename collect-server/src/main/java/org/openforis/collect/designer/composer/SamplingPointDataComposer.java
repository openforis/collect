package org.openforis.collect.designer.composer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.designer.viewmodel.SamplingPointDataPagingListModel;
import org.openforis.collect.designer.viewmodel.SamplingPointDataVM;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignFileColumn;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.zkoss.bind.GlobalCommandEvent;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SamplingPointDataComposer extends SelectorComposer<Component> {

	private static final long serialVersionUID = -17595662335212913L;

	@WireVariable
	private SamplingDesignManager samplingDesignManager;
	@WireVariable
	private SessionManager sessionManager;

	private final int _pageSize = 20;
	private int _startPageNumber = 0;
	private int _totalSize = 0;
	private boolean _needsTotalSizeUpdate = true;

	@Wire
	private Listbox samplingPointDataListBox;
	@Wire
	private Paging paging;

	SamplingPointDataPagingListModel model = null;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		samplingPointDataListBox.setItemRenderer(new ListitemRenderer<SamplingDesignItem>() {
			public void render(Listitem item, SamplingDesignItem data, int index) throws Exception {
				List<String> columnNames = getColumnNames();
				for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
					item.getChildren().add(new Listcell(getColValue((SamplingDesignItem) data, colIndex)));
				}
			}
		});

		refreshModel();

		// Refresh model on sampling point data update
		EventQueues.lookup(SamplingPointDataVM.SAMPLING_POINT_DATA_QUEUE).subscribe(new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				if (event instanceof GlobalCommandEvent && SamplingPointDataVM.SAMPLING_POINT_DATA_UPDATED_COMMAND
						.equals(((GlobalCommandEvent) event).getCommand())) {
					onSamplingPointDataUpdated();
				}
			}
		});
	}

	public void onSamplingPointDataUpdated() {
		this._startPageNumber = 0;
		this._needsTotalSizeUpdate = true;
		refreshModel();
	}

	public List<String> getColumnNames() {
		ArrayList<String> colNames = new ArrayList<String>();
		colNames.addAll(Arrays.asList(SamplingDesignFileColumn.ALL_COLUMN_NAMES));
		List<String> infoAttributeNames = getSurvey().getReferenceDataSchema().getSamplingPointDefinition()
				.getAttributeNames();
		colNames.addAll(infoAttributeNames);
		return colNames;
	}

	public String getColLabel(String colName) {
		SamplingDesignFileColumn column = SamplingDesignFileColumn.fromColumnName(colName);
		if (column == null) {
			// info column
			return colName;
		} else {
			// predefined column (levelX_code, x, y, srs_id)
			return Labels.getLabel("survey.sampling_point_data." + colName);
		}
	}

	public String getColValue(SamplingDesignItem item, int index) {
		int fixedColumnsLength = SamplingDesignFileColumn.ALL_COLUMN_NAMES.length;
		if (index >= fixedColumnsLength) {
			// info column
			return item.getInfoAttribute(index - fixedColumnsLength);
		} else {
			SamplingDesignFileColumn column = SamplingDesignFileColumn.ALL_COLUMNS[index];
			if (column.isLevelColumn()) {
				int level = column.getLevel();
				return item.getLevelCodes().size() >= level ? item.getLevelCode(level) : null;
			} else if (column == SamplingDesignFileColumn.X) {
				Double value = item.getX();
				return value == null ? null : value.toString();
			} else if (column == SamplingDesignFileColumn.Y) {
				Double value = item.getY();
				return value == null ? null : value.toString();
			} else if (column == SamplingDesignFileColumn.SRS_ID) {
				return item.getSrsId();
			} else {
				return null;
			}
		}
	}

	@Listen("onPaging = paging#paging")
	public void onPaging(PagingEvent event) {
		_startPageNumber = event.getActivePage();
		refreshModel();
	}

	private void refreshModel() {
		CollectSurvey survey = getSurvey();
		int surveyId = survey.getId();

		paging.setPageSize(_pageSize);
		model = new SamplingPointDataPagingListModel(samplingDesignManager, surveyId, _startPageNumber, _pageSize);

		if (_needsTotalSizeUpdate) {
			_totalSize = model.getTotalSize();
			_needsTotalSizeUpdate = false;
		}

		paging.setTotalSize(_totalSize);

		samplingPointDataListBox.setModel(model);
	}

	private CollectSurvey getSurvey() {
		return sessionManager.getActiveDesignerSurvey();
	}

}