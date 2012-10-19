package org.openforis.collect.model
{
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class LanguageItem
	{
		private var _code:String;
		private var _label:String;
		
		public function LanguageItem(code:String, label:String) {
			_code = code;
			_label = label;
		}
		
		public function get code():String {
			return _code;
		}

		public function set code(value:String):void {
			_code = value;
		}

		public function get label():String {
			return _label;
		}

		public function set label(value:String):void {
			_label = value;
		}


	}
}