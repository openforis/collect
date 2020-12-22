import { AttributeDefinition } from './AttributeDefinition'

export class TextAttributeDefinition extends AttributeDefinition {
  static TextTypes = {
    SHORT: 'SHORT',
    MEMO: 'MEMO',
  }

  static TextTransform = {
    NONE: 'NONE',
    UPPERCASE: 'UPPERCASE',
    LOWERCASE: 'LOWERCASE',
    CAMELCASE: 'CAMELCASE',
  }

  textType
  textTransform
}
