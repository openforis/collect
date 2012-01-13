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
	import mx.controls.List;
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
	import spark.formatters.DateTimeFormatter;


	public class ListPresenter extends AbstractPresenter {
		
		private var _view:ListView;
		private var _dataClient:DataClient;
		
		private var _newRecordPopUp:AddNewRecordPopUp;
		
		/**
		 * The total number of records.
		 */
		private var totalRecords:int;
		/**
		 * The total pages of the pagination.
		 */
		private var totalPages:int;
		/**
		 * the current page of the pagination.
		 * It starts from 1 
		 */
		private var currentPage:int;
		/**
		 * Max number of records that can be loaded for a single page.
		 */
		private const MAX_RECORDS_PER_PAGE:int = 10;
		
		public function ListPresenter(view:ListView) {
			this._view = view;
			this._dataClient = ClientFactory.dataClient;
			super();
		}

		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.LOAD_RECORD_SUMMARIES, loadRecordSummariesHandler);

			this._view.newRecordButton.addEventListener(MouseEvent.CLICK, newRecordButtonClickHandler);
			this._view.paginationBar.previousPageButton.addEventListener(MouseEvent.CLICK, previousPageClickHandler);
			this._view.paginationBar.nextPageButton.addEventListener(MouseEvent.CLICK, nextPageClickHandler);
			this._view.paginationBar.goToPageButton.addEventListener(MouseEvent.CLICK, goToPageClickHandler);
		}
	
		/**
		 * New Record Button clicked 
		 * */
		protected function newRecordButtonClickHandler(event:MouseEvent):void {
			if(_newRecordPopUp == null) {
				_newRecordPopUp = new AddNewRecordPopUp();
				
			}
			PopUpManager.addPopUp(_newRecordPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_newRecordPopUp);
		}
		
		/**
		 * Loads records summaries for active root entity
		 * */
		protected function loadRecordSummariesHandler(event:UIEvent):void {
			updateDataGrid();
			currentPage = 1;
			loadRecordSummariesCurrentPage();
		}
		
		protected function updateDataGrid():void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var columns:IList = new ArrayList();
			var column:GridColumn;
			//TO DO id columns
			/*
			column = new GridColumn();
			column.headerText = "ID";
			column.dataField = "id";
			columns.addItem(column);
			*/
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
			column.labelFunction = dateLabelFunction;
			columns.addItem(column);
			//date modified column
			column = new GridColumn();
			column.headerText = Message.get("list.dateModified");
			column.dataField = "dateModified";
			column.labelFunction = dateLabelFunction;
			columns.addItem(column);

			_view.dataGrid.columns = columns;
		}
		
		protected function loadRecordSummariesCurrentPage():void {
			//offset starts from 0
			var offset:int = (currentPage - 1) * MAX_RECORDS_PER_PAGE;
			
			_dataClient.getRecordSummaries(new AsyncResponder(getRecordsSummaryResultHandler, faultHandler), 
				Application.activeRootEntity.name, offset, MAX_RECORDS_PER_PAGE, null, null);
		}
		
		protected function getRecordsSummaryResultHandler(event:ResultEvent, token:Object = null):void {
			var result:Object = event.result;
			var records:ListCollectionView = result.records;
			totalRecords = result.count as int;
			totalPages = Math.ceil(totalRecords / MAX_RECORDS_PER_PAGE);
			
			//update pagination bar
			_view.paginationBar.goToPageStepper.minimum = 1;
			_view.paginationBar.goToPageStepper.maximum = totalPages;
			_view.paginationBar.totalRecordsText.text = String(totalRecords);
			
			//records from position is the indexFrom value + 1
			var recordsFromPosition:int = ((currentPage - 1) * MAX_RECORDS_PER_PAGE) + 1;
			var recordsToPosition:int = recordsFromPosition + records.length - 1;
			_view.paginationBar.recordsFromText.text = String(recordsFromPosition);
			_view.paginationBar.recordsToText.text = String(recordsToPosition);
			
			//update datagrid dataprovider
			_view.dataGrid.dataProvider = records;
		}
		
		
		/**
		 * Pagination bar events
		 * */
		protected function nextPageClickHandler(event:Event):void {
			if(currentPage < totalPages) {
				currentPage ++;
				loadRecordSummariesCurrentPage();
			}
		}
		
		protected function previousPageClickHandler(event:Event):void {
			if(currentPage > 1) {
				currentPage --;
				loadRecordSummariesCurrentPage();
			}
		}
		
		protected function goToPageClickHandler(event:Event):void {
			currentPage = _view.paginationBar.goToPageStepper.value;
			loadRecordSummariesCurrentPage();
		}
		
		private function dateLabelFunction(item:Object,column:GridColumn):String {
			if(item.hasOwnProperty(column.dataField)) {
				var date:Date = item[column.dataField];
				var dateFormatter:DateTimeFormatter = new DateTimeFormatter();
				dateFormatter.dateTimePattern = "dd-MM-yyyy hh:mm:ss";
				return dateFormatter.format(date);
			} else {
				return null;
			}
		}
		
	}
}