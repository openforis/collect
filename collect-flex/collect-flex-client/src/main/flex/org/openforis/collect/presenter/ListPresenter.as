package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.BindingUtils;
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.controls.Alert;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.FlexMouseEvent;
	import mx.events.PropertyChangeEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.model.CollectRecord$State;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.RecordSummarySortField;
	import org.openforis.collect.model.RecordSummarySortField$Sortable;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.BackupPopUp;
	import org.openforis.collect.ui.component.DataExportPopUp;
	import org.openforis.collect.ui.component.RecordFilterPopUp;
	import org.openforis.collect.ui.component.SelectVersionPopUp;
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.ui.view.DataExportView;
	import org.openforis.collect.ui.view.ListView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.collections.Sort;
	import spark.collections.SortField;
	import spark.events.GridSortEvent;


	public class ListPresenter extends AbstractPresenter {
		
		private var _view:ListView;
		private var _dataClient:DataClient;
		
		private var _selectVersionPopUp:SelectVersionPopUp;
		private var _newRecordResponder:IResponder;
		private var _filterPopUp:RecordFilterPopUp;
		
		/**
		 * The total number of records.
		 */
		private var totalRecords:int;
		/**
		 * The total pages of the pagination.
		 */
		private var totalPages:int;
		/**
		 * The current page of the pagination.
		 * It starts from 1 
		 */
		private var currentPage:int;
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
			super();
		}

		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.LOAD_RECORD_SUMMARIES, loadRecordSummariesHandler);
			eventDispatcher.addEventListener(UIEvent.RELOAD_RECORD_SUMMARIES, reloadRecordSummariesHandler);

			this._view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			this._view.editButton.addEventListener(MouseEvent.CLICK, editButtonClickHandler);
			this._view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			this._view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			this._view.openFilterPopUpButton.addEventListener(MouseEvent.CLICK, openFilterPopUpButtonClickHandler);
			
			this._view.dataGrid.addEventListener(GridSortEvent.SORT_CHANGING, dataGridSortChangingHandler);
			
			this._view.paginationBar.firstPageButton.addEventListener(MouseEvent.CLICK, firstPageClickHandler);
			this._view.paginationBar.previousPageButton.addEventListener(MouseEvent.CLICK, previousPageClickHandler);
			this._view.paginationBar.nextPageButton.addEventListener(MouseEvent.CLICK, nextPageClickHandler);
			this._view.paginationBar.lastPageButton.addEventListener(MouseEvent.CLICK, lastPageClickHandler);
			//this._view.paginationBar.goToPageButton.addEventListener(MouseEvent.CLICK, goToPageClickHandler);
			_view.stage.addEventListener(MouseEvent.CLICK, stageClickHandler);
		}
	
		/**
		 * New Record Button clicked 
		 * */
		protected function addButtonClickHandler(event:MouseEvent):void {
			var versions:ListCollectionView = Application.activeSurvey.versions;
			if(versions.length > 1) {
				openSelectVersionPopUp();
			} else {
				addNewRecord(versions.getItemAt(0) as ModelVersionProxy);
			}
		}
		protected function openSelectVersionPopUp():void {
			if(_selectVersionPopUp == null) {
				_selectVersionPopUp = new SelectVersionPopUp();
				PopUpManager.addPopUp(_selectVersionPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
				_selectVersionPopUp.addEventListener(CloseEvent.CLOSE, cancelSelectVersionClickHandler);
				_selectVersionPopUp.addButton.addEventListener(MouseEvent.CLICK, newRecordVersionSelectedHandler);
				_selectVersionPopUp.cancelButton.addEventListener(MouseEvent.CLICK, cancelSelectVersionClickHandler);
			} else {
				PopUpManager.addPopUp(_selectVersionPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			}
			var rootEntityLabel:String = Application.activeRootEntity.getLabelText();
			_selectVersionPopUp.versionsDropDownList.dataProvider = Application.activeSurvey.versions;
			_selectVersionPopUp.title = Message.get('list.newRecordPopUp.title', [rootEntityLabel]);
			PopUpManager.centerPopUp(_selectVersionPopUp);
		}
		
		protected function newRecordVersionSelectedHandler(event:Event):void {
			var version:ModelVersionProxy = ModelVersionProxy(_selectVersionPopUp.versionsDropDownList.selectedItem);
			PopUpManager.removePopUp(_selectVersionPopUp);
			addNewRecord(version);
		}

		protected function addNewRecord(version:ModelVersionProxy):void {
			var rootEntityName:String = Application.activeRootEntity.name;
			_dataClient.createNewRecord(_newRecordResponder, rootEntityName, version.name);
		}
		
		protected function cancelSelectVersionClickHandler(event:Event):void {
			PopUpManager.removePopUp(_selectVersionPopUp);
		}
		
		protected function createRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var record:RecordProxy = event.result as RecordProxy;
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
			var rootEntityLabel:String = Application.activeRootEntity.getLabelText();
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
				currentKeyValuesFilter = null;
				_view.openFilterPopUpButton.selected = false;
				currentPage = 1;
				loadRecordSummariesCurrentPage();
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
				currentPage = 1;
				loadRecordSummariesCurrentPage();
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
			currentPage = 1;
			loadRecordSummariesCurrentPage();
			closeFilterPopUp();
		}
		
		/**
		 * Loads records summaries for active root entity
		 * */
		protected function loadRecordSummariesHandler(event:UIEvent):void {
			var surveyProjectName:String = Application.activeSurvey.getProjectName();
			var rootEntityLabel:String = Application.activeRootEntity.getLabelText();
			_view.titleLabel.text = Message.get("list.title", [surveyProjectName, rootEntityLabel]);
			updateDataGrid();
			currentPage = 1;
			loadRecordSummariesCurrentPage();
		}
		
		protected function reloadRecordSummariesHandler(event:UIEvent):void {
			loadRecordSummariesCurrentPage();
		}
		
		protected function updateDataGrid():void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var columns:IList = UIBuilder.getRecordSummaryListColumns(rootEntity);
			_view.dataGrid.columns = columns;
		}
		
		protected function loadRecordSummariesCurrentPage():void {
			_view.paginationBar.currentPageText.text = new String(currentPage);
			
			_view.currentState = ListView.INACTIVE_STATE;
			_view.paginationBar.currentState = PaginationBar.LOADING_STATE;
			
			//offset starts from 0
			var offset:int = (currentPage - 1) * MAX_RECORDS_PER_PAGE;
			
			var responder:IResponder = new AsyncResponder(getRecordsSummaryResultHandler, faultHandler);
			var rootEntityName:String = Application.activeRootEntity.name;
			
			_dataClient.loadRecordSummaries(responder, rootEntityName, 
				offset, MAX_RECORDS_PER_PAGE, currentSortFields, currentKeyValuesFilter);
		}
		
		protected function getRecordsSummaryResultHandler(event:ResultEvent, token:Object = null):void {
			var result:Object = event.result;
			var records:ListCollectionView = result.records;
			totalRecords = result.count as int;
			totalPages = Math.ceil(totalRecords / MAX_RECORDS_PER_PAGE);
			
			_view.dataGrid.dataProvider = records;
			_view.dataGrid.setSortedColumns(currentSortFields);
			_view.currentState = ListView.DEFAULT_STATE;
			
			updatePaginationBar();
			
			if(totalRecords == 0 && currentKeyValuesFilter != null) {
				AlertUtil.showMessage("list.filter.noRecordsFound");
			}
		}
		
		protected function deleteRecordResultHandler(event:ResultEvent, token:Object = null):void {
			loadRecordSummariesCurrentPage();
		}

		protected function updatePaginationBar():void {
			var recordsFromPosition:int = ((currentPage - 1) * MAX_RECORDS_PER_PAGE) + 1;
			var recordsToPosition:int = recordsFromPosition + _view.dataGrid.dataProvider.length - 1;
			
			//calculate pagination bar state
			if(totalRecords == 0) {
				//no records found
				_view.paginationBar.currentState = PaginationBar.NO_PAGES_STATE;
			} else if(totalPages == 1) {
				//showing a single page
				_view.paginationBar.currentState = PaginationBar.SINGLE_PAGE_STATE;
			} else if(currentPage == 1) {
				//showing first page
				_view.paginationBar.currentState = PaginationBar.FIRST_PAGE_STATE;
			} else if(currentPage == totalPages) {
				//showing last page
				_view.paginationBar.currentState = PaginationBar.LAST_PAGE_STATE;
			} else {
				//showing a page in the middle
				_view.paginationBar.currentState = PaginationBar.MIDDLE_PAGE_STATE;
			}
		}
		
		/**
		 * Pagination bar events
		 * */
		protected function firstPageClickHandler(event:Event):void {
			currentPage = 1;
			loadRecordSummariesCurrentPage();
		}
		
		protected function previousPageClickHandler(event:Event):void {
			if(currentPage > 1) {
				currentPage --;
				loadRecordSummariesCurrentPage();
			}
		}
		
		protected function nextPageClickHandler(event:Event):void {
			if(currentPage < totalPages) {
				currentPage ++;
				loadRecordSummariesCurrentPage();
			}
		}
		
		protected function lastPageClickHandler(event:Event):void {
			currentPage = totalPages;
			loadRecordSummariesCurrentPage();
		}
		
		protected function goToPageClickHandler(event:Event):void {
			//currentPage = _view.paginationBar.goToPageStepper.value;
			loadRecordSummariesCurrentPage();
		}
		
		protected function dataGridSortChangingHandler(event:GridSortEvent):void {
			//avoid client sorting, perform the sorting by server side
			event.preventDefault();
			var oldSortFields:IList = currentSortFields;
			currentSortFields = RecordSummaryDataGrid.createRecordSummarySortFields(event.newSortFields, oldSortFields);;
			
			loadRecordSummariesCurrentPage();
		}
		
		
	}
}