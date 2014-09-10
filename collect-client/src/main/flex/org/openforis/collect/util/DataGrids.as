package org.openforis.collect.util
{
	import com.shortybmc.data.parser.CSV;
	
	import flash.net.FileReference;
	import flash.utils.ByteArray;
	
	import mx.collections.IList;
	
	import spark.components.DataGrid;
	import spark.components.gridClasses.GridColumn;

	public class DataGrids {
		
		public static function writeToCSV(dataGrid:DataGrid, outputFileName:String):void {
			//build header
			var header:Array = new Array();
			var columns:Array = dataGrid.columns.toArray();
			for each (var c:GridColumn in columns) {
				header.push(c.headerText);
			}
			
			//build records
			var rows:Array = new Array();
			var dataProvider:IList = dataGrid.dataProvider;
			for each (var item:Object in dataProvider) {
				var row:Array = new Array();
				for each (var col:GridColumn in columns) {
					var value:String = col.itemToLabel(item);
					row.push(value);
				}
				rows.push(row);
			}
			CSVUtils.saveToCsv(header, rows, outputFileName);
		}
	}
}