package org.openforis.collect.presenter {
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.controls.CheckBox;
	import mx.events.FlexEvent;
	import mx.events.ValidationResultEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.validators.Validator;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.UserClient;
	import org.openforis.collect.event.UserManagementEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.UserPerRoleWrapper;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.component.user.UserManagementPopUp;
	import org.openforis.collect.ui.component.user.UserPerRoleContainer;
	import org.openforis.collect.ui.component.user.UsersListContainer;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.events.GridSelectionEvent;
	import spark.events.IndexChangeEvent;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class UserManagementPresenter extends PopUpPresenter {
		
		public static const ADMIN_USER_NAME:String = "admin";
		
		private var _userClient:UserClient;
		private var _loadedUsers:IList;
		private var _usersPerRoleMap:Array;
		
		public function UserManagementPresenter(view:UserManagementPopUp) {
			super(view);
			
			_userClient = ClientFactory.userClient;
			_usersPerRoleMap = new Array();
		}
		
		override public function init():void {
			super.init();
			initRoles();
			loadAll();
		}
		
		private function get view():UserManagementPopUp {
			return UserManagementPopUp(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.tabBar.addEventListener(IndexChangeEvent.CHANGE, tabBarIndexChangeHandler);
			view.usersListContainer.dataGrid.addEventListener(GridSelectionEvent.SELECTION_CHANGE, dataGridSelectionChangeHandler);
			view.usersListContainer.newUserButton.addEventListener(MouseEvent.CLICK, newUserButtonClickHandler);
			view.usersListContainer.userDetailsBox.addEventListener(FlexEvent.CREATION_COMPLETE, userDetailsBoxCreationCompleteHandler);
			
			view.userPerRoleContainer.rolesList.addEventListener(IndexChangeEvent.CHANGE, roleChangeHandler);
			
			eventDispatcher.addEventListener(UserManagementEvent.USER_PER_ROLE_SELECTED, userPerRoleSelectedHandler);
		}
		
		protected function userDetailsBoxCreationCompleteHandler(event:FlexEvent):void {
			view.usersListContainer.saveButton.addEventListener(MouseEvent.CLICK, saveButtonClickHandler);
			view.usersListContainer.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
		}
		
		protected function tabBarIndexChangeHandler(event:IndexChangeEvent):void {
			loadAll();
		}
		
		protected function userPerRoleSelectedHandler(event:UserManagementEvent):void {
			var user:UserProxy = event.user;
			var role:String = event.role;
			var selected:Boolean = event.selected;
			
			if (validateUserPerRoleSelection(user, role, selected)) {
				if ( selected ) {
					user.addRole(role);
				} else {
					user.removeRole(role);
				}
				var responder:IResponder = new AsyncResponder(saveRolePerUserResultHandler, faultHandler);
				_userClient.save(responder, user);
			} else {
				//undo selection
				if (event.source.hasOwnProperty("selected")) {
					event.source["selected"] = !selected;
				}
			}
		}
		
		private function validateUserPerRoleSelection(user:UserProxy, role:String, selected:Boolean):Boolean {
			if (selected) {
				return true;
			} else {
				if(user.roles.length > 1) {
					return true;
				} else {
					AlertUtil.showError('usersManagement.error.userMustHaveAtLeastOneRole', [user.name]);
					return false;
				}
			}
		}
		
		protected function initRoles():void {
			view.userPerRoleContainer.rolesList.dataProvider = new ArrayCollection(UserProxy.ROLES);
		}
		
		protected function dataGridSelectionChangeHandler(event:GridSelectionEvent):void {
			var selectedUser:UserProxy = view.usersListContainer.dataGrid.selectedItem as UserProxy;
			/* workaround flex bug on inherited states: reset currentState before changing from child to parent state */
			view.usersListContainer.currentState = UsersListContainer.STATE_DEFAULT; 
			if ( selectedUser != null ) {
				if ( selectedUser.name == ADMIN_USER_NAME ) {
					view.usersListContainer.currentState = UsersListContainer.STATE_ADMIN_SELECTED;
				} else {
					view.usersListContainer.currentState = UsersListContainer.STATE_SELECTED;
				}
				fillForm(selectedUser);
			}
		}
		
		protected function roleChangeHandler(event:IndexChangeEvent):void {
			var role:String = null;
			var newIndex:int = event.newIndex;
			if ( newIndex >= 0 ) {
				role = UserProxy.ROLES[newIndex];
				var selectableUsers:IList = new ArrayCollection();
				for each (var user:UserProxy in _loadedUsers) {
					var hasRole:Boolean = user.hasRole(role);
					var item:UserPerRoleWrapper = new UserPerRoleWrapper(user, role, hasRole);
					selectableUsers.addItem(item);
				}
				view.userPerRoleContainer.currentState = UserPerRoleContainer.STATE_ROLE_SELECTED;
				view.userPerRoleContainer.usersDataGroup.dataProvider = selectableUsers;
			} else {
				view.userPerRoleContainer.currentState = UserPerRoleContainer.STATE_DEFAULT;
			}
		}
		
		protected function resetForm():void {
			view.usersListContainer.enabledCheckBox.selected = true;
			view.usersListContainer.nameTextInput.text = "";
			view.usersListContainer.nameTextInput.errorString = "";
			view.usersListContainer.passwordTextInput.text = "";
			view.usersListContainer.passwordTextInput.errorString = "";
			view.usersListContainer.repeatPasswordTextInput.text = "";
			view.usersListContainer.repeatPasswordTextInput.errorString = "";
			resetRolesCheckBoxes();
		}
		
		protected function fillForm(user:UserProxy):void {
			view.usersListContainer.enabledCheckBox.selected = user.enabled;
			view.usersListContainer.nameTextInput.text = user.name;
			view.usersListContainer.passwordTextInput.text = "";
			view.usersListContainer.repeatPasswordTextInput.text = "";
			resetRolesCheckBoxes();
			var roles:ListCollectionView = user.roles;
			for each (var role:String in roles) {
				switch ( role ) {
					case UserProxy.ROLE_VIEW:
						view.usersListContainer.roleViewCheckBox.selected = true;
						break;
					case UserProxy.ROLE_ENTRY:
						view.usersListContainer.roleEntryCheckBox.selected = true;
						break;
					case UserProxy.ROLE_ENTRY_LIMITED:
						view.usersListContainer.roleEntryLimitedCheckBox.selected = true;
						break;
					case UserProxy.ROLE_CLEANSING:
						view.usersListContainer.roleCleansingCheckBox.selected = true;
						break;
					case UserProxy.ROLE_ANALYSIS:
						view.usersListContainer.roleAnalysisCheckBox.selected = true;
						break;
					case UserProxy.ROLE_ADMIN:
						view.usersListContainer.roleAdminCheckBox.selected = true;
						break;
				}
			}
		}

		protected function getAllCheckBoxes():Array {
			var checkBoxes:Array = [
				view.usersListContainer.roleViewCheckBox,
				view.usersListContainer.roleEntryCheckBox,
				view.usersListContainer.roleEntryLimitedCheckBox,  
				view.usersListContainer.roleCleansingCheckBox, 
				view.usersListContainer.roleAnalysisCheckBox, 
				view.usersListContainer.roleAdminCheckBox
			];
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
						case view.usersListContainer.roleViewCheckBox:
							role = UserProxy.ROLE_VIEW;
							break;
						case view.usersListContainer.roleEntryCheckBox:
							role = UserProxy.ROLE_ENTRY;
							break;
						case view.usersListContainer.roleEntryLimitedCheckBox:
							role = UserProxy.ROLE_ENTRY_LIMITED;
							break;
						case view.usersListContainer.roleCleansingCheckBox:
							role = UserProxy.ROLE_CLEANSING;
							break;
						case view.usersListContainer.roleAnalysisCheckBox:
							role = UserProxy.ROLE_ANALYSIS;
							break;
						case view.usersListContainer.roleAdminCheckBox:
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
			var name:String = view.usersListContainer.nameTextInput.text;
			name = StringUtil.trim(name);
			view.usersListContainer.nameTextInput.text = name;
			var selectedUser:UserProxy = view.usersListContainer.dataGrid.selectedItem as UserProxy;
			if ( (selectedUser == null || selectedUser.name != name) && existsUsername(name) ) {
				AlertUtil.showError("usersManagement.error.duplicatedUsername");
				return false;
			}
			var result:ValidationResultEvent;
			var validators:Array = [view.usersListContainer.fNameV, view.usersListContainer.fPasswordV, view.usersListContainer.fRepeatedPasswordV];
			var failed:Boolean = false;
			for each (var validator:Validator in validators) {
				result = validator.validate();
				if ( result != null && result.type==ValidationResultEvent.INVALID ) {
					failed = true;
				}
			}
			var roles:ListCollectionView = getSelectedRoles();
			if ( roles.length == 0 ) {
				AlertUtil.showError('usersManagement.error.noRolesSelected');
				return false;
			}
			if ( failed ) {
				AlertUtil.showError("usersManagement.error.errorsInForm");
				return false;
			}
			return true;
		}
		
		protected function existsUsername(username:String):Boolean {
			var result:Boolean = CollectionUtil.containsItemWith(_loadedUsers, "name", username);
			return result;
		}
		
		protected function extractUserFromForm():UserProxy {
			var user:UserProxy = new UserProxy();
			var selectedUser:UserProxy = view.usersListContainer.dataGrid.selectedItem as UserProxy;
			if ( selectedUser != null ) {
				user.id = selectedUser.id;
			}
			user.enabled = view.usersListContainer.enabledCheckBox.selected;
			user.name = view.usersListContainer.nameTextInput.text;
			user.rawPassword = view.usersListContainer.passwordTextInput.text;
			var roles:ListCollectionView = getSelectedRoles();
			user.roles = roles;
			return user;
		}
		
		protected function loadAll():void {
			view.usersListContainer.currentState = UsersListContainer.STATE_LOADING;
			view.userPerRoleContainer.currentState = UserPerRoleContainer.STATE_LOADING;
			view.userPerRoleContainer.rolesList.selectedItem = null;
			
			var responder:IResponder = new AsyncResponder(loadAllResultHandler, faultHandler);
			_userClient.loadAll(responder);
		}		
		
		protected function loadAllResultHandler(event:ResultEvent, token:Object = null):void {
			view.usersListContainer.currentState = UsersListContainer.STATE_DEFAULT;
			view.userPerRoleContainer.currentState = UserPerRoleContainer.STATE_DEFAULT;
			
			_loadedUsers = event.result as IList;
			
			view.usersListContainer.dataGrid.dataProvider = _loadedUsers;
		}
		
		protected function newUserButtonClickHandler(event:MouseEvent):void {
			view.usersListContainer.currentState = UsersListContainer.STATE_NEW;
			view.usersListContainer.dataGrid.selectedItem = null;
			resetForm();
		}
		
		protected function saveButtonClickHandler(event:MouseEvent):void {
			if ( validateForm() ) {
				var user:UserProxy = extractUserFromForm();
				var responder:IResponder = new AsyncResponder(saveUserResultHandler, saveUserFaultHandler);
				_userClient.save(responder, user);
			}
		}
		
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			var selectedUser:UserProxy = view.usersListContainer.dataGrid.selectedItem as UserProxy;
			if ( selectedUser != null ) {
				AlertUtil.showConfirm("usersManagement.delete.confirm", null, "global.deleteTitle", performDelete, [selectedUser.id]);
			} else {
				AlertUtil.showError("usersManagement.delete.selectUser");
			}
		}
		
		protected function performDelete(id:int):void {
			var responder:IResponder = new AsyncResponder(deleteUserResultHandler, deleteFaultHandler);
			_userClient.deleteUser(responder, id);
		}
		
		protected function deleteFaultHandler(event:FaultEvent, token:Object = null):void {
			var faultCode:String = event.fault.faultCode;
			switch(faultCode) {
				case "org.openforis.collect.manager.CannotDeleteUserException":
					AlertUtil.showError("usersManagement.error.cannotDelete");
					break;
				case "org.openforis.collect.manager.InvalidUserPasswordException":
					AlertUtil.showError("usersManagement.error.invalidPassword");
					break;
				default:
					faultHandler(event, token);
			}
		}
		
		protected function saveUserResultHandler(event:ResultEvent, token:Object = null):void {
			var savedUser:UserProxy = event.result as UserProxy;
			var selectedUser:UserProxy = view.usersListContainer.dataGrid.selectedItem as UserProxy;
			if ( selectedUser != null ) {
				var selectedUserIndex:int = _loadedUsers.getItemIndex(selectedUser);
				_loadedUsers.setItemAt(savedUser, selectedUserIndex);
			} else {
				loadAll();
			}
			view.usersListContainer.messageDisplay.show(Message.get("usersManagement.userSaved"));
		}
		
		protected function saveUserFaultHandler(event:FaultEvent, token:Object = null):void {
			var faultCode:String = event.fault.faultCode;
			switch(faultCode) {
				case "org.openforis.collect.manager.InvalidUserPasswordException":
					AlertUtil.showError("usersManagement.error.invalidPassword");
					break;
				default:
					faultHandler(event, token);
			}
		}
		
		protected function saveRolePerUserResultHandler(event:ResultEvent, token:Object = null):void {
			var savedUser:UserProxy = event.result as UserProxy;
			
		}
		
		protected function deleteUserResultHandler(event:ResultEvent, token:Object = null):void {
			loadAll();
		}
	}
}