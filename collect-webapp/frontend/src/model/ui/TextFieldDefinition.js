import { FieldDefinition } from './FieldDefinition'

export class TextFieldDefinition extends FieldDefinition {
  textTranform

  static TextTranform = {
    NONE: 'NONE',
    UPPERCASE: 'UPPERCASE',
    LOWERCASE: 'LOWERCASE',
    CAMELCASE: 'CAMELCASE',
  }
}
