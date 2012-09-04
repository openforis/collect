package org.openforis.collect.model {
	/**
	 * @author S. Ricci
	 */
	public class Queue {
		private var _first:Node;
		private var _last:Node;
		
		public function isEmpty():Boolean {
			return (_first == null);
		}
		
		/**
		 * 
		 * Append the object to the queue
		 * */
		public function push(data:Object):void {
			var node:Node = new Node();
			node.data = data;
			node.next = null;
			if (isEmpty()) {
				_first = node;
				_last = node;
			} else {
				_last.next = node;
				_last = node;
			}
		}
		
		/**
		 * Returns the head of the queue and removes it
		 * */
		public function pop():Object {
			if (isEmpty()) {
				return null;
			}
			var data:Object = _first.data;
			_first = _first.next;
			return data;
		}
		
		/**
		 * Returns the head of the queue without removing it
		 */
		public function get element():Object {
			if (isEmpty()) {
				return null;
			}
			var data:Object = _first.data;
			return data;
		}
		
		/**
		 * Append all the items to the queue
		 */
		public function pushAll(items:Array):void {
			for each (var item:* in items) {
				push(item);
			}
			
		}
	}
}

internal class Node {
	public var next : Node;
	public var data : Object;
}