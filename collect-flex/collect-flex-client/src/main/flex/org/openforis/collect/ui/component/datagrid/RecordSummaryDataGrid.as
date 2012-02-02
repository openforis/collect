package org.openforis.collect.ui.component.datagrid
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.ClassFactory;
	
	import org.openforis.collect.model.proxy.RecordProxy;
	
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
		
		public static function dateLabelFunction(item:Object,column:GridColumn):String {
			if(item.hasOwnProperty(column.dataField)) {
				var date:Date = item[column.dataField];
				var dateFormatter:DateTimeFormatter = new DateTimeFormatter();
				dateFormatter.dateTimePattern = "dd-MM-yyyy hh:mm:ss";
				return dateFormatter.format(date);
			} else {
				return null;
			}
		}
		
		public static function recordSummariesKeyLabelFunction(item:Object, gridColumn:GridColumn):String {
			return listFieldLabelFunction(item, gridColumn, "rootEntityKeys", "key");
		}
		
		public static function recordSummariesCountEntityLabelFunction(item:Object, gridColumn:GridColumn):String {
			return listFieldLabelFunction(item, gridColumn, "entityCounts", "count");
		}
		
		public static function numberLabelFunction(item:Object, gridColumn:GridColumn):String {
			var dataField:String = gridColumn.dataField;
			if(item.hasOwnProperty(dataField)) {
				var value:Number = item[dataField];
				if(! isNaN(value)) {
					return value.toString();
				}
			}
			return "";
		}
		
		private static function listFieldLabelFunction(item:Object, gridColumn:GridColumn, listFieldName:String, dataFieldPrefix:String):String {
			var recordSummary:RecordProxy = item as RecordProxy;
			var list:IList = recordSummary[listFieldName];
			var dataField:String = gridColumn.dataField;
			if(dataField.indexOf(dataFieldPrefix) == 0) {
				var posText:String = dataField.substring(dataFieldPrefix.length);
				var position:int = int(posText);
				if(position < list.length) {
					var value:Object = list.getItemAt(position - 1);
					if((value is Number && !isNaN(Number(value))) || value != null) {
						return String(value);
					}
				}
			}
			return "";
		}
	}
}