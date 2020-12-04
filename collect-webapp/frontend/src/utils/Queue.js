export default class Queue {
  arr = []

  enqueue(item) {
    this.arr.push(item)
    return this
  }

  enqueueItems(items) {
    this.arr.push(...items)
    return this
  }

  dequeue() {
    return this.isEmpty() ? undefined : this.arr.shift()
  }

  isEmpty() {
    return this.arr.length === 0
  }
}
