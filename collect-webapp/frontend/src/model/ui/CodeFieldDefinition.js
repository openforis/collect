import { FieldDefinition } from './FieldDefinition'

export class CodeFieldDefinition extends FieldDefinition {
  layout
  itemsOrientation
  showCode

  static Layouts = {
    RADIO: 'RADIO',
    DROPDOWN: 'DROPDOWN',
    TEXT: 'TEXT',
  }
}
