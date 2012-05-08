/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.openforis.collect.model.proxy {
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.util.CollectionUtil;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.model.proxy.UserProxy")]
    public class UserProxy extends UserProxyBase {
		
		public static const ROLE_ENTRY:String = "ROLE_ENTRY";
		public static const ROLE_CLEANSING:String = "ROLE_CLEANSING";
		public static const ROLE_ANALYSIS:String = "ROLE_ANALYSIS";
		public static const ROLE_ADMIN:String = "ROLE_ADMIN";
		
		public static const ROLES_HIERARCHY:Array = [ROLE_ENTRY, ROLE_CLEANSING, ROLE_ANALYSIS, ROLE_ADMIN];
		
		private function calculateHighestRoleIndex():int {
			var max:int = -1;
			for each (var role:String in roles) {
				var i:int = ROLES_HIERARCHY.indexOf(role);
				if ( i > max ) {
					max = i;
				}
			}
			return max;
		}
		
		public function hasRole(role:String):Boolean {
			var highest:int = calculateHighestRoleIndex();
			var index:int = ROLES_HIERARCHY.indexOf(role);
			return highest >= index;
			//var result:Boolean = CollectionUtil.contains(roles, role);
			//return result;
		}
		
		public function canSubmit(record:RecordProxy):Boolean {
			return hasRole(ROLE_ENTRY) && record.step == CollectRecord$Step.ENTRY || 
				hasRole(ROLE_CLEANSING) && record.step == CollectRecord$Step.CLEANSING;
		}

		public function canReject(record:RecordProxy):Boolean {
			return hasRole(ROLE_CLEANSING) && record.step == CollectRecord$Step.CLEANSING || 
				hasRole(ROLE_ANALYSIS) && record.step == CollectRecord$Step.ANALYSIS;
		}
}
}