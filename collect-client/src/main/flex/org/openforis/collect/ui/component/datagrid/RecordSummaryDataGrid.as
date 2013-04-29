package org.openforis.collect.ui.component.datagrid
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.ClassFactory;
	
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.RecordSummarySortField;
	import org.openforis.collect.model.RecordSummarySortField$Sortable;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	
	import spark.collections.SortField;
	import spark.components.DataGrid;
	import spark.components.gridClasses.GridColumn;
	import spark.formatters.DateTimeFormatter;
	
	/**
	 * @author S. Ricci
	 */
	public class RecordSummaryDataGrid extends spark.components.DataGrid {
		
		/**
		 * list of items selected using checkboxes (see SelectRecordColumnHeaderRenderer and SelectRecordColumnItemRenderer) 
		 */ 
		[Bindable]
		public var selectedByCheckBox:ArrayCollection = new ArrayCollection();
		
		public function RecordSummaryDataGrid() {
			super();
			this.itemRenderer = new ClassFactory(RecordSummaryDataGridItemRenderer);
		}
		
		override public function set dataProvider(value:IList):void {
			super.dataProvider = value;
			selectedByCheckBox.removeAll();
		}
		
		public function setAllItemsByChecbox():void {
			selectedByCheckBox.removeAll();
			selectedByCheckBox.addAll(dataProvider);
		}
		
		public static function keyLabelFunction(item:Object, gridColumn:GridColumn):String {
			return listFieldLabelFunction(item, gridColumn, "rootEntityKeys", "key");
		}
		
		public static function entityCountLabelFunction(item:Object, gridColumn:GridColumn):String {
			return listFieldLabelFunction(item, gridColumn, "entityCounts", "count");
		}
		
		public static function errorsCountLabelFunction(item:Object, gridColumn:GridColumn):String {
			var recordSummary:RecordProxy = item as RecordProxy;
			var totalErrors:int = 0;
			switch ( recordSummary.step ) {
				case CollectRecord$Step.ANALYSIS:
				case CollectRecord$Step.CLEANSING:
					totalErrors = recordSummary.errors + recordSummary.missing + recordSummary.skipped;
					break;
				case CollectRecord$Step.ENTRY:
					totalErrors = recordSummary.errors + recordSummary.skipped;
					break;
			}
			return totalErrors.toString();
		}
		
		private static function listFieldLabelFunction(item:Object, gridColumn:GridColumn, listFieldName:String, dataFieldPrefix:String):String {
			var recordSummary:RecordProxy = item as RecordProxy;
			var list:IList = recordSummary[listFieldName];
			var dataField:String = gridColumn.dataField;
			if(dataField.indexOf(dataFieldPrefix) == 0) {
				var posText:String = dataField.substring(dataFieldPrefix.length);
				var position:int = int(posText);
				if(position <= list.length) {
					var value:Object = list.getItemAt(position - 1);
					if(ObjectUtil.isNotNull(value)) {
						return String(value);
					}
				}
			}
			return "";
		}
		
		public static function createRecordSummarySortFields(newSortFields:Array, oldRecordSummarySortFields:IList = null):IList {
			var result:IList = new ArrayCollection();
			for each (var dataGridSortField:SortField in newSortFields) {
				var name:String = dataGridSortField.name;
				var sortField:RecordSummarySortField = createRecordSummarySortField(name);
				var oldSortField:RecordSummarySortField = RecordSummarySortField(CollectionUtil.getItem(oldRecordSummarySortFields, "dataField", name));
				if(oldSortField != null) {
					sortField.descending = ! (oldSortField.descending);
				}
				result.addItem(sortField);
			}
			return result;
		}
		
		private static function createRecordSummarySortField(name:String):RecordSummarySortField {
			var sortField:RecordSummarySortField = new RecordSummarySortField();
			sortField.dataField = name;
			switch(name) {
				case "key1":
					sortField.field = RecordSummarySortField$Sortable.KEY1;
					break;
				case "key2":
					sortField.field = RecordSummarySortField$Sortable.KEY2;
					break;
				case "key3":
					sortField.field = RecordSummarySortField$Sortable.KEY3;
					break;
				case "count1":
					sortField.field = RecordSummarySortField$Sortable.COUNT1;
					break;
				case "count2":
					sortField.field = RecordSummarySortField$Sortable.COUNT2;
					break;
				case "count3":
					sortField.field = RecordSummarySortField$Sortable.COUNT3;
					break;
				case "skipped":
					sortField.field = RecordSummarySortField$Sortable.SKIPPED;
					break;
				case "missing":
					sortField.field = RecordSummarySortField$Sortable.MISSING;
					break;
				case "warnings":
					sortField.field = RecordSummarySortField$Sortable.WARNINGS;
					break;
				case "errors":
					sortField.field = RecordSummarySortField$Sortable.ERRORS;
					break;
				case "creationDate":
					sortField.field = RecordSummarySortField$Sortable.DATE_CREATED;
					break;
				case "modifiedDate":
					sortField.field = RecordSummarySortField$Sortable.DATE_MODIFIED;
					break;
				case "entryComplete":
				case "cleansingComplete":
					sortField.field = RecordSummarySortField$Sortable.STEP;
					break;
			}
			return sortField;
		}
		
		public function getColumn(dataField:String):GridColumn {
			for(var i:int = 0; i < columns.length; i++) {
				var column:GridColumn = GridColumn(columns.getItemAt(i));
				if(column.dataField == dataField) {
					return column;
				}
			}
			return null;
		}
		
		public function setSortedColumns(recordSummarySortFields:IList):void {
			var visibleSortIndicatorIndices:Vector.<int> = new Vector.<int>();
			for each (var sortField:RecordSummarySortField in recordSummarySortFields) {
				var column:GridColumn = getColumn(sortField.dataField);
				if(column != null) {
					column.sortDescending = sortField.descending;
					visibleSortIndicatorIndices.push(column.columnIndex);
				}
			}
			columnHeaderGroup.visibleSortIndicatorIndices = visibleSortIndicatorIndices;
		}
	}
}