package org.openforis.collect.ui.component.datagrid
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.ClassFactory;
	
	import org.granite.collections.IMap;
	import org.openforis.collect.model.RecordSummary;
	
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
			return mapFieldLabelFunction(item, gridColumn, "rootEntityKeys", "key_");
		}
		
		public static function recordSummariesCountEntityLabelFunction(item:Object, gridColumn:GridColumn):String {
			return mapFieldLabelFunction(item, gridColumn, "entityCounts", "count_");
		}
		
		private static function mapFieldLabelFunction(item:Object, gridColumn:GridColumn, mapFieldName:String, prefix:String):String {
			var recordSummary:RecordSummary = item as RecordSummary;
			var map:IMap = recordSummary[mapFieldName];
			var dataField:String = gridColumn.dataField;
			if(dataField.indexOf(prefix) == 0) {
				//dataField starts with the correct prefix
				var key:String = dataField.substr(prefix.length);
				return String(map.get(key));
			}
			return null;
		}
	}
}