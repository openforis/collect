package org.openforis.collect.client
{
	import mx.collections.IList;
	
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.ui.component.input.InputField;
	
	/**
	 * @author S. Ricci
	 */
	public class UpdateRequestToken
	{
		public static const UPDATE_SYMBOL:String = "symbol";
		public static const UPDATE_REMARKS:String = "remarks";
		public static const UPDATE_VALUE:String = "value";
		public static const CONFIRM_ERROR:String = "confirmError";
		public static const APPROVE_MISSING:String = "approveMissing";
		
		private var _type:String;
		private var _updatedFields:IList;
		private var _value:Object;
		private var _symbol:FieldSymbol;
		private var _remarks:String;
		
		public function UpdateRequestToken(type:String = "value") {
			_type = type;
		}
		
		public function get type():String {
			return _type;
		}
		
		public function set type(value:String):void {
			_type = value;
		}

		public function get value():Object {
			return _value;
		}
		
		public function set value(value:Object):void {
			_value = value;
		}

		public function get symbol():FieldSymbol {
			return _symbol;
		}

		public function set symbol(value:FieldSymbol):void {
			_symbol = value;
		}

		public function get remarks():String {
			return _remarks;
		}

		public function set remarks(value:String):void {
			_remarks = value;
		}

		public function get updatedFields():IList {
			return _updatedFields;
		}

		public function set updatedFields(value:IList):void {
			_updatedFields = value;
		}


	}
}