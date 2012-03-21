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
		public static const TYPE_UPDATE_SYMBOL:String = "symbol";
		public static const TYPE_UPDATE_REMARKS:String = "remarks";
		public static const TYPE_UPDATE_VALUE:String = "value";
		public static const TYPE_CONFIRM_ERROR:String = "value";
		
		private var _type:String;
		//private var _inputField:InputField;
		private var _updatedFields:IList;
		private var _value:Object;
		private var _symbol:FieldSymbol;
		private var _remarks:String;
		
		public function UpdateRequestToken(type:String = "value") {
			//, inputField:InputField = null	
			_type = type;
			//_inputField = inputField;
		}
		
		public function get type():String {
			return _type;
		}
		
		public function set type(value:String):void {
			_type = value;
		}

		/*public function get inputField():InputField {
			return _inputField;
		}

		public function set inputField(value:InputField):void {
			_inputField = value;
		}*/

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