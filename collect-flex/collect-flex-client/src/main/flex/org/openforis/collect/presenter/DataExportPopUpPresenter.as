package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.NodeItem;
	import org.openforis.collect.ui.component.DataExportPopUp;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.web.session.DataExportState;


	public class DataExportPopUpPresenter extends AbstractPresenter {
		
		private static const PROGRESS_DELAY:int = 5000;
		
		private var _view:DataExportPopUp;
		private var _cancelResponder:IResponder;
		private var _exportResponder:IResponder;
		private var _getStateResponder:IResponder;
		private var _progressTimer:Timer;
		private var _state:DataExportState;
		private var _firstOpen:Boolean = true;
		
		public function DataExportPopUpPresenter(view:DataExportPopUp) {
			this._view = view;
			this._exportResponder = new AsyncResponder(exportResultHandler, faultHandler);
			this._cancelResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			this._getStateResponder = new AsyncResponder(getStateResultHandler, faultHandler);
			
			super();
			
			if(Application.activeRootEntity != null) {
				initView(Application.activeRootEntity);
			}
		}
		
		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			_view.addEventListener(CloseEvent.CLOSE, closeHandler);
			_view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			_view.downloadButton.addEventListener(MouseEvent.CLICK, downloadButtonClickHandler);
			_view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
		}
		
		protected function closeHandler(event:CloseEvent):void {
			if ( _state != null && _state.running ) {
				AlertUtil.showMessage("export.cannotClosePopUp");
			} else {
				PopUpManager.removePopUp(_view);
			}
		}
		
		internal function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntityDef:EntityDefinitionProxy = event.obj as EntityDefinitionProxy;
			initView(rootEntityDef);
		}
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			var selectedEntity:NodeItem = _view.rootTree.selectedItem as NodeItem;
			if ( selectedEntity == null ) {
				AlertUtil.showMessage("export.selectAnEntity");
			} else {
				var rootEntity:String = Application.activeRootEntity.name;
				var stepNumber:int = 1;
				var entityId:int = selectedEntity.id;
				ClientFactory.dataExportClient.export(_exportResponder, rootEntity, entityId, stepNumber);
	
				_view.currentState = DataExportPopUp.STATE_EXPORTING;
				_view.progressBar.setProgress(0, 0);
			}
		}
		
		protected function downloadButtonClickHandler(event:MouseEvent):void {
			var url:String = ApplicationConstants.DOWNLOAD_EXPORTED_DATA_URL;
			navigateToURL(new URLRequest(url), "_new");
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			ClientFactory.dataExportClient.cancel(_cancelResponder);
		}
		
		protected function exportResultHandler(event:ResultEvent, token:Object = null):void {
			if ( event.result == true ) {
				//startProgressTimer();
			} else {
				//AlertUtil.showError("export.concurrentException");
				//do nothing, update the state and try to "recover" the exporting
			}
			updateExportState();
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
			updateExportState();
		}
		
		protected function updateExportState():void {
			ClientFactory.dataExportClient.getState(_getStateResponder);
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			_state = null;
			_progressTimer.stop();
			_view.currentState = DataExportPopUp.STATE_DEFAULT;
		}
		
		protected function getStateResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as DataExportState;
			if(_state != null) {
				if ( _state.running && _state.count <= _state.total ) {
					_view.currentState = DataExportPopUp.STATE_EXPORTING;
					_view.progressBar.setProgress(_state.count, _state.total);
					_view.progressLabel.text = Message.get("export.progressLabel", [_state.count, _state.total]);
					if ( _progressTimer == null ) {
						startProgressTimer();
					}
				} else if ( _state.error) {
					_view.currentState = DataExportPopUp.STATE_DEFAULT;
					stopProgressTimer();
					AlertUtil.showError("export.error");
				} else if ( _state.complete ) {
					if ( _firstOpen ) {
						_view.currentState = DataExportPopUp.STATE_DEFAULT;
					} else {
						_view.currentState = DataExportPopUp.STATE_COMPLETE;
					}
					stopProgressTimer();
				}
			} else {
				_view.currentState = DataExportPopUp.STATE_DEFAULT;
				stopProgressTimer();
			}
			_firstOpen = false;
		}
		
		internal function initView(rootEntityDef:EntityDefinitionProxy):void {
			var rootNodeItem:NodeItem = NodeItem.fromNodeDef(rootEntityDef, true, false, false);
			_view.rootTree.dataProvider = new ArrayCollection([rootNodeItem]);
			_view.rootTree.callLater(function():void {
				_view.rootTree.expandItem(rootNodeItem, true);
			});
			//try to see if there is an export still running
			updateExportState();
		}
		
	}
}