import { Attribute } from './Attribute'

export class FileAttribute extends Attribute {
  get value() {
    const val = super.value
    if (val === null) {
      return null
    }
    const { file_name, file_size } = val
    return { filename: file_name, size: file_size }
  }

  set value(value) {
    if (value) {
      // Workaround: File fields name are different from attribute field names
      const { filename: file_name, size: file_size } = value
      super.value = { file_name, file_size }
    } else {
      super.value = null
    }
  }
}
