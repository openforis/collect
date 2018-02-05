package org.openforis.collect.ui.component.detail
{
	import mx.collections.IList;
	import mx.core.UIComponent;
	
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.BooleanAttributeDefinitionProxy
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UIOptionsProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeAttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RelevanceDisplayManager {
		
		public static const STYLE_NAME_NOT_RELEVANT:String = "notRelevant";
		
		/**
		 * Display of the error (stylename "error" or "warning" will be set on this component)
		 */
		private var _display:UIComponent;
		
		public function RelevanceDisplayManager(display:UIComponent) {
			this._display = display;
		}
		
		public function displayNodeRelevance(parentEntity:EntityProxy, defn:NodeDefinitionProxy):void {
			if(parentEntity != null && defn != null) {
				if ( canBeHidden(parentEntity, defn) ) {
					_display.visible = false;
					_display.includeInLayout = false;
				} else {
					_display.visible = true;
					_display.includeInLayout = true;
					var relevant:Boolean = parentEntity.isRelevant(defn);
					UIUtil.toggleStyleName(_display, STYLE_NAME_NOT_RELEVANT, ! relevant);
				}
			}
		}
		
		public function reset():void {
			_display.visible = true;
			_display.includeInLayout = true;

			UIUtil.removeStyleNames(_display, [
				STYLE_NAME_NOT_RELEVANT
			]);
		}
		
		private function canBeHidden(parentEntity:EntityProxy, childDefn:NodeDefinitionProxy):Boolean {
			if ( childDefn.hideWhenNotRelevant ) {
				//if nearest parent entity is table, hide fields when all cousins are not relevant and empty
				var nearestParentMultipleEntity:EntityDefinitionProxy = childDefn.nearestParentMultipleEntity;
				
				var nodes:IList;
				if ( nearestParentMultipleEntity.layout == UIUtil.LAYOUT_TABLE ) {
					nodes = parentEntity.getDescendantCousins(childDefn);
				} else {
					nodes = parentEntity.getChildren(childDefn);
				}
				//do not hide multiple entities renderer if they are relevant but no entities are defined or it will be impossible to add new entities
				if ( childDefn is EntityDefinitionProxy && nodes.length == 0 ) {
					var result:Boolean = ! parentEntity.isRelevant(childDefn);
					return result;
				} else {
					//hide table columns when all the cells are not relevant and empty
					var allNodesEmptyAndNotRelevant:Boolean = isAllNodesEmptyAndNotRelevant(nodes);
					return allNodesEmptyAndNotRelevant;
				}
			} else {
				return false;
			}
		}
		
		public static function isAllNodesEmptyAndNotRelevant(nodes:IList):Boolean {
			for each (var node:NodeProxy in nodes) {
				if (node.relevant) {
					return false;
				} else if (node.userSpecified) {
					if (node.definition is BooleanAttributeDefinitionProxy 
						&& BooleanAttributeDefinitionProxy(node.definition).affirmativeOnly) {
						if (AttributeProxy(node).getField(0).value == true) {
							return false;
						}
					} else if (! node.empty) {
						return false;
					}
				}
			}
			return true;
		}
		
	}
}