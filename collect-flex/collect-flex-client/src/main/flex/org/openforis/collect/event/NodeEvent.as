package org.openforis.collect.event {
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.ui.component.input.InputField;

	/**
	 * 
	 * @author M.Togna
	 * @author S.Ricci
	 * 
	 * */
	public class NodeEvent extends UIEvent {
		
		public static const UPDATE_SYMBOL:String = "nodeEventUpdateSymbol";
		public static const UPDATE_REMARKS:String = "nodeEventUpdateRemarks";
		public static const CONFIRM_ERROR:String = "nodeEventConfirmError";
		public static const APPROVE_MISSING:String = "nodeEventApproveMissing";
		
		private var _inputField:InputField;
		private var _parentEntity:EntityProxy;
		private var _nodeName:String;
		private var _nodeId:Number;
		private var _nodeProxy:NodeProxy;
		private var _fieldIdx:Number;
		private var _symbol:FieldSymbol;
		private var _remarks:String;
		
		public function NodeEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}
		
		public function get inputField():InputField {
			return _inputField;
		}
		
		public function set inputField(value:InputField):void {
			_inputField = value;
		}
		
		public function get symbol():FieldSymbol {
			return _symbol;
		}
		
		public function set symbol(value:FieldSymbol):void {
			_symbol = value;
		}

		public function get nodeProxy():NodeProxy {
			return _nodeProxy;
		}

		public function set nodeProxy(value:NodeProxy):void {
			_nodeProxy = value;
		}

		public function get fieldIdx():Number {
			return _fieldIdx;
		}

		public function set fieldIdx(value:Number):void {
			_fieldIdx = value;
		}

		public function get remarks():String {
			return _remarks;
		}

		public function set remarks(value:String):void {
			_remarks = value;
		}

		public function get parentEntity():EntityProxy {
			return _parentEntity;
		}

		public function set parentEntity(value:EntityProxy):void {
			_parentEntity = value;
		}

		public function get nodeName():String {
			return _nodeName;
		}

		public function set nodeName(value:String):void {
			_nodeName = value;
		}

		public function get nodeId():Number {
			return _nodeId;
		}

		public function set nodeId(value:Number):void {
			_nodeId = value;
		}


	}
}