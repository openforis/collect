package org.openforis.collect.ui.component.detail {
	
	/**
	 * Used by FormsContainer to identify each FormContainer
	 */
	public class FormContainerKey {
		
		private var _surveyId:int;
		private var _rootEntityId:int;
		private var _versionId:int;
		
		public function FormContainerKey(surveyId:int, rootEntityId:int, versionId:int) {
			this._surveyId = surveyId;
			this._rootEntityId = rootEntityId;
			this._versionId = versionId;
		}
		
		public function get surveyId():int {
			return _surveyId;
		}
		
		public function get rootEntityId():int {
			return _rootEntityId;
		}
		
		public function get versionId():int {
			return _versionId;
		}
	}
}