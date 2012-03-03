/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.openforis.collect.model.proxy {
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.granite.collections.IMap;
	import org.openforis.collect.remoting.service.UpdateResponse;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.model.proxy.RecordProxy")]
    public class RecordProxy extends RecordProxyBase {
		
		private var validationResults:ValidationResultsProxy;
		
		public function getNode(id:int):NodeProxy {
			if(id == rootEntity.id) {
				return rootEntity;
			} else {
				return rootEntity.getNode(id);
			}
		}
		
		public function update(responses:IList):void {
			for each (var response:UpdateResponse in responses)	{
				processResponse(response);
			}
			
		}
		
		private function processResponse(response:UpdateResponse):void {
			var node:NodeProxy, oldNode:NodeProxy, parent:EntityProxy;
			if(response.createdNode != null) {
				node = response.createdNode;
				parent = getNode(node.parentId) as EntityProxy;
				parent.addChild(node);
			}
			if(response.deletedNodeId > 0) {
				node = getNode(response.deletedNodeId);
				parent = getNode(node.parentId) as EntityProxy;
				parent.removeChild(node);
			} else {
				node = getNode(response.nodeId);
				if(node is AttributeProxy) {
					var a:AttributeProxy = AttributeProxy(node);
					if(response.validationResults != null) {
						a.validationResults = response.validationResults;
					}
					if(response.updatedFieldValues != null) {
						var fieldIdxs:ArrayCollection = response.updatedFieldValues.keySet;
						for each (var i:int in fieldIdxs) {
							var f:FieldProxy = a.getField(i);
							f.value = response.updatedFieldValues.get(i);
						}
					}
				} else if(node is EntityProxy) {
					var e:EntityProxy = EntityProxy(node);
					if(response.maxCountValidation) {
						
					}
					if(response.minCountValidation != null) {
						
					}
					if(response.relevant != null) {
						
					}
					if(response.required != null) {
						
					}
				}
			}
			
			
			//remove nodes
			/*for each (var id:int in response.deletedNodeIds) {
				oldNode = getNode(id);
				if(oldNode != null) {
					parent = EntityProxy(getNode(oldNode.parentId));
					if(parent != null) {
						oldNode = parent.getChildById(id);
						parent.removeChild(oldNode);
					}
				}
			}*/
			//add new nodes
			/*for each (node in response.addedNodes) {
				parent = EntityProxy(getNode(node.parentId));
				parent.addChild(node);
			}*/
			
			//replace updated nodes
			/*for each (node in response.updatedNodes) {
				oldNode = getNode(node.id);
				parent = EntityProxy(getNode(oldNode.parentId));
				parent.replaceChild(oldNode, node);
			}*/
			
			//update node state
			/*
			for each (var state:NodeStateProxy in response.states) {
				node = getNode(state.nodeId);
				node.state = state;
			}
			*/
		}
		
		private function updateMaxCountValidations(value:ValidationResultsProxy):void {
		}
    }
}