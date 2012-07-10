package org.openforis.collect.event
{
	import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	
	/**
	 * @author S. Ricci
	 */
	public class UserManagementEvent extends InputFieldEvent
	{
		public static const USER_PER_ROLE_SELECTED:String = "rolePerUserSelected";
		
		private var _role:String;
		private var _user:UserProxy;
		private var _selected:Boolean;
			
		public function UserManagementEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false, 
											role:String = null, user:UserProxy = null, selected:Boolean = false) {
			super(type, bubbles, cancelable);
			this.role = role;
			this.user = user;
			this.selected = selected;
		}

		public function get role():String {
			return _role;
		}

		public function set role(value:String):void {
			_role = value;
		}

		public function get user():UserProxy {
			return _user;
		}

		public function set user(value:UserProxy):void {
			_user = value;
		}

		public function get selected():Boolean {
			return _selected;
		}

		public function set selected(value:Boolean):void {
			_selected = value;
		}

	}
}