package org.openforis.collect.event
{
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.ui.component.input.InputField;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class InputFieldEvent extends UIEvent {
		
		public static const FOCUS_IN:String = "inputFieldFocusIn";
		public static const CHANGING:String = "inputFieldChanging";
		public static const OPEN_REMARKS_POPUP:String = "inputFieldOpenRemarksPopup";
		public static const VISITED:String = "visited";
		public static const SET_FOCUS:String = "setFocus";
		
		private var _inputField:InputField;
		private var _parentEntityId:int;
		private var _nodeName:String;
		private var _attributeId:int;
		private var _fieldIdx:int;
		
		public function InputFieldEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}

		public function get inputField():InputField {
			return _inputField;
		}

		public function set inputField(value:InputField):void {
			_inputField = value;
		}

		public function get parentEntityId():int {
			return _parentEntityId;
		}
		
		public function set parentEntityId(value:int):void {
			_parentEntityId = value;
		}
		
		public function get nodeName():String {
			return _nodeName;
		}
		
		public function set nodeName(value:String):void {
			_nodeName = value;
		}
		
		public function get attributeId():int {
			return _attributeId;
		}

		public function set attributeId(value:int):void {
			_attributeId = value;
		}

		public function get fieldIdx():int {
			return _fieldIdx;
		}

		public function set fieldIdx(value:int):void {
			_fieldIdx = value;
		}


	}
}