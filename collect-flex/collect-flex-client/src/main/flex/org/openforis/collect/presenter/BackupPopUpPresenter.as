package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	import flash.net.navigateToURL;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.ui.component.BackupPopUp;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class BackupPopUpPresenter extends PopUpPresenter {
		
		private static const PROGRESS_DELAY:int = 5000;
		
		private var _cancelResponder:IResponder;
		private var _backupResponder:IResponder;
		private var _getStatusResponder:IResponder;
		private var _progressTimer:Timer;
		private var _status:Object;
		private var _firstOpen:Boolean = true;
		
		public function BackupPopUpPresenter(view:BackupPopUp) {
			this._backupResponder = new AsyncResponder(backupResultHandler, faultHandler);
			this._cancelResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			this._getStatusResponder = new AsyncResponder(getStatusResultHandler, faultHandler);
			
			super(view);
			
			initView();
		}
		
		internal function initView():void {
			var steps:IList = new ArrayCollection(CollectRecord$Step.constants);
			var allStepsItem:Object = {label: Message.get('global.allItemsLabel')};
			steps.addItemAt(allStepsItem, 0);
			BackupPopUp(_view).stepDropDownList.dataProvider = steps;
			BackupPopUp(_view).stepDropDownList.callLater(function():void {
				BackupPopUp(_view).stepDropDownList.selectedIndex = 0;
			});
			//try to see if there is an export still running
			updateStatus();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			BackupPopUp(_view).backupButton.addEventListener(MouseEvent.CLICK, backupButtonClickHandler);
			BackupPopUp(_view).cancelButton.addEventListener(MouseEvent.CLICK, closeHandler);
			BackupPopUp(_view).backupCompleteCloseButton.addEventListener(MouseEvent.CLICK, closeHandler);
			BackupPopUp(_view).downloadButton.addEventListener(MouseEvent.CLICK, downloadButtonClickHandler);
			BackupPopUp(_view).cancelBackupButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
		}
		
		override protected function closeHandler(event:Event = null):void {
			if ( _status != null && _status.running ) {
				AlertUtil.showMessage("backup.cannotClosePopUp");
			} else {
				super.closeHandler(event);
			}
		}
		
		protected function backupButtonClickHandler(event:MouseEvent):void {
			var rootEntity:String = Application.activeRootEntity.name;
			var step:Object = BackupPopUp(_view).stepDropDownList.selectedItem;
			var stepNumber:int = -1;
			switch(step) {
				case CollectRecord$Step.ENTRY:
					stepNumber = 1;
					break;
				case CollectRecord$Step.CLEANSING:
					stepNumber = 2;
					break;
				case CollectRecord$Step.ANALYSIS:
					stepNumber = 3;
					break;
			}
			ClientFactory.backupClient.backup(_backupResponder, rootEntity, null, stepNumber);
			
			BackupPopUp(_view).currentState = BackupPopUp.STATE_RUNNING;
			BackupPopUp(_view).progressBar.setProgress(0, 0);
		}
		
		protected function downloadButtonClickHandler(event:MouseEvent):void {
			var url:String = ApplicationConstants.DOWNLOAD_BACKUP_URL;
			var req:URLRequest = new URLRequest(url);
			var rootEntity:String = Application.activeRootEntity.name;
			var variables:URLVariables = new URLVariables();
			variables.rootEntityName = rootEntity;
			req.data = variables;
			navigateToURL(req, "_new");
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			var rootEntity:String = Application.activeRootEntity.name;
			ClientFactory.backupClient.cancel(_cancelResponder, rootEntity);
		}
		
		protected function backupResultHandler(event:ResultEvent, token:Object = null):void {
			/*
			if ( event.result.start != null ) {
				//startProgressTimer();
			} else if ( event.result.error != null ) {
				//AlertUtil.showError("backup.concurrentException");
				//AlertUtil.showError(event.result.error);
				
			}
			*/
			
			//do nothing, update the state and try to "recover" the backup
			updateStatus();
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
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			updateStatus();
		}
		
		protected function updateStatus():void {
			var rootEntity:String = Application.activeRootEntity.name;
			ClientFactory.backupClient.getStatus(_getStatusResponder, rootEntity);
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			_status = null;
			stopProgressTimer();
			BackupPopUp(_view).currentState = BackupPopUp.STATE_DEFAULT;
		}
		
		protected function getStatusResultHandler(event:ResultEvent, token:Object = null):void {
			_status = event.result;
			if(_status != null) {
				if ( _status.active && _status.count < _status.total ) {
					BackupPopUp(_view).currentState = BackupPopUp.STATE_RUNNING;
					BackupPopUp(_view).progressBar.setProgress(_status.count, _status.total);
					BackupPopUp(_view).progressLabel.text = Message.get("backup.progressLabel", [_status.count, _status.total]);
					if ( _progressTimer == null || ! _progressTimer.running) {
						startProgressTimer();
					}
				} else if ( _status.error) {
					BackupPopUp(_view).currentState = BackupPopUp.STATE_DEFAULT;
					stopProgressTimer();
					AlertUtil.showError("backup.error");
				} else if ( _status.count == _status.total ) {
					if ( _firstOpen ) {
						BackupPopUp(_view).currentState = BackupPopUp.STATE_DEFAULT;
					} else {
						BackupPopUp(_view).currentState = BackupPopUp.STATE_COMPLETE;
					}
					stopProgressTimer();
				}
			} else {
				BackupPopUp(_view).currentState = BackupPopUp.STATE_DEFAULT;
				stopProgressTimer();
			}
			_firstOpen = false;
		}
		
	}
}