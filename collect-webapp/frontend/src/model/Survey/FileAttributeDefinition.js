import { AttributeDefinition } from './AttributeDefinition'

export class FileAttributeDefinition extends AttributeDefinition {
  fileType
  extensions
  maxSize

  static FileTypes = {
    AUDIO: 'AUDIO',
    DOCUMENT: 'DOCUMENT',
    IMAGE: 'IMAGE',
    VIDEO: 'VIDEO',
  }
}
