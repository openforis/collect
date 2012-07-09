package org.openforis.collect.presenter {
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.rpc.AsyncRequest;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.UserClient;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.component.PopUp;
	import org.openforis.collect.ui.component.UserManagementPopUp;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.CheckBox;
	import spark.events.GridSelectionEvent;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class UserManagementPresenter extends PopUpPresenter {
		
		private var _userClient:UserClient;
		private var _loadedUsers:IList;
		
		public function UserManagementPresenter(view:UserManagementPopUp) {
			super(view);
			
			_userClient = ClientFactory.userClient;
			loadAll();
		}
		
		private function get view():UserManagementPopUp {
			return UserManagementPopUp(_view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			view.dataGrid.addEventListener(GridSelectionEvent.SELECTION_CHANGE, dataGridSelectionChangeHandler);
			view.newUserButton.addEventListener(MouseEvent.CLICK, newUserButtonClickHandler);
			view.saveButton.addEventListener(MouseEvent.CLICK, saveButtonClickHandler);
			view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
		}
		
		protected function dataGridSelectionChangeHandler(event:GridSelectionEvent):void {
			var selectedUser:UserProxy = view.dataGrid.selectedItem as UserProxy;
			if ( selectedUser == null ) {
				view.currentState = UserManagementPopUp.STATE_DEFAULT;
			} else {
				view.currentState = UserManagementPopUp.STATE_SELECTED;
				fillForm(selectedUser);
			}
		}
		
		protected function resetForm():void {
			view.enabledCheckBox.selected = true;
			view.nameTextInput.text = "";
			view.passwordTextInput.text = "";
			resetRolesCheckBoxes();
		}
		
		protected function fillForm(user:UserProxy):void {
			view.enabledCheckBox.selected = user.enabled;
			view.nameTextInput.text = user.name;
			view.passwordTextInput.text = "";
			resetRolesCheckBoxes();
			var roles:ListCollectionView = user.roles;
			for each (var role:String in roles) {
				switch ( role ) {
					case UserProxy.ROLE_ENTRY:
						view.roleEntryCheckBox.selected = true;
						break;
					case UserProxy.ROLE_CLEANSING:
						view.roleCleansingCheckBox.selected = true;
						break;
					case UserProxy.ROLE_ANALYSIS:
						view.roleAnalysisCheckBox.selected = true;
						break;
					case UserProxy.ROLE_ADMIN:
						view.roleAdminCheckBox.selected = true;
						break;
				}
			}
		}

		protected function getAllCheckBoxes():Array {
			var checkBoxes:Array = [view.roleEntryCheckBox, view.roleCleansingCheckBox, view.roleAnalysisCheckBox, view.roleAdminCheckBox];
			return checkBoxes;
		}
		
		protected function resetRolesCheckBoxes():void {
			var checkBoxes:Array = getAllCheckBoxes();
			for each (var cb:CheckBox in checkBoxes) {
				 cb.selected = false;
			}
		}
		
		protected function getSelectedRoles():ListCollectionView {
			var roles:ListCollectionView = new ArrayCollection();
			var checkBoxes:Array = getAllCheckBoxes();
			for each (var checkBox:CheckBox in checkBoxes) {
				if ( checkBox.selected ) {
					var role:String = null;
					switch ( checkBox ) {
						case view.roleEntryCheckBox:
							role = UserProxy.ROLE_ENTRY;
							break;
						case view.roleCleansingCheckBox:
							role = UserProxy.ROLE_CLEANSING;
							break;
						case view.roleAnalysisCheckBox:
							role = UserProxy.ROLE_ANALYSIS;
							break;
						case view.roleAdminCheckBox:
							role = UserProxy.ROLE_ADMIN;
							break;
					}
					roles.addItem(role);
				}
			}
			return roles;
		}
		
		protected function validateForm():Boolean {
			//trim fields
			view.nameTextInput.text = StringUtil.trim(view.nameTextInput.text);
			if ( StringUtil.isBlank(view.nameTextInput.text) ) {
				AlertUtil.showError('usersManagement.error.repeatPasswordCorrectly');
				return false;
			}
			//TODO validate password against regexp
			
			if ( view.passwordTextInput.text != view.repeatPasswordTextInput.text ) {
				AlertUtil.showError('usersManagement.error.repeatPasswordCorrectly');
				return false;
			}
			return true;
		}
		
		protected function extractUserFromForm():UserProxy {
			var user:UserProxy = new UserProxy();
			var selectedUser:UserProxy = view.dataGrid.selectedItem as UserProxy;
			if ( selectedUser != null ) {
				user.id = selectedUser.id;
			}
			user.enabled = view.enabledCheckBox.selected;
			user.name = view.nameTextInput.text;
			user.password = view.passwordTextInput.text;
			var roles:ListCollectionView = getSelectedRoles();
			user.roles = roles;
			return user;
		}
		
		protected function loadAll():void {
			_view.currentState = UserManagementPopUp.STATE_LOADING;
			var responder:IResponder = new AsyncResponder(loadAllResultHandler, faultHandler);
			_userClient.loadAll(responder);
		}		
		
		protected function loadAllResultHandler(event:ResultEvent, token:Object = null):void {
			_view.currentState = UserManagementPopUp.STATE_DEFAULT;
			_loadedUsers = event.result as IList;
			
			UserManagementPopUp(_view).dataGrid.dataProvider = _loadedUsers;
		}		

		protected function newUserButtonClickHandler(event:MouseEvent):void {
			_view.currentState = UserManagementPopUp.STATE_NEW;
			resetForm();
		}
		
		protected function saveButtonClickHandler(event:MouseEvent):void {
			var user:UserProxy = extractUserFromForm();
			var responder:IResponder = new AsyncResponder(saveUserResultHandler, faultHandler);
			_userClient.save(responder, user);
		}
		
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			var selectedUser:UserProxy = view.dataGrid.selectedItem as UserProxy;
			if ( selectedUser != null ) {
				AlertUtil.showConfirm("usersManagement.delete.confirm", null, "global.confirm.delete", performDelete, [selectedUser.id]);
			} else {
				AlertUtil.showError("usersManagement.delete.selectUser");
			}
		}
		
		protected function performDelete(id:int):void {
			var responder:IResponder = new AsyncResponder(deleteUserResultHandler, faultHandler);
			_userClient.deleteUser(responder, id);
		}
		
		protected function saveUserResultHandler(event:ResultEvent, token:Object = null):void {
			var savedUser:UserProxy = event.result as UserProxy;
			var selectedUser:UserProxy = view.dataGrid.selectedItem as UserProxy;
			if ( selectedUser != null ) {
				var selectedUserIndex:int = _loadedUsers.getItemIndex(selectedUser);
				_loadedUsers.setItemAt(savedUser, selectedUserIndex);
			} else {
				loadAll();
			}
		}
		
		protected function deleteUserResultHandler(event:ResultEvent, token:Object = null):void {
			loadAll();
		}
	}
}