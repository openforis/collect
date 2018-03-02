package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import com.adobe.serialization.json.JSON;
	
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	import flash.net.navigateToURL;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.controls.Alert;
	import mx.controls.TextInput;
	import mx.core.FlexGlobals;
	import mx.core.UIComponent;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.MenuEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	import mx.utils.ObjectUtil;
	import mx.utils.URLUtil;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.concurrency.CollectJobStatusPopUp;
	import org.openforis.collect.event.CollectJobEvent;
	import org.openforis.collect.event.PaginationBarEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.model.CollectRecord$State;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.RecordFilterProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.remoting.service.concurrency.proxy.SurveyLockingJobProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.DataExportPopUp;
	import org.openforis.collect.ui.component.DataImportPopUp;
	import org.openforis.collect.ui.component.RecordFilterPopUp;
	import org.openforis.collect.ui.component.SelectVersionPopUp;
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.view.ListView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.concurrency.proxy.JobProxy;
	
	import spark.events.GridSortEvent;


	public class ListPresenter extends AbstractPresenter {
		
		private const EXPORT_DATA_MENU_ITEM:String = Message.get("list.admin.exportData");
		private const IMPORT_DATA_MENU_ITEM:String = Message.get("list.admin.importData");
		private const VALIDATION_REPORT_MENU_ITEM:String = Message.get("list.admin.validationReport");
		private const PROMOTE_ENTRY_RECORDS_MENU_ITEM:String = Message.get("list.admin.promote_entry_records");
		private const PROMOTE_CLEANSING_RECORDS_MENU_ITEM:String = Message.get("list.admin.promote_cleansing_records");
		private const DEMOTE_CLEANSING_RECORDS_MENU_ITEM:String = Message.get("list.admin.demote_cleansing_records");
		private const DEMOTE_ANALYSIS_RECORDS_MENU_ITEM:String = Message.get("list.admin.demote_analysis_records");
		
		private var _dataClient:DataClient;
		
		private var _selectVersionPopUp:SelectVersionPopUp;
		private var _newRecordResponder:IResponder;
		private var _filterPopUp:RecordFilterPopUp;
		
		/**
		 * The current sortField
		 */
		private var currentSortFields:IList;
		
		private var filterEnabled:Boolean = false;
		/**
		 * The current filter applied on record list. 
		 * */
		private var currentFilter:RecordFilterProxy = null;
		
		private var filterErrorAlert:Alert = null;
		
		/**
		 * Max number of records that can be loaded for a single page.
		 */
		private const MAX_RECORDS_PER_PAGE:int = 20;
		
		private var dataGridInitialized:Boolean = false;
		
		public function ListPresenter(view:ListView) {
			super(view);
			this._newRecordResponder = new AsyncResponder(createRecordResultHandler, faultHandler);
			this._dataClient = ClientFactory.dataClient;
		}
		
		private function get view():ListView {
			return ListView(_view);
		}
		
		override public function init():void {
			super.init();
			view.dataGrid.requestedRowCount = MAX_RECORDS_PER_PAGE;
			view.paginationBar.maxRecordsPerPage = MAX_RECORDS_PER_PAGE;
			updateView();
			createAdvancedFunctionMenu();
		}

		override protected function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.LOAD_RECORD_SUMMARIES, loadRecordSummariesHandler);
			eventDispatcher.addEventListener(UIEvent.RELOAD_RECORD_SUMMARIES, reloadRecordSummariesHandler);
			eventDispatcher.addEventListener(CollectJobEvent.COLLECT_JOB_END, jobEndHandler);

			view.backToMainMenuButton.addEventListener(MouseEvent.CLICK, backToMainMenuClickHandler);
			view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			view.viewButton.addEventListener(MouseEvent.CLICK, viewButtonClickHandler);
			view.editButton.addEventListener(MouseEvent.CLICK, editButtonClickHandler);
			view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			view.advancedFunctionsButton.addEventListener(MenuEvent.ITEM_CLICK, advancedFunctionItemClickHandler);
			view.openFilterPopUpButton.addEventListener(MouseEvent.CLICK, openFilterPopUpButtonClickHandler);
			
			view.dataGrid.addEventListener(GridSortEvent.SORT_CHANGING, dataGridSortChangingHandler);
			
			view.paginationBar.addEventListener(PaginationBarEvent.PAGE_CHANGE, summaryPageChangeHandler);
			view.stage.addEventListener(MouseEvent.CLICK, stageClickHandler);
		}
		
		private function jobEndHandler(event:CollectJobEvent):void {
			if (event.job is SurveyLockingJobProxy) {
				reloadRecordSummaries();
			}
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
			if ( Application.user.hasEffectiveRole(UserProxy.ROLE_CLEANSING) ) {
				result.addItem(IMPORT_DATA_MENU_ITEM);
			}
			result.addItem({type: "separator"});
			result.addItem(VALIDATION_REPORT_MENU_ITEM);
			if ( Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN) ) {
				result.addItem({type: "separator"});
				result.addItem(PROMOTE_ENTRY_RECORDS_MENU_ITEM);
				result.addItem(PROMOTE_CLEANSING_RECORDS_MENU_ITEM);
				result.addItem(DEMOTE_CLEANSING_RECORDS_MENU_ITEM);
				result.addItem(DEMOTE_ANALYSIS_RECORDS_MENU_ITEM);
			}
			view.advancedFunctionsButton.dataProvider = result;
		}
		
		protected function advancedFunctionItemClickHandler(event:MenuEvent):void {
			switch ( event.item ) {
			case IMPORT_DATA_MENU_ITEM:
				PopUpUtil.createPopUp(DataImportPopUp, true);
				break;
			case EXPORT_DATA_MENU_ITEM:
				PopUpUtil.createPopUp(DataExportPopUp, true);
				break;
			case VALIDATION_REPORT_MENU_ITEM:
				var url:String = ApplicationConstants.VALIDATION_REPORT_URL;
				var req:URLRequest = new URLRequest(url);
				var params:URLVariables = new URLVariables();
				params.surveyName = Application.activeSurvey.name;
				params.rootEntityId = Application.activeRootEntity.id;
				params.locale = Application.localeString;
				var filter:RecordFilterProxy = (! filterEnabled || currentFilter == null) ? new RecordFilterProxy(): RecordFilterProxy(ObjectUtil.clone(currentFilter));
				if (CollectionUtil.isNotEmpty(filter.keyValues)) {
					params.recordKeys = filter.keyValues;
				}
				if (filter.modifiedSince != null) {
					params.modifiedSince = filter.modifiedSince;
				}
				req.data = params;
				navigateToURL(req, "_new");
				break;
			case PROMOTE_ENTRY_RECORDS_MENU_ITEM:
			case PROMOTE_CLEANSING_RECORDS_MENU_ITEM:
			case DEMOTE_CLEANSING_RECORDS_MENU_ITEM:
			case DEMOTE_ANALYSIS_RECORDS_MENU_ITEM:
				var confirmMessageKey:String = null;
				var initialStep:CollectRecord$Step = null;
				var promote:Boolean;
				switch(event.item) {
					case PROMOTE_ENTRY_RECORDS_MENU_ITEM:
						confirmMessageKey = "list.admin.promote_entry_records.confirm";
						initialStep = CollectRecord$Step.ENTRY;
						promote = true;
						break;
					case PROMOTE_CLEANSING_RECORDS_MENU_ITEM:
						confirmMessageKey = "list.admin.promote_cleansing_records.confirm";
						initialStep = CollectRecord$Step.CLEANSING;
						promote = true;
						break;
					case DEMOTE_CLEANSING_RECORDS_MENU_ITEM:
						confirmMessageKey = "list.admin.demote_cleansing_records.confirm";
						initialStep = CollectRecord$Step.CLEANSING;
						promote = false;
						break;
					case DEMOTE_ANALYSIS_RECORDS_MENU_ITEM:
						confirmMessageKey = "list.admin.demote_analysis_records.confirm";
						initialStep = CollectRecord$Step.ANALYSIS;
						promote = false;
						break;
				}
				var responder:IResponder = new AsyncResponder(function(resultEvent:ResultEvent, token:Object = null):void {
					var job:JobProxy = resultEvent.result as JobProxy;
					CollectJobStatusPopUp.openPopUp(job);
				}, faultHandler);
				AlertUtil.showConfirm(confirmMessageKey, null, null, function():void {
					ClientFactory.dataClient.moveRecords(Application.activeRootEntity.name, initialStep, promote, responder);
					//AlertUtil.showMessage("list.admin.record_process_started");
				}); 
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
			_dataClient.createNewRecord(_newRecordResponder, rootEntityName, versionName, CollectRecord$Step.ENTRY);
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
			PopUpManager.removePopUp(view);
		}
		
		protected function viewButtonClickHandler(event:MouseEvent):void {
			editButtonClickHandler(event);
		}
		
		/**
		 * Edit Button clicked 
		 * */
		protected function editButtonClickHandler(event:MouseEvent):void {
			var selectedRecord:RecordProxy = view.dataGrid.selectedItem as RecordProxy;
			if(selectedRecord == null) {
				AlertUtil.showError("list.error.recordNotSelected");
			} else {
				var uiEvent:UIEvent = new UIEvent(UIEvent.RECORD_SELECTED);
				uiEvent.obj = selectedRecord;
				eventDispatcher.dispatchEvent(uiEvent);
			}
		}
		
		/**
		 * Delete Button clicked 
		 * */
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			var rootEntityLabel:String = Application.activeRootEntity.getInstanceOrHeadingLabelText();
			var selectedRecord:RecordProxy = view.dataGrid.selectedItem as RecordProxy;
			if(selectedRecord == null) {
				AlertUtil.showError("list.error.recordNotSelected");
			} else if ( selectedRecord.step != CollectRecord$Step.ENTRY ) {
				var stepName:String = Message.get("phase." + selectedRecord.step.name);
				if ( Application.user.canReject(selectedRecord) ) {
					AlertUtil.showError("list.error.cannotDelete.rejectBeforeDeletePromotedRecord", [rootEntityLabel, stepName]);
				} else {
					AlertUtil.showError("list.error.cannotDeletePromotedRecord", [rootEntityLabel, stepName]);
				}
			} else if ( ! selectedRecord.unassigned && ! Application.user.isOwner(selectedRecord) &&
					! Application.user.canDeleteNotOwnedRecords ) {
				AlertUtil.showError("list.error.cannotDelete.differentOwner", [selectedRecord.owner.username]);
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
			} else {
				openFilterPopUp();
			}
		}
		
		protected function openFilterPopUp():void {
			var application:DisplayObject = DisplayObject(FlexGlobals.topLevelApplication);
			_filterPopUp = RecordFilterPopUp(PopUpManager.createPopUp(application, RecordFilterPopUp));
			_filterPopUp.addEventListener(CloseEvent.CLOSE, filterPopUpCloseHandler);
			_filterPopUp.addEventListener("apply", filterPopUpApplyHandler);
			
			//focus on first field when the repeater finishes to create the input fields
			_filterPopUp.fieldsRp.addEventListener(FlexEvent.REPEAT_END, function(event:FlexEvent):void {
				var firstTextInput:TextInput = _filterPopUp.textInput[0];
				firstTextInput.setFocus();
				fillFilterPopUpForm();
			});

			var schema:SchemaProxy = Application.activeSurvey.schema;
			var keyAttributeDefinitions:IList = schema.getKeyAttributeDefinitions(Application.activeRootEntity);

			if ( CollectionUtil.isEmpty(keyAttributeDefinitions) ) {
				AlertUtil.showError("list.error.missingRootEntityKeys");
			} else {
				_filterPopUp.fields = keyAttributeDefinitions;
	
				PopUpManager.addPopUp(_filterPopUp, application);
	
				PopUpUtil.alignToField(_filterPopUp, view.openFilterPopUpButton, 
					PopUpUtil.POSITION_BELOW, 
					PopUpUtil.VERTICAL_ALIGN_BOTTOM, 
					PopUpUtil.HORIZONTAL_ALIGN_RIGHT);
			}
		}
		
		protected function stageClickHandler(event:MouseEvent):void {
			if ( _filterPopUp != null 
				&& event.target != view.openFilterPopUpButton
				&& ! hitsTarget(event, _filterPopUp.modifiedSinceDateField.dropdown)
				&& ! hitsTarget(event, _filterPopUp)
				&& ! (filterErrorAlert != null && hitsTarget(event, filterErrorAlert))
			) {
				closeFilterPopUp();
			}
		}
		
		private function hitsTarget(event:MouseEvent, target:UIComponent):Boolean {
			return target != null && target.hitTestPoint( event.stageX, event.stageY );
		}
		
		private function extractFilterFromPopUpForm():RecordFilterProxy {
			var filter:RecordFilterProxy = new RecordFilterProxy();
			filter.keyValues = new ArrayCollection();
			for each (var textInput:TextInput in _filterPopUp.textInput) {
				var key:String = StringUtil.trim(textInput.text).toUpperCase();
				filter.keyValues.addItem(key);
			}
			filter.rootEntityId = Application.activeRootEntity.id;
			filter.modifiedSince = _filterPopUp.modifiedSinceDateField.selectedDate;
			return filter;
		}
		
		private function fillFilterPopUpForm():void {
			_filterPopUp.enabledCheckBox.selected = filterEnabled;
			var filter:RecordFilterProxy;
			if (currentFilter == null) {
				filter = new RecordFilterProxy();
				filter.keyValues = new ArrayCollection();
				CollectionUtil.fill(filter.keyValues, null, _filterPopUp.textInput.length);
			} else {
				filter = currentFilter;
			}
			for(var idx:int = 0; idx < _filterPopUp.textInput.length && idx < filter.keyValues.length; idx++) {
				var textInput:TextInput = _filterPopUp.textInput[idx];
				var keyValue:String = filter.keyValues.getItemAt(idx) as String;
				textInput.text = keyValue;
			}
			_filterPopUp.modifiedSinceDateField.selectedDate = filter.modifiedSince;
		}

		protected function closeFilterPopUp():void {
			if ( _filterPopUp != null ) {
				PopUpManager.removePopUp(_filterPopUp);
				_filterPopUp = null;
				filterErrorAlert = null;
			}
		}
		
		protected function filterPopUpCloseHandler(event:Event = null):void {
			closeFilterPopUp();
		}
		
		protected function filterPopUpApplyHandler(event:Event):void {
			if (_filterPopUp.enabledCheckBox.selected) {
				var filter:RecordFilterProxy = extractFilterFromPopUpForm();
				if (filter.isEmpty()) {
					filterErrorAlert = AlertUtil.showError("list.filter.error.empty_parameters");
				} else {
					filterEnabled = true;
					currentFilter = filter;
					view.openFilterPopUpButton.label = Message.get('list.filterOn');
					loadRecordSummaries(0, view.paginationBar.maxRecordsPerPage);
					closeFilterPopUp();
				}
			} else {
				filterEnabled = false;
				view.openFilterPopUpButton.label = Message.get('list.filterOff');
				loadRecordSummaries(0, view.paginationBar.maxRecordsPerPage);
				closeFilterPopUp();
			}
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
			view.titleLabel.text = Message.get("list.title", [surveyProjectName, rootEntityLabel]);
			initializeDataGrid();
			loadRecordSummaries(0, view.paginationBar.maxRecordsPerPage);
		}
		
		protected function reloadRecordSummariesHandler(event:UIEvent):void {
			reloadRecordSummaries();
		}
		
		protected function reloadRecordSummaries():void {
			loadRecordSummaries(view.paginationBar.offset, view.paginationBar.maxRecordsPerPage);
		}
		
		protected function resetCurrentFilter():void {
			currentFilter = null;
			view.openFilterPopUpButton.label = Message.get('list.filterOff');
		}
		
		protected function initializeDataGrid():void {
			view.dataGrid.dataProvider = null;
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var columns:IList = UIBuilder.getRecordSummaryListColumns(rootEntity);
			view.dataGrid.columns = columns;
			this.dataGridInitialized = true;
		}
		
		protected function loadRecordSummaries(offset:int = 0, recordsPerPage:int = MAX_RECORDS_PER_PAGE):void {
			if ( offset == 0 ) {
				view.paginationBar.showPage(1);
			}
			//view.paginationBar.currentPageText.text = new String(currentPage);
			
			view.currentState = ListView.INACTIVE_STATE;
			view.paginationBar.currentState = PaginationBar.LOADING_STATE;
			
			var filter:RecordFilterProxy = (! filterEnabled || currentFilter == null) ? new RecordFilterProxy(): ObjectUtil.clone(currentFilter) as RecordFilterProxy;
			filter.offset = offset;
			filter.maxNumberOfRecords = recordsPerPage;
			filter.rootEntityId = Application.activeRootEntity == null ? NaN : Application.activeRootEntity.id;
			
			if (! Application.user.canViewNotOwnedRecords) {
				filter.ownerIds = [Application.user.id];
			}
			
			_dataClient.loadRecordSummaries(new AsyncResponder(getRecordsSummaryResultHandler, faultHandler), 
				filter, currentSortFields, Application.localeString);
		}
		
		protected function getRecordsSummaryResultHandler(event:ResultEvent, token:Object = null):void {
			var result:Object = event.result;
			
			if (! dataGridInitialized) {
				initializeDataGrid();
			}
			view.dataGrid.dataProvider = IList(result.records);
			view.dataGrid.setSortedColumns(currentSortFields);
			view.currentState = ListView.DEFAULT_STATE;
			
			view.paginationBar.totalRecords = result.count;
			
			if(result.totalCount == 0 && currentFilter != null) {
				AlertUtil.showMessage("list.filter.noRecordsFound");
			}
		}
		
		protected function deleteRecordResultHandler(event:ResultEvent, token:Object = null):void {
			reloadRecordSummaries();
		}

		protected function summaryPageChangeHandler(event:PaginationBarEvent):void {
			loadRecordSummaries(event.offset, event.recordsPerPage);
		}
		
		protected function dataGridSortChangingHandler(event:GridSortEvent):void {
			//avoid client sorting, perform the sorting by server side
			event.preventDefault();
			var oldSortFields:IList = currentSortFields;
			currentSortFields = RecordSummaryDataGrid.createRecordSummarySortFields(event.newSortFields, oldSortFields);;
			
			reloadRecordSummaries();
		}
		
		private function updateView():void {
			view.addButton.visible = view.addButton.includeInLayout = Application.user.canAddRecords;
			view.editButton.visible = view.editButton.includeInLayout = Application.user.canEditRecords;
			view.deleteButton.visible = view.deleteButton.includeInLayout = Application.user.canDeleteRecords;
			view.viewButton.visible = view.viewButton.includeInLayout = Application.user.canViewAllRecords;
		}
		
	}
}