package org.openforis.collect.presenter {
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	import flash.net.navigateToURL;
	import flash.utils.Timer;
	
	import mx.collections.IList;
	import mx.events.FlexEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.Proxy;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.concurrency.CollectJobStatusPopUp;
	import org.openforis.collect.event.BackupEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.io.proxy.SurveyBackupJobProxy;
	import org.openforis.collect.model.proxy.ConfigurationProxy;
	import org.openforis.collect.ui.component.BackupView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.DateUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.concurrency.proxy.JobProxy;
	import org.openforis.concurrency.proxy.JobProxy$Status;
	
	import spark.components.DropDownList;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class BackupViewPresenter extends AbstractPresenter {
		
		private static const PROGRESS_DELAY:int = 2000;
		
		private static const EXPORT_FILE_NAME_FORMAT:String = "{0}_{1}.collect-backup";
		
		private var _cancelResponder:IResponder;
		private var _exportResponder:IResponder;
		private var _getStateResponder:IResponder;
		private var _progressTimer:Timer;
		private var _type:String;
		private var _job:Proxy;
		private var _firstOpen:Boolean = true;
		private var selectedSurveyInfo:Object;
		
		public function BackupViewPresenter(view:BackupView) {
			super(view);
			this._exportResponder = new AsyncResponder(exportResultHandler, faultHandler);
			this._cancelResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			this._getStateResponder = new AsyncResponder(getStateResultHandler, faultHandler);
			
			view.remoteCloneContainer.addEventListener(FlexEvent.CREATION_COMPLETE, remoteCloneContainerCreationCompleteHandler);
		}
		
		private function remoteCloneContainerCreationCompleteHandler(event:FlexEvent):void {
			initSendToRemoteCloneContainer();
		}
		
		private function get view():BackupView {
			return BackupView(_view);
		}
		
		override public function init():void {
			super.init();
			initView();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.exportButton.addEventListener(MouseEvent.CLICK, backupButtonClickHandler);
			view.cancelExportButton.addEventListener(MouseEvent.CLICK, cancelExportButtonClickHandler);
			view.downloadButton.addEventListener(MouseEvent.CLICK, downloadLastBackupButtonClickHandler);
			view.backButton.addEventListener(MouseEvent.CLICK, backButtonClickHandler);
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(UIEvent.SURVEYS_UPDATED, surveysUpdatedHandler);
		}
		
		protected function backButtonClickHandler(event:MouseEvent):void {
			resetView();
		}
		
		protected function backupButtonClickHandler(event:MouseEvent):void {
			if ( ! validateForm() ) {
				return;
			}
			var surveyName:String = view.surveyDropDown.selectedItem.name;
			ClientFactory.dataExportClient.backup(_exportResponder, surveyName);
			view.currentState = BackupView.STATE_EXPORTING;
			view.progressBar.setProgress(0, 0);
		}
		
		private function validateForm():Boolean {
			if (view.surveyDropDown.selectedItem == null) {
				AlertUtil.showMessage("backup.validation.select_survey");
				return false;
			}
			return true;
		}
		
		private function downloadLastBackupButtonClickHandler(event:MouseEvent):void {
			var surveyName:String = getSelectedSurveyName();
			var url:String = ApplicationConstants.URL + "surveys/" + surveyName + "/data/backup/last";
			
			var req:URLRequest = new URLRequest(url);
			req.data = new URLVariables();
			navigateToURL(req, "_new");
		}
		
		protected function cancelExportButtonClickHandler(event:MouseEvent):void {
			ClientFactory.dataExportClient.abort(_cancelResponder);
		}
		
		protected function exportResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as Proxy;
			updateView();
		}
		
		protected function startProgressTimer():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(PROGRESS_DELAY);
				_progressTimer.addEventListener(TimerEvent.TIMER, progressTimerHandler);
			}
			_progressTimer.start();
		}
		
		protected function stopProgressTimer():void {
			if ( _progressTimer != null ) {
				_progressTimer.stop();
				_progressTimer = null;
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			updateExportState();
		}
		
		protected function updateExportState():void {
			ClientFactory.dataExportClient.getCurrentJob(_getStateResponder);
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function getStateResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as Proxy;
			updateView();
		}
		
		protected function updateView():void {
			if ( _job != null ) {
				var job:JobProxy = _job as JobProxy;
				var progress:int = job.progressPercent;
				if ( job.running && progress <= 100 ) {
					view.currentState = BackupView.STATE_EXPORTING;
					view.progressBar.setProgress(progress, 100);
					var progressText:String = Message.get("export.processing");
					view.progressLabel.text = progressText;
					if ( _progressTimer == null ) {
						startProgressTimer();
					}
				} else if ( _firstOpen ) {
					resetView();
				} else {
					switch ( job.status ) {
					case JobProxy$Status.COMPLETED:
						view.currentState = BackupView.STATE_COMPLETE;
						stopProgressTimer();
						if (SurveyBackupJobProxy(job).dataBackupErrorsFound) { 
							AlertUtil.showError("export.complete_with_errors");
						}
						eventDispatcher.dispatchEvent(new BackupEvent(BackupEvent.BACKUP_COMPLETE, getSelectedSurveyName()));
						break;
					case JobProxy$Status.FAILED:
						AlertUtil.showError("export.error");
						resetView();
						break;
					case JobProxy$Status.ABORTED:
						AlertUtil.showError("export.cancelled");
						resetView();
						break;
					default:
						//process starting in a while...
						startProgressTimer();
					}
				}
			} else {
				resetView();
			}
			_firstOpen = false;
		}
		
		private function getSelectedSurvey():Object {
			var selectedSurvey:Object = view.surveyDropDown.selectedItem;
			return selectedSurvey;
		}
		
		private function getSelectedSurveyName():String {
			var selectedSurvey:Object = getSelectedSurvey();
			return selectedSurvey == null ? null: selectedSurvey.name;
		}
		
		private function getSelectedSurveyId():Number {
			var selectedSurvey:Object = getSelectedSurvey();
			return selectedSurvey == null ? NaN: selectedSurvey.id;
		}
		
		protected function resetView():void {
			stopProgressTimer();
			_job = null;
			view.currentState = BackupView.STATE_PARAMETERS_SELECTION;
			view.surveyDropDown.selectedIndex = 0;
			populateForm();
			checkEnabledFields();
		}
		
		protected function initView():void {
			initSurveyDropDown();
			
			populateForm();
			
			view.currentState = BackupView.STATE_PARAMETERS_SELECTION;

			//try to see if there is an export still running
			updateExportState();
		}
		
		private function initSendToRemoteCloneContainer():void {
			ClientFactory.configurationClient.loadConfiguration(new AsyncResponder(function(event:ResultEvent, token:Object = null):void {
				var conf:ConfigurationProxy = event.result as ConfigurationProxy;
				if (org.openforis.collect.util.StringUtil.isNotBlank(conf.remoteCloneUrl)) {
					view.sendToRemoteCloneContainer.visible = view.sendToRemoteCloneContainer.includeInLayout = true;
					view.sendToRemoteCloneButton.addEventListener(MouseEvent.CLICK, sendToRemoteUrlClickHandler);
					view.remoteCloneUrlTextInput.text = conf.remoteCloneUrl;
					view.remoteCloneNotConfiguredLabel.visible = view.remoteCloneNotConfiguredLabel.includeInLayout = false;
				}
			}, faultHandler));
		}
		
		protected function checkEnabledFields():void {
		}
		
		protected function populateForm():void {
		}
		
		protected function initSurveyDropDown():void {
			refreshSurveyDropDown();
			
			var dropDownList:DropDownList = view.surveyDropDown;
			
			dropDownList.callLater(function():void {
				dropDownList.selectedIndex = 0;
			});
			dropDownList.addEventListener(FlexEvent.VALUE_COMMIT, function(event:Event):void {
				updateSelectedSurveyInfo();
			});
		}
		
		private function refreshSurveyDropDown():void {
			var surveys:IList = Application.surveySummaries;
			var dropDownList:DropDownList = view.surveyDropDown;
			dropDownList.dataProvider = surveys;
		}
		
		private function updateSelectedSurveyInfo():void {
			selectedSurveyInfo = null;
			var surveyName:String = getSelectedSurveyName();
			if (surveyName == null) {
				view.lastBackupDateLabel.text = view.updatedRecordsSinceLastBackupCountLabel.text = "-";
			} else {
				view.currentState = BackupView.STATE_LOADING;
				
				view.lastBackupDateLabel.text = "";
				
				var responder:AsyncResponder = new AsyncResponder(function(event:ResultEvent, token:Object = null):void {
					selectedSurveyInfo = event.result;
					if (selectedSurveyInfo.date != null) {
						view.lastBackupDateLabel.text = DateUtil.format(selectedSurveyInfo.date);
					}
					view.updatedRecordsSinceLastBackupCountLabel.text = String(selectedSurveyInfo.updatedRecordsSinceBackup);
					view.currentState = BackupView.STATE_PARAMETERS_SELECTION;
				}, faultHandler);
				
				ClientFactory.dataExportClient.getLastBackupInfo(responder, surveyName);
			}
		}
		
		private function sendToRemoteUrlClickHandler(event:Event):void {
			ClientFactory.configurationClient.isRemoteCloneValid(new AsyncResponder(function(event:ResultEvent, token:Object = null):void {
				if (! event.result) {
					AlertUtil.showError(Message.get("backup.remote_clone.not_valid"));
					return;
				}
				var responder:IResponder = new AsyncResponder(function(event:ResultEvent, token:Object = null):void {
					var jobId:String = event.result as String;
					var jobMonitor:JobMonitor = new JobMonitor(jobId);
					CollectJobStatusPopUp.openPopUp();
					jobMonitor.start();
				}, faultHandler);
				ClientFactory.dataExportClient.sendBackupToRemoteClone(responder, getSelectedSurveyName());
			}, function(event:FaultEvent, token:Object = null):void {
				AlertUtil.showError(Message.get("configuration.remote_clone.error_verifying_validity", [event.fault.message]));
			}));
		}
		
		private function surveysUpdatedHandler(event:UIEvent):void {
			refreshSurveyDropDown();
		}
	}
}