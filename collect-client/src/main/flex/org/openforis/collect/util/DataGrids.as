package org.openforis.collect.util
{
	import com.shortybmc.data.parser.CSV;
	
	import flash.net.FileReference;
	import flash.utils.ByteArray;
	
	import mx.collections.IList;
	
	import spark.components.DataGrid;
	import spark.components.gridClasses.GridColumn;

	public class DataGrids {
		
		public static function writeToCSV(dataGrid:DataGrid):void {
			var csv:CSV = new CSV();
			csv.embededHeader = false;
			
			//build header
			var header:Array = new Array();
			var columns:Array = dataGrid.columns.toArray();
			for each (var c:GridColumn in columns) {
				header.push(c.headerText);
			}
			csv.header = header;
			
			//build records
			var dataProvider:IList = dataGrid.dataProvider;
			for each (var item:Object in dataProvider) {
				var values:Array = new Array();
				for each (var col:GridColumn in columns) {
					var value:String = col.itemToLabel(item);
					values.push(value);
				}
				csv.addRecordSet(values);
			}
			csv.encode();

			//download generated file
			var data:ByteArray = new ByteArray();
			data.writeUTFBytes(csv.data);
			
			var fileReference:FileReference = new FileReference();
			fileReference.save(data, "errors.csv");
		}
	}
}