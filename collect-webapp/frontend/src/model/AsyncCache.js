class AsyncCache {
  constructor() {
    this.maxSize = 100 // maximum number of keys to be stored
    this.keysSortedByLastAccess = [] // used to determine the key to delete when limit is reached
    this.itemsByKey = {} // items indexed by keys
    this.itemsFetchingByKey = {} // items
    this.itemsFetchListenersByKey = {}
  }

  async getItem({ fetchFunction, params }) {
    return new Promise((resolve, reject) => {
      const key = this._getKey(params)
      this._trackAccess({ key })
      const item = this.itemsByKey[key]
      if (item) {
        return resolve(item)
      }
      this._addFetchListener({ key, callbacks: [resolve, reject] })
      if (!this._isFetching(key)) {
        return this._startFetch({ key, fetchFunction, params })
      }
    })
  }

  _getKey(params) {
    return JSON.stringify(Object.values(params).join('_'))
  }

  _trackAccess({ key }) {
    this._deleteKeySortedByLastAccess(key)
    this.keysSortedByLastAccess.push(key)
  }

  _startFetch({ key, fetchFunction, params }) {
    this.itemsFetchingByKey[key] = true

    const $this = this
    fetchFunction
      .apply(null, [params])
      .then((item) => {
        $this.itemsByKey[key] = item
        $this.itemsFetchingByKey[key] = false
        $this._notifyListeners({ key, item })

        if ($this.keysSortedByLastAccess.length > $this.maxSize) {
          $this._deleteElderKey()
        }
      })
      .catch((error) => {
        $this._notifyListeners({ key, error })
      })
  }

  _addFetchListener({ key, callbacks }) {
    let listeners = this.itemsFetchListenersByKey[key]
    if (!listeners) {
      listeners = []
      this.itemsFetchListenersByKey[key] = listeners
    }
    listeners.push(callbacks)
  }

  _notifyListeners({ key, item, error }) {
    const listeners = this.itemsFetchListenersByKey[key]
    while (listeners.length > 0) {
      const [resolve, reject] = listeners.pop()
      if (error) {
        reject(error)
      } else {
        resolve(item)
      }
    }
  }

  _deleteElderKey() {
    const $this = this
    const keyToDelete = this.keysSortedByLastAccess.find((key) => !$this._isFetching(key) && !$this._hasListeners(key))
    if (keyToDelete) {
      this._deleteKeySortedByLastAccess(keyToDelete)
      delete this.itemsByKey[keyToDelete]
      delete this.itemsFetchListenersByKey[keyToDelete]
      delete this.itemsFetchingByKey[keyToDelete]
    }
  }

  _isFetching(key) {
    return this.itemsFetchingByKey && this.itemsFetchingByKey[key]
  }

  _hasListeners(key) {
    const listeners = (this.itemsFetchListenersByKey ? this.itemsFetchListenersByKey[key] : []) || []
    return listeners.length > 0
  }

  _deleteKeySortedByLastAccess(key) {
    const idx = this.keysSortedByLastAccess.indexOf(key)
    if (idx >= 0) {
      this.keysSortedByLastAccess.splice(idx, 1)
    }
  }
}

export default AsyncCache
