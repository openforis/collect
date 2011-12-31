package org.openforis.collect.presenter.input {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.utils.setTimeout;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;
	import mx.managers.PopUpManager;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.event.CodeInputFieldEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.idm.model.impl.AbstractValue;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CodeListDialog;
	import org.openforis.collect.ui.component.input.CodeListItem;
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CodeInputFieldPresenter extends InputFieldPresenter {
		
		private var _codeInputField:CodeInputField;
		
		private static var codeListDialog:CodeListDialog;
		
		public function CodeInputFieldPresenter(inputField:CodeInputField = null) {
			super();
			this.inputField = inputField;
		}
		
		override public function set inputField(value:InputField):void {
			super.inputField = value;
			
			_codeInputField = value as CodeInputField;
			
			if(_codeInputField != null) {
				_codeInputField.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
			}
		}
		
		
		override public function set value(value:AbstractValue):void {
			_attributeValue = value;
		}

		protected function openImageClickHandler(event:Event):void {
			if(codeListDialog == null) {
				codeListDialog = new CodeListDialog();
				codeListDialog.addEventListener(CodeInputFieldEvent.CODE_LIST_DIALOG_APPLY_CLICK, dialogApplyClickHandler);
				codeListDialog.addEventListener(CodeInputFieldEvent.CODE_LIST_DIALOG_CANCEL_CLICK, dialogCancelClickHandler);
			}
			codeListDialog.title = Message.get("edit.code.list_dialog_title");
			PopUpManager.addPopUp(codeListDialog, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(codeListDialog);
			
			loadListDialogData();
		}
		
		protected function loadListDialogData():void {
			codeListDialog.currentState = "loading";
			
			//call service method
			setTimeout(loadListDialogDataResultHandler, 1000);
		}
		
		protected function loadListDialogDataResultHandler(event:ResultEvent = null):void {
			//var data:ArrayCollection = event.result as ArrayCollection
			
			//test data
			var data:ArrayCollection = new ArrayCollection();
			for(var index:int = 0; index < 9; index ++) {
				data.addItem(new CodeListItem(
					StringUtil.substitute("00{0}", index + 1),
					StringUtil.substitute("00{0} - Item {0}", index + 1, index + 1), 
					index %3 == 0
				));
			}
			data.addItem(new CodeListItem("999", "Other", true, true, "Test"));
			codeListDialog.dataProvider = data;
			codeListDialog.currentState = "default";
		}

		protected function loadListDialogDataFaultHandler(event:FaultEvent):void {
			
		}

		protected function dialogApplyClickHandler(event:CodeInputFieldEvent):void {
			var data:ArrayCollection = codeListDialog.dataProvider;
			
			//call service method
		}
		
		protected function dialogCancelClickHandler(event:CodeInputFieldEvent):void {
			PopUpManager.removePopUp(codeListDialog);
		}
		
		protected function listDialogApplyResultHandler(event:ResultEvent):void {
			PopUpManager.removePopUp(codeListDialog);
		}
		
		protected function listDialogApplyFaultHandler(event:ResultEvent):void {
			//error: do not close popup
			
		}
	}
}
