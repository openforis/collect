package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.FlexGlobals;
	import mx.events.StateChangeEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeLabelProxy;
	import org.openforis.collect.ui.component.AddNewRecordPopUp;
	import org.openforis.collect.ui.component.datagrid.SelectRecordColumn;
	import org.openforis.collect.ui.view.ListView;
	
	import spark.components.gridClasses.GridColumn;


	public class ListPresenter extends AbstractPresenter {
		
		private var _view:ListView;
		private var _dataClient:DataClient;
		
		private var _newRecordPopUp:AddNewRecordPopUp;
		
		private var totalRecords:int;
		private var currentPage:int;
		private var totalPages:int;
		
		private const MAX_RECORDS_PER_PAGE:int = 10;
		
		public function ListPresenter(view:ListView) {
			this._view = view;
			this._dataClient = ClientFactory.dataClient;
			super();
		}
	

		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);

			this._view.newRecordButton.addEventListener(MouseEvent.CLICK, newRecordButtonClickHandler);
			this._view.addEventListener(StateChangeEvent.CURRENT_STATE_CHANGE, currentStateChangeHandler);
			this._view.paginationBar.previousPageButton.addEventListener(MouseEvent.CLICK, previousPageClickHandler);
			this._view.paginationBar.nextPageButton.addEventListener(MouseEvent.CLICK, nextPageClickHandler);
			this._view.paginationBar.goToPageButton.addEventListener(MouseEvent.CLICK, goToPageClickHandler);

			
			//workaround: updateDataGrid when the presenter is created
			if(Application.activeRootEntity != null) {
				updateDataGrid(Application.activeRootEntity);
				
				//load records summary
				loadRecordsSummary();
			}

		}
		
	
		protected function newRecordButtonClickHandler(event:MouseEvent):void {
			if(_newRecordPopUp == null) {
				_newRecordPopUp = new AddNewRecordPopUp();
				
			}
			PopUpManager.addPopUp(_newRecordPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_newRecordPopUp);
		}
		
		protected function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntity:EntityDefinitionProxy = event.obj as EntityDefinitionProxy;
			updateDataGrid(rootEntity);
		}
		
		protected function currentStateChangeHandler(event:StateChangeEvent):void {
			var selectionColumn:SelectRecordColumn = null;
			if(_view.dataGrid.columns.length > 0) {
				selectionColumn = _view.dataGrid.columns.getItemAt(0) as SelectRecordColumn;
				if(selectionColumn != null) {
					selectionColumn.visible = (event.newState == ListView.SELECTION_STATE);
				}
			}
		}
		
		protected function updateDataGrid(rootEntity:EntityDefinitionProxy):void {
			var columns:IList = new ArrayList();
			var column:GridColumn;
			//selection column
			column = new SelectRecordColumn();
			columns.addItem(column);
			var firstLevelDefs:ListCollectionView = rootEntity.childDefinitions;
			for each(var nodeDef:NodeDefinitionProxy in firstLevelDefs) {
				if(nodeDef is EntityDefinitionProxy) {
					column = new GridColumn();
					column.headerText = NodeLabelProxy(nodeDef.labels.getItemAt(0)).text + " Count";
					column.dataField = nodeDef.name + "Count";
					columns.addItem(column);
				}
			}
			
			//errors count column
			column = new GridColumn();
			column.headerText = Message.get("list.errorCount");
			column.dataField = "errorCount";
			columns.addItem(column);
			//warnings count column
			column = new GridColumn();
			column.headerText = Message.get("list.warningCount");
			column.dataField = "warningCount";
			columns.addItem(column);
			//creation date column
			column = new GridColumn();
			column.headerText = Message.get("list.creationDate");
			column.dataField = "creationDate";
			columns.addItem(column);
			//date modified column
			column = new GridColumn();
			column.headerText = Message.get("list.dateModified");
			column.dataField = "dateModified";
			columns.addItem(column);

			_view.dataGrid.columns = columns;
		}
		
		protected function loadRecordsSummary():void {
			_dataClient.getCountRecords(new AsyncResponder(getCountRecordsResultHandler, faultHandler));
		}
		
		protected function getCountRecordsResultHandler(event:ResultEvent, token:Object = null):void {
			totalRecords = event.result as int;
			currentPage = 1;
			totalPages = Math.ceil(totalRecords / MAX_RECORDS_PER_PAGE);
			_view.paginationBar.goToPageStepper.minimum = 1;
			_view.paginationBar.goToPageStepper.maximum = totalPages;
			_view.paginationBar.totalRecordsText.text = String(totalRecords);
			loadRecordsSummaryCurrentPage();
		}
		
		protected function loadRecordsSummaryCurrentPage():void {
			var fromIndex:int = (currentPage - 1) * MAX_RECORDS_PER_PAGE + 1;
			var toIndex:int = (currentPage * MAX_RECORDS_PER_PAGE);
			_dataClient.getRecordsSummary(new AsyncResponder(getRecordsSummaryResultHandler, faultHandler), 
				Application.activeRootEntity.id, fromIndex, toIndex, null);
		}
		
		protected function getRecordsSummaryResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			_view.dataGrid.dataProvider = result;
			_view.paginationBar.recordsFromText.text = String(((currentPage - 1) * MAX_RECORDS_PER_PAGE) + 1);
			_view.paginationBar.recordsToText.text = String(((currentPage - 1) * MAX_RECORDS_PER_PAGE) + result.length);
		}
		
		protected function nextPageClickHandler(event:Event):void {
			if(currentPage < totalPages) {
				currentPage ++;
				loadRecordsSummaryCurrentPage();
			}
		}
		
		protected function previousPageClickHandler(event:Event):void {
			if(currentPage > 1) {
				currentPage --;
				loadRecordsSummaryCurrentPage();
			}
		}
		
		protected function goToPageClickHandler(event:Event):void {
			currentPage = _view.paginationBar.goToPageStepper.value;
			loadRecordsSummaryCurrentPage();
		}
		
		
	}
}