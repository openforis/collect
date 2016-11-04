package org.openforis.collect.event
{
	import mx.core.UIComponent;
	
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
		private var _source:UIComponent;
			
		public function UserManagementEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false, 
											role:String = null, user:UserProxy = null, selected:Boolean = false, 
											source:UIComponent = null) {
			super(type, bubbles, cancelable);
			this._role = role;
			this._user = user;
			this._selected = selected;
			this._source = source;
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
		
		public function get source():UIComponent {
			return _source;
		}

	}
}