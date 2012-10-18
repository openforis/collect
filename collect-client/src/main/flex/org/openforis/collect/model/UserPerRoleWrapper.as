package org.openforis.collect.model
{
	import org.openforis.collect.model.proxy.UserProxy;

	/**
	 * @author S. Ricci
	 */
	[Bindable]
	public class UserPerRoleWrapper {
		
		private var _user:UserProxy;
		private var _role:String;
		private var _selected:Boolean;
		
		public function UserPerRoleWrapper(user:UserProxy, role:String, selected:Boolean) {
			_user = user;
			_role = role;
			_selected = selected;
		}

		public function get role():String {
			return _role;
		}

		public function set role(value:String):void {
			_role = value;
		}

		public function get selected():Boolean {
			return _selected;
		}

		public function set selected(value:Boolean):void {
			_selected = value;
		}

		public function get user():UserProxy {
			return _user;
		}

		public function set user(value:UserProxy):void {
			_user = value;
		}

	}
}