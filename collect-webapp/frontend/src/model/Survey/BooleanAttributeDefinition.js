import { AttributeDefinition } from './AttributeDefinition'

export class BooleanAttributeDefinition extends AttributeDefinition {
  layoutType

  static LayoutTypes = {
    CHECKBOX: 'CHECKBOX',
    TEXTBOX: 'TEXTBOX',
  }
}
