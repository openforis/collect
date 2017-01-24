package org.openforis.collect.presenter {
	import flash.events.MouseEvent;
	
	import mx.events.ValidationResultEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.validators.Validator;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.UserSessionClient;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.manager.OperationResult;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.component.user.ChangePasswordPopUp;
	import org.openforis.collect.util.AlertUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class ChangePasswordPresenter extends PopUpPresenter {
		
		private var _userSessionClient:UserSessionClient;
		
		public function ChangePasswordPresenter(view:ChangePasswordPopUp) {
			super(view);
			
			_userSessionClient = ClientFactory.userSessionClient;
			
			fillForm();
		}
		
		private function get view():ChangePasswordPopUp {
			return ChangePasswordPopUp(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.saveButton.addEventListener(MouseEvent.CLICK, saveButtonClickHandler);
		}
		
		protected function resetForm():void {
		}
		
		protected function fillForm():void {
			view.nameTextInput.text = Application.user.name;
		}

		protected function validateForm():Boolean {
			var result:ValidationResultEvent;
			var validators:Array = [view.fOldPasswordV, view.fPasswordV, view.fRepeatedPasswordV];
			var failed:Boolean = false;
			for each (var validator:Validator in validators) {
				result = validator.validate();
				if ( result != null && result.type==ValidationResultEvent.INVALID ) {
					failed = true;
				}
			}
			if ( failed ) {
				AlertUtil.showError("usersManagement.error.errorsInForm");
			}
			return ! failed;
		}
		
		protected function saveButtonClickHandler(event:MouseEvent):void {
			if ( validateForm() ) {
				var responder:IResponder = new AsyncResponder(changePasswordResultHandler, faultHandler);
				var oldPassword:String = view.oldPasswordTextInput.text;
				var newPassword:String = view.passwordTextInput.text;
				_userSessionClient.changePassword(responder, oldPassword, newPassword);
			}
		}
		
		protected function changePasswordResultHandler(event:ResultEvent, token:Object = null):void {
			var result:OperationResult = event.result as OperationResult;
			if (result.success) {
				PopUpManager.removePopUp(view);
				AlertUtil.showMessage("usersManagement.message.passwordChanged");
			} else {
				switch(result.errorCode) {
				case "WRONG_PASSWORD":
					AlertUtil.showError("usersManagement.error.wrongOldPassword");
					break;
				case "INVALID_PASSWORD":
					AlertUtil.showError("usersManagement.error.invalidPassword");
					break;
				default:
					AlertUtil.showError(result.errorMessage);
				}
			}
		}
		
	}
}