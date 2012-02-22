package org.openforis.collect.client {
	import mx.rpc.AbstractOperation;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;

	/**
	 * @author M. Togna
	 * @author S. Ricci
	 **/
	public class RemoteCallWrapper {
		
		private var _responder:IResponder;
		private var _operation:AbstractOperation;
		private var _args:Array;
		private var _attempts:int;
		private var _active:Boolean;
		
		public function RemoteCallWrapper(responder:IResponder, operation:AbstractOperation, ... args:Array) {
			this._responder = responder;
			this._operation = operation;
			this._args = args[0]; //args is an array of array
			this._active = false;
			this._attempts = 0;
		}
		
		public function send():AsyncToken {
			var token:AsyncToken = this._operation.send.apply(null, this._args) as AsyncToken; 
			token.addResponder(this._responder);
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

		/*public function set attempts(value:int):void {
			_attempts = value;
		}*/

	}
}