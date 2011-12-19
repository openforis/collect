package org.openforis.collect.client
{
	import mx.rpc.AbstractOperation;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.model.Queue;

	/**
	 * 
	 * @author M. Togna
	 * @author S. Ricci
	 */
	public class RemoteCallQueueProcessor {
		private var _queue:Queue; //queue of ProcessableItem objects
		private var _responder:IResponder;
		private var _faultHandler:Function;
		private var _maxAttempts:int;
		
		public function RemoteCallQueueProcessor(maxAttempts:int = 1, faultHandler:Function = null) {
			this._queue = new Queue();
			this._maxAttempts = maxAttempts;
			this._faultHandler = faultHandler;
			_responder = new AsyncResponder(internalResultHandler, internalFaultHandler);
		}
		
		public function isEmpty():Boolean {
			return this._queue.isEmpty();
		}
		
		public function append(responder:IResponder, operation:AbstractOperation, ... args:Array):void {
			var queueItem:RemoteCallWrapper = new RemoteCallWrapper(responder, operation, args);
			_queue.push(queueItem);
			sendHeadRemoteCall();
		}
		
		protected function sendHeadRemoteCall():void {
			if(_queue.isEmpty()) {
				return;
			}
			var remoteCall:RemoteCallWrapper = getHeadElement();
			if(! remoteCall.active) {
				var token:AsyncToken = remoteCall.send();
				token.addResponder(_responder);
			}
		}

		protected function internalResultHandler(event:ResultEvent, token:Object = null):void {
			var call:RemoteCallWrapper = RemoteCallWrapper(_queue.pop()); //removes the first element
			call.reset();
			sendHeadRemoteCall();
		}
		
		protected function internalFaultHandler(event:FaultEvent, token:Object = null):void {
			//after it fails 3 times, the system has to be stopped.
			var remoteCall:RemoteCallWrapper = getHeadElement();
			if(remoteCall.attempts >= _maxAttempts){
				if(_faultHandler != null) {
					_faultHandler(event);
				}				
			} else {
				remoteCall.reset();
				sendHeadRemoteCall();
			}
		}
		
		private function getHeadElement():RemoteCallWrapper {
			var call:RemoteCallWrapper = RemoteCallWrapper(_queue.element);
			return call;
		}

	}
}