package org.openforis.collect.util
{
	import com.shortybmc.data.parser.CSV;
	
	import flash.net.FileReference;
	import flash.utils.ByteArray;

	public class CSVUtils
	{
		public static function saveToCsv(header:Array, rows:Array, outputFileName:String):void {
			var fieldEnclosureToken:String = '"';
			
			var csv:CSV = new CSV();
			csv.embededHeader = false;
			
			//build header
			var csvHeader:Array = new Array();
			for each (var h:String in header) {
				csvHeader.push(CSVUtils.encloseCsvValue(h, fieldEnclosureToken));
			}
			csv.header = csvHeader;
			
			//build records
			for each (var row:Array in rows) {
				var recordSet:Array = new Array();
				for each (var v:String in row) {
					recordSet.push(CSVUtils.encloseCsvValue(v, fieldEnclosureToken));
				}
				csv.addRecordSet(recordSet);
			}
			csv.encode();
			
			//download generated file
			var data:ByteArray = new ByteArray();
			data.writeUTFBytes(csv.data);
			
			var fileReference:FileReference = new FileReference();
			fileReference.save(data, outputFileName);
		}
		
		public static function escapeCsv(value:String, fieldEnclosureToken:String = '"'):String {
			var regExp:RegExp = new RegExp(fieldEnclosureToken, "g");
			var escaped:String = value.replace(regExp, fieldEnclosureToken+fieldEnclosureToken);
			return escaped;
		}
		
		public static function encloseCsvValue(value:String, enclosureToken:String = '"'):String {
			var escaped:String = value == null ? "": CSVUtils.escapeCsv(value, enclosureToken);
			var enclosed:String = enclosureToken + escaped + enclosureToken;
			return enclosed;
		}
	}
}