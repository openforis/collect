package org.openforis.collect.ui.container
{
	import spark.components.SkinnableContainer;
	
	public class TitleBorderBox extends SkinnableContainer {
		
		private var _label : String;
		
		public function TitleBorderBox() {
			super();
		}
		
		public function get label():String {
			return _label;
		}
		
		public function set label(value:String):void {
			_label = value;
		}
	}
}