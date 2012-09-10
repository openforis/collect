package org.openforis.collect.model
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;

	/**
	 * @author S. Ricci
	 */
	[Bindable]
	public class NodeItem {
		
		private var _id:int;
		private var _label:String;
		private var _children:IList;
		private var _state:String;
		
		public function NodeItem() {
		}
		
		public static function fromNodeDef(nodeDef:NodeDefinitionProxy, 
										   includeChildren:Boolean = true, 
										   includeChildrenAttributes:Boolean = true, 
										   includeSingleEntities:Boolean = true):NodeItem {
			var item:NodeItem = new NodeItem();
			item.id = nodeDef.id;
			item.label = nodeDef.getLabelText();
			if(includeChildren) {
				var children:IList = new ArrayCollection();
				var childDefinitions:ListCollectionView = EntityDefinitionProxy(nodeDef).childDefinitions;
				for each (var childNodeDefn:NodeDefinitionProxy in childDefinitions) {
					if ( (childNodeDefn is EntityDefinitionProxy && (childNodeDefn.multiple || includeSingleEntities)) || 
						(childNodeDefn is AttributeDefinitionProxy && includeChildrenAttributes) ) {
						var child:NodeItem = fromNodeDef(childNodeDefn, includeChildren, includeChildrenAttributes, includeSingleEntities);
						children.addItem(child);
					}
				}
				if (children.length > 0) {
					item.children = children;
				}
			}
			return item;
		}
		
		
		public function get label():String {
			return _label;
		}

		public function set label(value:String):void {
			_label = value;
		}

		public function get children():IList {
			return _children;
		}

		public function set children(value:IList):void {
			_children = value;
		}

		public function get state():String {
			return _state;
		}

		public function set state(value:String):void {
			_state = value;
		}

		public function get id():int {
			return _id;
		}

		public function set id(value:int):void {
			_id = value;
		}


	}
}