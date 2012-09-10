package org.openforis.collect.ui.component.input {
	import flash.events.Event;
	
	import mx.binding.utils.BindingUtils;
	import mx.controls.TextInput;
	import mx.core.UIComponent;
	import mx.events.FlexEvent;
	
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.Group;
	import spark.layouts.HorizontalLayout;

	/**
	 * 
	 * @author M. Togna
	 * @author S. Ricci
	 * */
	public class InputField extends Group {
		
		public static const REMARKS_PRESENT_STYLE:String = "remarksPresent";
		public static const READONLY_STYLE:String = "readOnly";
		
		public static const STATE_SAVING:String = "saving";
		public static const STATE_SAVE_COMPLETE:String = "saveComplete";
		public static const STATE_ERROR_SAVING:String = "errorSaving";
		
		private var _attributeDefinition:AttributeDefinitionProxy;
		private var _parentEntity:EntityProxy;
		private var _attribute:AttributeProxy;
		private var _fieldIndex:int = 0;
		private var _presenter:InputFieldPresenter;
		private var _updating:Boolean = false;
		private var _visited:Boolean = false;
		private var _applyChangesOnFocusOut:Boolean = true;
		private var _formatFunction:Function;
		private var _maxChars:int;
		private var _restrict:String;
		private var _changed:Boolean;
		private var _editable:Boolean = false;
		protected var _textInput:UIComponent;
		
		public function InputField() {
			super();
			layout = new HorizontalLayout();
			this.addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
		}
		
		protected function creationCompleteHandler(event:FlexEvent):void {
			initPresenter();
			if(_textInput != null && _textInput is mx.controls.TextInput) {
				BindingUtils.bindProperty(_textInput, "restrictPattern", this, "restrict");
				BindingUtils.bindProperty(_textInput, "maxChars", this, "maxChars");
			}
		}
		
		protected function initPresenter():void {
			this._presenter = new InputFieldPresenter(this);
		}
		
		public static function zeroPaddingFormatFunction(value:String, length:int):String {
			if(StringUtil.isNotBlank(value)) {
				var number:Number = Number(value);
				if(!isNaN(number)) {
					return StringUtil.zeroPad(number, length);
				} else {
					return value;
				}
			} else {
				return "";
			}
		}
		
		/**
		 * returns true if the field is not filled
		 */
		public function isEmpty():Boolean {
			return StringUtil.isBlank(text);
		}
		
		public function getField():FieldProxy {
			if(attribute != null) {
				var f:FieldProxy = attribute.getField(fieldIndex);
				return f;
			} else {
				return null;
			}
		}
		
		public function get text():String {
			return ObjectUtil.getValue(_textInput, "text");
		}
		
		public function set text(value:String):void {
			if(_textInput != null && _textInput.hasOwnProperty("text")) {
				var text:String;
				if(textFormatFunction != null) {
					text = textFormatFunction(value);
				} else {
					text = value;
				}
				_textInput["text"] = text;
			}
		}
		
		[Bindable(event="editablePropertyChange")]
		public function get editable():Boolean {
			return _editable;
		}
		
		public function set editable(value:Boolean):void {
			_editable = value;
			if( textInput != null ) {
				if ( textInput.hasOwnProperty("editable") ) {
					textInput["editable"] = value;
				}
			}
			dispatchEvent(new Event("editablePropertyChange")); 
		}
		
		public function set hasRemarks(value:Boolean):void {
			UIUtil.toggleStyleName(validationStateDisplay, REMARKS_PRESENT_STYLE, value);
		}
		
		public function get textInput():UIComponent {
			return _textInput;
		}
		
		public function set textInput(value:UIComponent):void {
			_textInput = value;
		}
		
		public function get validationStateDisplay():UIComponent {
			return _textInput;
		}
		
		public function get presenter():InputFieldPresenter {
			return _presenter;
		}

		public function set presenter(value:InputFieldPresenter):void {
			_presenter = value;
		}

		[Bindable]
		public function get attribute():AttributeProxy {
			return _attribute;
		}
		
		public function set attribute(value:AttributeProxy):void {
			this._attribute = value;
		}
		
		[Bindable]
		public function get attributeDefinition():AttributeDefinitionProxy {
			return _attributeDefinition;
		}

		public function set attributeDefinition(value:AttributeDefinitionProxy):void {
			_attributeDefinition = value;
		}

		[Bindable]
		public function get parentEntity():EntityProxy {
			return _parentEntity;
		}

		public function set parentEntity(value:EntityProxy):void {
			_parentEntity = value;
		}

		[Bindable]
		public function get fieldIndex():int {
			return _fieldIndex;
		}
		
		public function set fieldIndex(value:int):void {
			_fieldIndex = value;
		}

		public function get applyChangesOnFocusOut():Boolean {
			return _applyChangesOnFocusOut;
		}

		public function set applyChangesOnFocusOut(value:Boolean):void {
			_applyChangesOnFocusOut = value;
		}

		public function get textFormatFunction():Function {
			return _formatFunction;
		}

		public function set textFormatFunction(value:Function):void {
			_formatFunction = value;
		}

		[Bindable]
		public function get updating():Boolean {
			return _updating;
		}
		
		public function set updating(value:Boolean):void {
			_updating = value;
		}
		
		[Bindable]
		public function get visited():Boolean {
			return _visited;
		}
		
		public function set visited(value:Boolean):void {
			_visited = value;
		}
		
		[Bindable]
		public function get maxChars():int {
			return _maxChars;
		}
		
		public function set maxChars(value:int):void {
			_maxChars = value;
		}

		[Bindable]
		public function get restrict():String {
			return _restrict;
		}
		
		public function set restrict(value:String):void {
			_restrict = value;
		}

		[Bindable]
		public function get changed():Boolean {
			return _changed;
		}

		public function set changed(value:Boolean):void {
			_changed = value;
		}
		

}
}