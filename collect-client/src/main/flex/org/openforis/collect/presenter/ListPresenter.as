package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.MenuEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.PaginationBarEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.DataExportPopUp;
	import org.openforis.collect.ui.component.DataImportPopUp;
	import org.openforis.collect.ui.component.RecordFilterPopUp;
	import org.openforis.collect.ui.component.SelectVersionPopUp;
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.ui.view.ListView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.events.GridSortEvent;


	public class ListPresenter extends AbstractPresenter {
		
		private const EXPORT_DATA_MENU_ITEM:String = Message.get("list.admin.exportData");
		private const IMPORT_DATA_MENU_ITEM:String = Message.get("list.admin.importData");
		
		private var _view:ListView;
		private var _dataClient:DataClient;
		
		private var _selectVersionPopUp:SelectVersionPopUp;
		private var _newRecordResponder:IResponder;
		private var _filterPopUp:RecordFilterPopUp;
		
		/**
		 * The current sortField
		 */
		private var currentSortFields:IList;
		/**
		 * The current filter applied on root entity key fields. 
		 * */
		private var currentKeyValuesFilter:Array = null;
		
		/**
		 * Max number of records that can be loaded for a single page.
		 */
		private const MAX_RECORDS_PER_PAGE:int = 20;
		
		public function ListPresenter(view:ListView) {
			this._view = view;
			this._dataClient = ClientFactory.dataClient;
			this._view.dataGrid.requestedRowCount = MAX_RECORDS_PER_PAGE;
			_newRecordResponder = new AsyncResponder(createRecordResultHandler, faultHandler);
			createAdvancedFunctionMenu();
			_view.paginationBar.maxRecordsPerPage = MAX_RECORDS_PER_PAGE;
			super();
		}

		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.LOAD_RECORD_SUMMARIES, loadRecordSummariesHandler);
			eventDispatcher.addEventListener(UIEvent.RELOAD_RECORD_SUMMARIES, reloadRecordSummariesHandler);
			
			_view.backToMainMenuButton.addEventListener(MouseEvent.CLICK, backToMainMenuClickHandler);
			_view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			_view.editButton.addEventListener(MouseEvent.CLICK, editButtonClickHandler);
			_view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			_view.advancedFunctionsButton.addEventListener(MenuEvent.ITEM_CLICK, advancedFunctionItemClickHandler);
			_view.openFilterPopUpButton.addEventListener(MouseEvent.CLICK, openFilterPopUpButtonClickHandler);
			
			_view.dataGrid.addEventListener(GridSortEvent.SORT_CHANGING, dataGridSortChangingHandler);
			
			_view.paginationBar.addEventListener(PaginationBarEvent.PAGE_CHANGE, summaryPageChangeHandler);
			_view.stage.addEventListener(MouseEvent.CLICK, stageClickHandler);
		}
		
		protected function backToMainMenuClickHandler(event:Event):void {
			eventDispatcher.dispatchEvent(new UIEvent(UIEvent.SHOW_HOME_PAGE));
		}
		
		/**
		 * New Record Button clicked 
		 * */
		protected function addButtonClickHandler(event:MouseEvent):void {
			var versions:ListCollectionView = Application.activeSurvey.versions;
			if ( CollectionUtil.isEmpty(versions) ) {
				addNewRecord();
			} else if ( versions.length == 1) {
				addNewRecord(versions.getItemAt(0) as ModelVersionProxy);
			} else {
				openSelectVersionPopUp();
			}
		}
		protected function openSelectVersionPopUp():void {
			if(_selectVersionPopUp == null) {
				_selectVersionPopUp = new SelectVersionPopUp();
				PopUpManager.addPopUp(_selectVersionPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
				_selectVersionPopUp.addEventListener(CloseEvent.CLOSE, cancelSelectVersionClickHandler);
				_selectVersionPopUp.addButton.addEventListener(MouseEvent.CLICK, addRecordButtonClickHandler);
				_selectVersionPopUp.cancelButton.addEventListener(MouseEvent.CLICK, cancelSelectVersionClickHandler);
			} else {
				PopUpManager.addPopUp(_selectVersionPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			}
			var rootEntityLabel:String = Application.activeRootEntity.getInstanceOrHeadingLabelText();
			_selectVersionPopUp.versionsDropDownList.dataProvider = Application.activeSurvey.versions;
			_selectVersionPopUp.title = Message.get('list.newRecordPopUp.title', [rootEntityLabel]);
			PopUpManager.centerPopUp(_selectVersionPopUp);
		}
		
		protected function createAdvancedFunctionMenu():void {
			var result:ArrayCollection = new ArrayCollection();
			result.addItem(EXPORT_DATA_MENU_ITEM);
			if ( Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN) ) {
				result.addItem(IMPORT_DATA_MENU_ITEM);
			}
			_view.advancedFunctionsButton.dataProvider = result;
		}
		
		protected function advancedFunctionItemClickHandler(event:MenuEvent):void {
			switch ( event.item ) {
				case IMPORT_DATA_MENU_ITEM:
					PopUpUtil.createPopUp(DataImportPopUp, true);
					break;
				case EXPORT_DATA_MENU_ITEM:
					PopUpUtil.createPopUp(DataExportPopUp, true);
					break;
			}
		}
		
		protected function addRecordButtonClickHandler(event:Event):void {
			var version:ModelVersionProxy = ModelVersionProxy(_selectVersionPopUp.versionsDropDownList.selectedItem);
			PopUpManager.removePopUp(_selectVersionPopUp);
			addNewRecord(version);
		}

		protected function addNewRecord(version:ModelVersionProxy = null):void {
			var rootEntityName:String = Application.activeRootEntity.name;
			var versionName:String = version != null ? version.name: null;
			_dataClient.createNewRecord(_newRecordResponder, rootEntityName, versionName);
		}
		
		protected function cancelSelectVersionClickHandler(event:Event):void {
			PopUpManager.removePopUp(_selectVersionPopUp);
		}
		
		protected function createRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var record:RecordProxy = event.result as RecordProxy;
			record.survey = Application.activeSurvey;
			record.init();
			var uiEvent:UIEvent = new UIEvent(UIEvent.RECORD_CREATED);
			uiEvent.obj = record;
			eventDispatcher.dispatchEvent(uiEvent);
			PopUpManager.removePopUp(_view);
		}
		
		/**
		 * Edit Button clicked 
		 * */
		protected function editButtonClickHandler(event:MouseEvent):void {
			var selectedRecord:RecordProxy = _view.dataGrid.selectedItem as RecordProxy;
			if(selectedRecord != null) {
				var uiEvent:UIEvent = new UIEvent(UIEvent.RECORD_SELECTED);
				uiEvent.obj = selectedRecord;
				eventDispatcher.dispatchEvent(uiEvent);
			} else {
				AlertUtil.showError("list.error.recordNotSelected");
			}
		}
		
		/**
		 * Delete Button clicked 
		 * */
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			var rootEntityLabel:String = Application.activeRootEntity.getInstanceOrHeadingLabelText();
			var selectedRecord:RecordProxy = _view.dataGrid.selectedItem as RecordProxy;
			if(selectedRecord == null) {
				AlertUtil.showError("list.error.recordNotSelected");
			} else if ( selectedRecord.step != CollectRecord$Step.ENTRY ) {
				var stepName:String = Message.get("phase." + selectedRecord.step.name);
				if ( Application.user.canReject(selectedRecord) ) {
					AlertUtil.showError("list.error.cannotDelete.rejectBeforeDeletePromotedRecord", [rootEntityLabel, stepName]);
				} else {
					AlertUtil.showError("list.error.cannoDeletePromotedRecord", [rootEntityLabel, stepName]);
				}
			} else {
				AlertUtil.showConfirm("list.delete.confirm", [rootEntityLabel], "list.delete.confirmTitle", executeDelete, [selectedRecord]);
			}
		}
		
		protected function executeDelete(record:RecordProxy):void {
			_dataClient.deleteRecord(new AsyncResponder(deleteRecordResultHandler, faultHandler), record.id);
		}
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			PopUpUtil.createPopUp(DataExportPopUp, true);
		}
		
		protected function openFilterPopUpButtonClickHandler(event:Event):void {
			if ( _filterPopUp != null ) {
				closeFilterPopUp();
			} else if ( currentKeyValuesFilter == null ) {
				openFilterPopUp();
			} else {
				resetCurrentFilter();
				loadRecordSummaries();
			}
		}
		
		protected function openFilterPopUp():void {
			var application:DisplayObject = DisplayObject(FlexGlobals.topLevelApplication);
			_filterPopUp = RecordFilterPopUp(PopUpManager.createPopUp(application, RecordFilterPopUp));
			_filterPopUp.addEventListener(CloseEvent.CLOSE, filterPopUpCloseHandler);
			_filterPopUp.applyButton.addEventListener(MouseEvent.CLICK, filterPopUpApplyHandler);
			PopUpManager.addPopUp(_filterPopUp, application);
			var keyAttributeDefinitions:IList = Application.activeRootEntity.keyAttributeDefinitions;
			_filterPopUp.fieldsRp.dataProvider = keyAttributeDefinitions;
			for(var index:int = 0; index < _filterPopUp.textInput.length; index ++) {
				var textInput:TextInput = _filterPopUp.textInput[index];
				textInput.addEventListener(FlexEvent.ENTER, filterPopUpApplyHandler);
			}
			PopUpUtil.alignToField(_filterPopUp, _view.openFilterPopUpButton, 
				PopUpUtil.POSITION_BELOW, 
				PopUpUtil.VERTICAL_ALIGN_BOTTOM, 
				PopUpUtil.HORIZONTAL_ALIGN_RIGHT);
			
			var firstTextInput:TextInput = _filterPopUp.textInput[0];
			firstTextInput.setFocus();
		}
		
		protected function stageClickHandler(event:MouseEvent):void {
			if ( event.target != _view.openFilterPopUpButton &&  _filterPopUp != null && 
				! _filterPopUp.hitTestPoint( event.stageX, event.stageY ) ) {
				closeFilterPopUp();
			}
		}
		
		protected function closeFilterPopUp():void {
			if ( _filterPopUp != null ) {
				PopUpManager.removePopUp(_filterPopUp);
				_filterPopUp = null;
			}
			_view.openFilterPopUpButton.selected = currentKeyValuesFilter != null;
		}
		
		protected function filterPopUpCloseHandler(event:Event = null):void {
			var oldFilter:Array = currentKeyValuesFilter;
			currentKeyValuesFilter = null;
			if(oldFilter != null) {
				loadRecordSummaries();
			}
			closeFilterPopUp();
		}
		
		protected function filterPopUpApplyHandler(event:Event):void {
			var filter:Array = new Array();
			var empty:Boolean = true;
			for each (var textInput:TextInput in _filterPopUp.textInput) {
				var key:String = StringUtil.trim(textInput.text).toUpperCase();
				filter.push(key);
				if ( key != "" ) {
					empty = false;
				}
			}
			if ( ! empty ) {
				currentKeyValuesFilter = filter;
			} else {
				currentKeyValuesFilter = null;
			}
			loadRecordSummaries();
			closeFilterPopUp();
		}
		
		/**
		 * Loads records summaries for active root entity
		 * */
		protected function loadRecordSummariesHandler(event:UIEvent):void {
			if ( event.obj != null && event.obj.firstAccess ) {
				resetCurrentFilter();
			}
			var surveyProjectName:String = Application.activeSurvey.getProjectName();
			var rootEntityLabel:String = Application.activeRootEntity.getInstanceOrHeadingLabelText();
			_view.titleLabel.text = Message.get("list.title", [surveyProjectName, rootEntityLabel]);
			updateDataGrid();
			loadRecordSummaries();
		}
		
		protected function reloadRecordSummariesHandler(event:UIEvent):void {
			reloadRecordSummaries();
		}
		
		protected function reloadRecordSummaries():void {
			loadRecordSummaries(_view.paginationBar.offset);
		}
		
		protected function resetCurrentFilter():void {
			currentKeyValuesFilter = null;
			_view.openFilterPopUpButton.selected = false;
		}
		
		protected function updateDataGrid():void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var columns:IList = UIBuilder.getRecordSummaryListColumns(rootEntity);
			_view.dataGrid.columns = columns;
		}
		
		protected function loadRecordSummaries(offset:int = 0):void {
			if ( offset == 0 ) {
				_view.paginationBar.showPage(1);
			}
			//_view.paginationBar.currentPageText.text = new String(currentPage);
			
			_view.currentState = ListView.INACTIVE_STATE;
			_view.paginationBar.currentState = PaginationBar.LOADING_STATE;
			
			var responder:IResponder = new AsyncResponder(getRecordsSummaryResultHandler, faultHandler);
			var rootEntityName:String = Application.activeRootEntity.name;
			
			_dataClient.loadRecordSummaries(responder, rootEntityName, 
				offset, MAX_RECORDS_PER_PAGE, currentSortFields, currentKeyValuesFilter);
		}
		
		protected function getRecordsSummaryResultHandler(event:ResultEvent, token:Object = null):void {
			var result:Object = event.result;
			
			_view.dataGrid.dataProvider = IList(result.records);
			_view.dataGrid.setSortedColumns(currentSortFields);
			_view.currentState = ListView.DEFAULT_STATE;
			
			_view.paginationBar.totalRecords = result.count;
			
			if(result.totalCount == 0 && currentKeyValuesFilter != null) {
				AlertUtil.showMessage("list.filter.noRecordsFound");
			}
		}
		
		protected function deleteRecordResultHandler(event:ResultEvent, token:Object = null):void {
			reloadRecordSummaries();
		}

		protected function summaryPageChangeHandler(event:PaginationBarEvent):void {
			loadRecordSummaries(event.offset);
		}
		
		protected function dataGridSortChangingHandler(event:GridSortEvent):void {
			//avoid client sorting, perform the sorting by server side
			event.preventDefault();
			var oldSortFields:IList = currentSortFields;
			currentSortFields = RecordSummaryDataGrid.createRecordSummarySortFields(event.newSortFields, oldSortFields);;
			
			reloadRecordSummaries();
		}
		
		
	}
}