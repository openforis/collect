package org.openforis.collect.event
{
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class BackupEvent extends UIEvent {

		public static const BACKUP_COMPLETE:String = "backupComplete";
		
		private var _surveyName:String;
		
		public function BackupEvent(type:String, surveyName:String = null) {
			super(type, bubbles, cancelable);
			_surveyName = surveyName;
		}
		
		public function get surveyName():String {
			return _surveyName;
		}

	}
}