package org.openforis.collect.ui.component.input {
	import flash.display.DisplayObject;
	
	import mx.core.UIComponent;
	import mx.events.FlexEvent;
	
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.Group;
	import spark.layouts.HorizontalLayout;

	/**
	 * 
	 * @author Mino Togna
	 * */
	public class InputField extends Group {
		
		protected static const WARN_STYLE:String = "warn";
		protected static const ERROR_STYLE:String = "error";
		protected static const APPROVED_STYLE:String = "approved";
		protected static const NOT_RELEVANT_STYLE:String = "notRelevant";
		protected static const REMARKS_PRESENT_STYLE:String = "remarksPresent";
		
		public static const STATE_SAVING:String = "saving";
		public static const STATE_SAVE_COMPLETE:String = "saveComplete";
		public static const STATE_ERROR_SAVING:String = "errorSaving";
		
		private var _attributeDefinition:AttributeDefinitionProxy;
		private var _parentEntity:EntityProxy;
		private var _attribute:AttributeProxy;
		private var _fieldIndex:int = 0;
		private var _presenter:InputFieldPresenter;
		private var _isInDataGroup:Boolean = false;
		private var _applyChangesOnFocusOut:Boolean = true;
		protected var _textInput:UIComponent;
		
		public function InputField() {
			super();
			layout = new HorizontalLayout();
			this.addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
		}
		
		protected function creationCompleteHandler(event:FlexEvent):void {
			initPresenter();
		}
		
		protected function initPresenter():void {
			this._presenter = new InputFieldPresenter(this);
		}
		
		public function changeSymbol(symbol:AttributeSymbol, remarks:String = null):void {
			presenter.changeSymbol(symbol, remarks);
		}
		
		/**
		 * returns trus if there is not an attribute associated to the field or
		 * the attribute's value is null
		 */
		public function isEmpty():Boolean {
			return attribute == null || 
				(attribute.empty && attribute.getField(fieldIndex).symbol == null);
		}
		
		public function applyChanges():void {
			presenter.applyChanges();
		}
		
		public function hasBlankReasonSpecified():Boolean {
			var reasonBlankSymbols:Array = [AttributeSymbol.BLANK_ON_FORM, 
				AttributeSymbol.DASH_ON_FORM, 
				AttributeSymbol.ILLEGIBLE];
			return attribute != null && (ArrayUtil.isIn(reasonBlankSymbols, attribute.getField(fieldIndex).symbol));  
		}
		
		public function undo():void {
			presenter.undoLastChange();
		}
		
		public function set relevant(value:Boolean):void {
			if(value) {
				UIUtil.removeStyleName(validationStateDisplay, NOT_RELEVANT_STYLE);
			} else {
				UIUtil.addStyleName(validationStateDisplay, NOT_RELEVANT_STYLE);
			}
		}
		
		public function get text():String {
			return ObjectUtil.getValue(_textInput, "text");
		}
		
		public function set text(value:String):void {
			if(_textInput != null && _textInput.hasOwnProperty("text")) {
				_textInput["text"] = value;
			}
		}
		
		public function get textInput():UIComponent {
			return _textInput;
		}
		
		public function set textInput(value:UIComponent):void {
			_textInput = value;
		}
		
		public function set error(value:String):void {
			if(StringUtil.isBlank(value)) {
				UIUtil.removeStyleName(validationStateDisplay, ERROR_STYLE);
			} else {
				UIUtil.addStyleName(validationStateDisplay, ERROR_STYLE);
			}
		}
		
		public function set warning(value:String):void {
			if(StringUtil.isBlank(value)) {
				UIUtil.removeStyleName(validationStateDisplay, WARN_STYLE);
			} else {
				UIUtil.addStyleName(validationStateDisplay, WARN_STYLE);
			}
		}
		
		public function set approved(value:Boolean):void {
			if(value) {
				UIUtil.addStyleName(validationStateDisplay, APPROVED_STYLE);
			} else {
				UIUtil.removeStyleName(validationStateDisplay, APPROVED_STYLE);
			}
		}

		public function set remarks(value:String):void {
			if(StringUtil.isBlank(value)) {
				UIUtil.removeStyleName(validationStateDisplay, REMARKS_PRESENT_STYLE);
			} else {
				UIUtil.addStyleName(validationStateDisplay, REMARKS_PRESENT_STYLE);
			}
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

		public function get isInDataGroup():Boolean {
			return _isInDataGroup;
		}

		public function set isInDataGroup(value:Boolean):void {
			_isInDataGroup = value;
		}

	}
}