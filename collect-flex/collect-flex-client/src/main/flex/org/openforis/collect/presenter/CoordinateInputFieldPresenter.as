package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.ui.component.input.CoordinateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CoordinateInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:CoordinateInputField;
		
		public function CoordinateInputFieldPresenter(inputField:CoordinateInputField = null) {
			_view = inputField;
			super(inputField);

			_view.srsDropDownList.labelFunction = srsDropDownLabelFunction;
			//TODO binding
			_view.srsDropDownList.dataProvider = Application.activeSurvey.spatialReferenceSystems;
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.srsDropDownList.addEventListener(Event.CHANGE, srsDropDownChangeHandler);
			_view.srsDropDownList.addEventListener(DropdownEvent.CLOSE, srsDropDownCloseHandler);
		}
		
		override protected function updateView():void {
			//this._inputField.attribute = attribute;
			/*
			this._view.srsDropDown.selectedItem = value.text1;
			this._view.xTextInput.text = value.text2;
			this._view.yTextInput.text = value.text3;
			this._view.remarks = value.remarks;
			this._view.approved = value.approved;
			*/
		}
		
		protected function createValue():* {
			var srs:SpatialReferenceSystemProxy = _view.srsDropDownList.selectedItem as SpatialReferenceSystemProxy;
			var srsId:String = "";
			if(srs != null) {
				srsId = srs.id;
			}
			var x:String = _view.xTextInput.text;
			var y:String = _view.yTextInput.text;
			
			var result:String = "SRID=" + srsId + ";POINT(" + x + " " + y + ")";
			return result;
		}
		
		protected function srsDropDownLabelFunction(item:Object):String {
			if(item != null) {
				return item.label;
			} else {
				return null;
			}	
		}
		
		protected function srsDropDownChangeHandler(event:Event):void {
			applyChanges();
		}
		
		protected function srsDropDownCloseHandler(event:Event):void {
		}
		
	}
}
