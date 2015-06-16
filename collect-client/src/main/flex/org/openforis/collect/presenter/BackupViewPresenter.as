package org.openforis.collect.presenter {
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	import flash.net.navigateToURL;
	import flash.utils.Timer;
	
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.Proxy;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.concurrency.CollectJobStatusPopUp;
	import org.openforis.collect.event.BackupEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.io.proxy.SurveyBackupJobProxy;
	import org.openforis.collect.ui.component.BackupView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.DateUtil;
	import org.openforis.concurrency.proxy.JobProxy;
	import org.openforis.concurrency.proxy.JobProxy$Status;
	
	import spark.components.DropDownList;
	import spark.events.IndexChangeEvent;
	
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
			
			view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			view.cancelExportButton.addEventListener(MouseEvent.CLICK, cancelExportButtonClickHandler);
			view.downloadButton.addEventListener(MouseEvent.CLICK, downloadButtonClickHandler);
			//view.closeButton1.addEventListener(MouseEvent.CLICK, closeHandler);
			//view.closeButton3.addEventListener(MouseEvent.CLICK, closeHandler);
			view.sendToRemoteCloneButton.addEventListener(MouseEvent.CLICK, sendToRemoteUrlClickHandler);
		}
		
		/*override protected function closeHandler(event:Event = null):void {
			if ( _job != null && _job is JobProxy && JobProxy(_job).running ) {
				AlertUtil.showMessage("export.cannotClosePopUp");
			} else {
				PopUpManager.removePopUp(view);
			}
		}*/
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
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
		
		protected function downloadButtonClickHandler(event:MouseEvent):void {
			var url:String = ApplicationConstants.DOWNLOAD_EXPORTED_DATA_URL;
			var dateStr:String = DateUtil.formatToXML(new Date());
			var fileName:String = SurveyBackupJobProxy(_job).outputFileName;
			var surveyName:String = view.surveyDropDown.selectedItem.name;
			var outputFileName:String = mx.utils.StringUtil.substitute(EXPORT_FILE_NAME_FORMAT, [surveyName, dateStr]); 

			var req:URLRequest = new URLRequest(url);
			req.data = new URLVariables();
			req.data.fileName = fileName;
			req.data.outputFileName = outputFileName;
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
		
		private function getSelectedSurveyName():String {
			var selectedSurvey:Object = view.surveyDropDown.selectedItem
			return selectedSurvey == null ? null: selectedSurvey.name;
		}
		
		protected function resetView():void {
			stopProgressTimer();
			_job = null;
			view.currentState = BackupView.STATE_PARAMETERS_SELECTION;
			checkEnabledFields();
		}
		
		protected function initView():void {
			initSurveyDropDown();
			
			populateForm();
			
			view.currentState = BackupView.STATE_PARAMETERS_SELECTION;

			//try to see if there is an export still running
			updateExportState();
		}
		
		protected function checkEnabledFields():void {
		}
		
		protected function populateForm():void {
		}
		
		protected function initSurveyDropDown():void {
			var surveys:IList = Application.surveySummaries;
			var dropDownList:DropDownList = view.surveyDropDown;
			dropDownList.dataProvider = surveys;
			dropDownList.callLater(function():void {
				dropDownList.selectedIndex = 0;
				updateSelectedSurveyInfo();
			});
			dropDownList.addEventListener(IndexChangeEvent.CHANGE, function(event:IndexChangeEvent):void {
				updateSelectedSurveyInfo();
			});
		}
		
		private function updateSelectedSurveyInfo():void {
			selectedSurveyInfo = null;
			var surveyName:String = getSelectedSurveyName();
			if (surveyName == null) {
				view.lastBackupDateLabel.text = view.updatedRecordsSinceLastBackupCountLabel.text = "-";
			} else {
				view.currentState = BackupView.STATE_LOADING;
				
				var responder:AsyncResponder = new AsyncResponder(function(event:ResultEvent, token:Object = null):void {
					selectedSurveyInfo = event.result;
					view.lastBackupDateLabel.text = DateUtil.format(selectedSurveyInfo.date);
					view.updatedRecordsSinceLastBackupCountLabel.text = String(selectedSurveyInfo.updatedRecordsSinceBackup);
					view.currentState = BackupView.STATE_PARAMETERS_SELECTION;
				}, faultHandler);
				
				ClientFactory.dataExportClient.getLastBackupInfo(responder, surveyName);
			}
		}
		
		private function sendToRemoteUrlClickHandler(event:Event):void {
			var responder:IResponder = new AsyncResponder(function(event:ResultEvent, token:Object = null):void {
				var jobId:String = event.result as String;
				var jobMonitor:JobMonitor = new JobMonitor(jobId);
				CollectJobStatusPopUp.openPopUp();
				jobMonitor.start();
			}, faultHandler);
			ClientFactory.dataExportClient.sendBackupToRemoteClone(responder, getSelectedSurveyName());
		}
		
	}
}