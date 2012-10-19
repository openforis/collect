package org.openforis.collect.client {
	import mx.rpc.AbstractOperation;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;

	/**
	 * @author M. Togna
	 * @author S. Ricci
	 **/
	public class RemoteCallWrapper {
		
		private var _operation:AbstractOperation;
		private var _args:Array;
		private var _attempts:int;
		private var _active:Boolean;
		private var _resultHandler:Function;
		private var _faultHandler:Function;
		private var _token:Object;
		
		public function RemoteCallWrapper(operation:AbstractOperation, ... args:Array) {
			this._operation = operation;
			this._args = args[0]; //args is an array of array
			this._active = false;
			this._attempts = 0;
		}
		
		public function send():AsyncToken {
			var token:AsyncToken = _operation.send.apply(null, this._args) as AsyncToken; 
			//token.addResponder(this._responder);
			this._active = true;
			this._attempts ++;
			return token;
		}
		
		public function get active():Boolean {
			return this._active;
		}
		
		public function reset():void {
			this._active = false;
		}

		public function get attempts():int {
			return _attempts;
		}
		
		public function get resultHandler():Function {
			return _resultHandler;
		}

		public function set resultHandler(value:Function):void {
			_resultHandler = value;
		}
		
		public function get faultHandler():Function {
			return _faultHandler;
		}

		public function set faultHandler(value:Function):void {
			_faultHandler = value;
		}

		public function get token():Object {
			return _token;
		}

		public function set token(value:Object):void {
			_token = value;
		}
		
		public function get operation():AbstractOperation {
			return _operation;
		}
	}
}