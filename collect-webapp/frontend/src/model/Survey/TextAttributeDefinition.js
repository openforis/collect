import { AttributeDefinition } from './AttributeDefinition'

export class TextAttributeDefinition extends AttributeDefinition {
  textType

  static TextTypes = {
    SHORT: 'SHORT',
    MEMO: 'MEMO',
  }
}
