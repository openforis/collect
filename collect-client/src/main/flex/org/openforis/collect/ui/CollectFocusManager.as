package org.openforis.collect.ui {
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.DateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumericAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.ui.UIOptions$CoordinateAttributeFieldsOrder;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeAttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.FieldProxy;


	/**
	 * @author S. Ricci
	 */
	public class CollectFocusManager {
		
		public static function moveFocusOnNextField(field:FieldProxy, horizontalMove:Boolean, offset:int):Boolean {
			var attr:AttributeProxy = field.parent;
			var parentMultipleEntity:EntityProxy = attr.getParentMultipleEntity();
			var parentMultipleEntityDefn:EntityDefinitionProxy = EntityDefinitionProxy(parentMultipleEntity.definition);
			if ( parentMultipleEntityDefn.hasTableLayout() ) {
				var attrDefn:AttributeDefinitionProxy = AttributeDefinitionProxy(attr.definition);
				var directionByColumns:Boolean = attrDefn.hasDirectionByColumns();
				var focusChanged:Boolean = false;
				if ( horizontalMove ) {
					//horizontal
					if ( directionByColumns ) {
						if ( attrDefn.hasAlwaysHorizontalLayout() ) {
							focusChanged = setFocusOnSiblingFieldInAttribute(field, offset);
						}
						if ( !focusChanged ) {
							focusChanged = setFocusOnAttributeInSiblingEntity(field, offset, true);
						}
						if ( !focusChanged ) {
							focusChanged = setFocusOnSiblingField(field, offset);
						}
					} else {
						focusChanged = setFocusOnSiblingFieldInAttribute(field, offset);
						if ( !focusChanged ) {
							focusChanged = setFocusOnSiblingField(field, offset);
						}
						if ( !focusChanged ) {
							focusChanged = setFocusOnBoundaryFieldInSiblingEntity(field, offset > 0 ? 1: -1, offset > 0);
						}
					}
				} else {
					//vertical
					if ( directionByColumns) {
						focusChanged = setFocusOnSiblingField(field, offset, true);
					} else {
						focusChanged = setFocusOnAttributeInSiblingEntity(field, offset, false);
					}
				}
			}
			return focusChanged;
		}
		
		public static function getSiblingFocusableField(field:FieldProxy, offset:int, limit:Boolean = true):FieldProxy {
			var parentMultipleEntity:EntityProxy = field.parent.getParentMultipleEntity();
			if ( parentMultipleEntity != null ) {
				var siblingFields:IList = getLeafFocusableFields(parentMultipleEntity);
				var currentFieldIndex:int = siblingFields.getItemIndex(field);
				var siblingFieldIndex:int = currentFieldIndex + offset;
				if ( siblingFieldIndex < 0 ) {
					if ( limit ) {
						siblingFieldIndex = 0;
					} else {
						return null;
					}
				} else if ( siblingFieldIndex > siblingFields.length - 1 ) {
					if ( limit ) {
						siblingFieldIndex = siblingFields.length - 1;
					} else {
						return null;
					}
				}
				if ( siblingFieldIndex != currentFieldIndex ) {
					return siblingFields.getItemAt(siblingFieldIndex) as FieldProxy;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		
		public static function getLeafFocusableFields(entity:EntityProxy):IList {
			var result:ArrayCollection = new ArrayCollection();
			var leafAttributes:IList = getLeafFocusableAttributes(entity);
			for each (var attr:AttributeProxy in leafAttributes) {
				var attrDefn:AttributeDefinitionProxy = AttributeDefinitionProxy(attr.definition);
				if ( attrDefn.hasDirectionByColumns() && attrDefn.hasAlwaysHorizontalLayout() ) {
					var field:FieldProxy = getFirstFocusableField(attr);
					result.addItem(field);
				} else {
					var fields:IList = getFocusableFieldsInOrder(attr);
					result.addAll(fields);
				}
			}
			return result;
		}
		
		protected static function getFocusableFieldsInOrder(attr:AttributeProxy):IList {
			var result:ArrayCollection = new ArrayCollection();
			var fields:Array = attr.fields.toArray();
			if ( attr.definition is CoordinateAttributeDefinitionProxy ) {
				var fieldsOrder:UIOptions$CoordinateAttributeFieldsOrder = CoordinateAttributeDefinitionProxy(attr.definition).fieldsOrder;
				switch ( fieldsOrder ) {
					case UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y:
						result.addItem(fields[2]); //srs
						result.addItem(fields[0]); //x
						result.addItem(fields[1]); //y
						break;
					case UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X:
						result.addItem(fields[2]); //srs
						result.addItem(fields[1]); //y
						result.addItem(fields[0]); //x
						break;
					default:
						result.addAll(new ArrayCollection(fields));
				}
			} else if ( attr.definition is DateAttributeDefinitionProxy ) {
				result.addItem(fields[2]); //year
				result.addItem(fields[1]); //month
				result.addItem(fields[0]); //day
			} else if ( attr.definition is NumericAttributeDefinitionProxy ) {
				result.addItem(fields[0]); //value field
				var unitIdField:FieldProxy = FieldProxy(fields[2]);
				if ( isFieldFocusable(unitIdField) ) {
					result.addItem(unitIdField);
				}
			} else {
				for each (var f:FieldProxy in attr.fields) {
					if ( isFieldFocusable(f) ) {
						result.addItem(f);
					}
				}
			}
			return result;
		}
		
		public static function getLeafFocusableAttributes(entity:EntityProxy):IList {
			var result:ArrayCollection = new ArrayCollection();
			var leafAttributes:IList = entity.getLeafAttributes();
			for each (var a:AttributeProxy in leafAttributes) {
				if ( ! (a.definition is CodeAttributeDefinitionProxy) || (! a.definition.multiple) || a.getIndex() == 0 ) { 
					result.addItem(a);
				}
			}
			return result;
		}
		
		public static function isFieldFocusable(field:FieldProxy):Boolean {
			var attrDefn:AttributeDefinitionProxy = AttributeDefinitionProxy(field.parent.definition);
			if ( attrDefn is CodeAttributeDefinitionProxy && 
					( CodeAttributeDefinitionProxy(attrDefn).enumeratingAttribute || field.index > 0 ) ) {
				return false;
			} else if ( attrDefn is NumberAttributeDefinitionProxy && NumberAttributeDefinitionProxy(attrDefn).units.length <= 1 && field.index > 0) {
				return false;
			} else if ( attrDefn is FileAttributeDefinitionProxy && field.index > 0 ) {
				return false;
			} else {
				return true;
			}
		}
		
		public static function getSiblingFocusableFieldInAttribute(field:FieldProxy, forward:Boolean = true):FieldProxy {
			var attr:AttributeProxy = field.parent;
			var focusableFields:IList = getFocusableFieldsInOrder(attr);
			var adaptedFieldIdx:int = focusableFields.getItemIndex(field);
			var siblingFieldIndex:int = forward ? adaptedFieldIdx + 1: adaptedFieldIdx -1;
			if ( siblingFieldIndex >= 0 && siblingFieldIndex < focusableFields.length ) {
				return FieldProxy(focusableFields.getItemAt(siblingFieldIndex));
			} else {
				return null;
			}
		}
		
		public static function getFirstFocusableField(attr:AttributeProxy):FieldProxy {
			var fieldIdx:int = getFirstFocusableFieldIndex(AttributeDefinitionProxy(attr.definition));
			if ( fieldIdx < 0 ) {
				return null;
			} else {
				return attr.getField(fieldIdx);
			}
		}
		
		public static function getFirstFocusableFieldIndex(attrDefn:AttributeDefinitionProxy):int {
			if ( attrDefn is DateAttributeDefinitionProxy ) {
				return 2;
			} else {
				return 0;
			}
		}
		
		public static function getLastFocusableFieldIndex(attrDefn:AttributeDefinitionProxy):int {
			if ( attrDefn is DateAttributeDefinitionProxy ) {
				return 0;
			} else {
				return 0;
			}
		}
		
		public static function setFocusOnAttributeInSiblingEntity(field:FieldProxy,
					 offset:int, circularLookup:Boolean = false):Boolean {
			var attribute:AttributeProxy = field.parent;
			var attrDefn:AttributeDefinitionProxy = AttributeDefinitionProxy(attribute.definition);
			var fieldIndex:int = field.index;
			var attributeToFocusIn:AttributeProxy = null;
			var fieldToFocusIn:FieldProxy = null;
			var circularLookupApplied:Boolean = false;
			if ( attrDefn.multiple && ! attrDefn is CodeAttributeDefinitionProxy ) {
				attributeToFocusIn = AttributeProxy(attribute.getSibling(offset));
			} else {
				var parentMultipleEntity:EntityProxy = attribute.getParentMultipleEntity();
				if ( parentMultipleEntity.getSiblings().length == 1 ) {
					return false;
				} else {
					var siblingEntity:EntityProxy = EntityProxy(parentMultipleEntity.getSibling(offset, circularLookup));
					if ( siblingEntity != null ) {
						attributeToFocusIn = siblingEntity.getDescendantSingleAttribute(attrDefn.id);
						circularLookupApplied = circularLookup && siblingEntity.index - parentMultipleEntity.index != offset;
					}
				}
			}
			if ( attributeToFocusIn == null ) {
				return false;
			} else if ( fieldToFocusIn == null ) {
				var attrToFocusInDefn:AttributeDefinitionProxy = AttributeDefinitionProxy(attributeToFocusIn.definition);
				var newFieldIndex:int;
				if ( attrToFocusInDefn == attrDefn && attributeToFocusIn != attribute && ! attrToFocusInDefn.hasAlwaysHorizontalLayout() ) {
					newFieldIndex = fieldIndex;
				} else if ( offset > 0 ) {
					newFieldIndex = getFirstFocusableFieldIndex(attrToFocusInDefn);
				} else {
					newFieldIndex = getLastFocusableFieldIndex(attrToFocusInDefn);
				}
				fieldToFocusIn = attributeToFocusIn.getField(newFieldIndex);
				if ( circularLookupApplied ) {
					//move to next field
					fieldToFocusIn = getSiblingFocusableField(fieldToFocusIn, offset > 0 ? 1: -1, false);
				}
			}
			return dispatchFocusSetEvent(fieldToFocusIn);
		}
		
		public static function setFocusOnBoundaryFieldInSiblingEntity(field:FieldProxy, offset:int, firstField:Boolean = true):Boolean {
			var attribute:AttributeProxy = field.parent;
			var parentMultipleEntity:EntityProxy = attribute.getParentMultipleEntity();
			var siblingEntity:EntityProxy = EntityProxy(parentMultipleEntity.getSibling(offset, false));
			if ( siblingEntity == null ) {
				return false;
			} else {
				var leafFocusableFields:IList = getLeafFocusableFields(siblingEntity);
				var fieldToFocusIdx:int = firstField ? 0: leafFocusableFields.length - 1;
				var fieldToFocus:FieldProxy = leafFocusableFields.getItemAt(fieldToFocusIdx) as FieldProxy;
				return dispatchFocusSetEvent(fieldToFocus);
			}
		}
		
		public static function setFocusOnSiblingField(field:FieldProxy, offset:int, limit:Boolean = false):Boolean {
			var fieldToFocusIn:FieldProxy = getSiblingFocusableField(field, offset, limit);
			return dispatchFocusSetEvent(fieldToFocusIn);
		}
		
		public static function setFocusOnSiblingFieldInAttribute(field:FieldProxy, offset:int):Boolean {
			var sibling:FieldProxy = getSiblingFieldInAttribute(field, offset);
			return dispatchFocusSetEvent(sibling);
		}
		
		public static function getSiblingFieldInAttribute(field:FieldProxy, offset:int):FieldProxy {
			var focusableFields:IList = getFocusableFieldsInOrder(field.parent);
			var adaptedIdx:int = focusableFields.getItemIndex(field);
			var siblingIdx:int = adaptedIdx + offset;
			if ( siblingIdx >= 0 && siblingIdx < focusableFields.length ) {
				return FieldProxy(focusableFields.getItemAt(siblingIdx));
			} else {
				return null;
			}
		}
		
		public static function dispatchFocusSetEvent(field:FieldProxy):Boolean {
			if ( field == null) {
				return false;
			} else {
				var attributeToFocusIn:AttributeProxy = field.parent;
				var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.SET_FOCUS);
				inputFieldEvent.fieldIdx = attributeToFocusIn is CodeAttributeProxy ? -1: field.index;
				inputFieldEvent.attributeId = attributeToFocusIn.id;
				inputFieldEvent.nodeName = attributeToFocusIn.name;
				inputFieldEvent.parentEntityId = attributeToFocusIn.parentId;
				EventDispatcherFactory.getEventDispatcher().dispatchEvent(inputFieldEvent);
				return true;
			}
		}
		
	}
}