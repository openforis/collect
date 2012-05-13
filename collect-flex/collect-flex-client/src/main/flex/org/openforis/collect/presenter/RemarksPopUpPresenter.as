package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.ui.Keyboard;
	
	import mx.collections.IList;
	import mx.events.FlexEvent;
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.RemarksPopUp;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.RadioButton;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RemarksPopUpPresenter {
		
		private var lastSelectedRadioButton:RadioButton = null;
		private var popUpOpened:Boolean = false;
		private var remarksChanged:Boolean = false;
		
		private var view:RemarksPopUp;
		private var _inputField:InputField;
		
		public function RemarksPopUpPresenter() {
		}
		
		protected function initPopUp():void {
			//init event listeners
			view.remarksTextArea.addEventListener(Event.CHANGE, remarksTextAreaChangeHandler);
			view.remarksTextArea.addEventListener(KeyboardEvent.KEY_DOWN, remarksTextAreaKeyDownHandler);
			view.okButton.addEventListener(MouseEvent.CLICK, okButtonClickHandler);
			view.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, mouseDownOutsideHandler);
		}
		
		protected function radioButtonClickHandler(event:MouseEvent):void {
			//allows the deselection of a selected radio button
			var radioButton:RadioButton = event.target as RadioButton;
			if(lastSelectedRadioButton == radioButton) {
				radioButton.selected = false;
				lastSelectedRadioButton = null;
			} else {
				lastSelectedRadioButton = radioButton;	
			}
		}
		
		protected function remarksTextAreaChangeHandler(event:Event):void {
			remarksChanged = true;
		}
		
		protected function remarksTextAreaKeyDownHandler(event:KeyboardEvent):void {
			switch(event.keyCode) {
				case Keyboard.TAB:
					//save();
					break;
				case Keyboard.ESCAPE:
					hidePopUp();
					break;
			}
		}
		
		protected function mouseDownOutsideHandler(event:FlexMouseEvent):void {
			okButtonClickHandler();
		}
		
		public function openPopUp(inputField:InputField, alignToField:Boolean = false, alignmentPoint:Point = null):void {
			remarksChanged = false;
			var firstOpen:Boolean = (view == null);
			if(firstOpen) {
				view = new RemarksPopUp();
			}
			view.editable = Application.activeRecordEditable;
			_inputField = inputField;

			if(! popUpOpened) {
				PopUpManager.addPopUp(view, inputField);
				
				if(firstOpen) {
					//init popup only after rendering to avoid null pointer exception accessing null objects
					initPopUp();
				}
			}
			setValuesInView();

			var alignmentPoint:Point;
			if(alignToField) {
				PopUpUtil.alignToField(view, inputField.validationStateDisplay, PopUpUtil.POSITION_RIGHT, PopUpUtil.VERTICAL_ALIGN_BOTTOM);
			} else if(alignmentPoint) {
				PopUpUtil.alignToPoint(view, alignmentPoint);
			} else {
				//align popup to mouse pointer
				PopUpUtil.alignToMousePoint(view, -10, -10);
			}
			
			popUpOpened = true;
		}
		
		protected function setValuesInView():void {
			var remarks:String = null;
			var attributes:IList = ObjectUtil.getValue(_inputField, "attributes") as IList;
			if(_inputField != null && (_inputField.attribute != null || CollectionUtil.isNotEmpty(attributes))) {
				var a:AttributeProxy = _inputField.attribute != null ? _inputField.attribute: attributes.getItemAt(0) as AttributeProxy;
				var field:FieldProxy = a.getField(_inputField.fieldIndex);
				remarks = field.remarks;
			}
			view.remarksTextArea.text = remarks;
			setFocusOnFirstField();
		}
		
		public function hidePopUp():void {
			PopUpManager.removePopUp(view);
			popUpOpened = false;
			if(_inputField.textInput != null) {
				_inputField.textInput.setFocus();
			}
		}
		
		protected function okButtonClickHandler(event:Event = null):void {
			if(remarksChanged) {
				var remarks:String = StringUtil.trim(view.remarksTextArea.text);
				var nodeEvent:NodeEvent = new NodeEvent(NodeEvent.UPDATE_REMARKS);
				nodeEvent.remarks = remarks;
				if(_inputField.attributeDefinition.multiple && _inputField is CodeInputField) {
					var attrName:String = _inputField.attributeDefinition.name;
					nodeEvent.nodes = _inputField.parentEntity.getChildren(attrName);
				} else {
					nodeEvent.node = _inputField.attribute;
					nodeEvent.fieldIdx = _inputField.fieldIndex;
				}
				EventDispatcherFactory.getEventDispatcher().dispatchEvent(nodeEvent);
			}
			hidePopUp();
		}
		
		protected function cancelHandler(event:Event):void {
			hidePopUp();
		}
		
		protected function reasonBlankGroupCreationCompleteHandler(event:FlexEvent):void {
			setFocusOnFirstField();
		}
		
		public function setFocusOnFirstField():void {
			if(view.remarksGroup != null) {
				view.remarksTextArea.setFocus();
			}
		}
	}
}