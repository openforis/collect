'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }(); /* eslint no-underscore-dangle: 0 */


var _utils = require('../utils');

var _utils2 = _interopRequireDefault(_utils);

var _sort = require('./sort');

var _rows = require('./rows');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Store = function () {
  function Store(keyField) {
    _classCallCheck(this, Store);

    this._data = [];
    this._filteredData = [];
    this._keyField = keyField;
    this._sortOrder = undefined;
    this._sortField = undefined;
    this._selected = [];
    this._filters = {};
    this._page = undefined;
    this._sizePerPage = undefined;
  }

  _createClass(Store, [{
    key: 'edit',
    value: function edit(rowId, dataField, newValue) {
      var row = (0, _rows.getRowByRowId)(this)(rowId);
      if (row) _utils2.default.set(row, dataField, newValue);
    }
  }, {
    key: 'setSort',
    value: function setSort(_ref, order, defaultOrder) {
      var dataField = _ref.dataField;

      this.sortOrder = (0, _sort.nextOrder)(this)(dataField, order, defaultOrder);
      this.sortField = dataField;
    }
  }, {
    key: 'sortBy',
    value: function sortBy(_ref2) {
      var sortFunc = _ref2.sortFunc;

      this.data = (0, _sort.sort)(this)(sortFunc);
    }
  }, {
    key: 'getAllData',
    value: function getAllData() {
      return this._data;
    }
  }, {
    key: 'setAllData',
    value: function setAllData(data) {
      this._data = data;
    }
  }, {
    key: 'data',
    get: function get() {
      if (Object.keys(this._filters).length > 0) {
        return this._filteredData;
      }
      return this._data;
    },
    set: function set(data) {
      if (Object.keys(this._filters).length > 0) {
        this._filteredData = data;
      } else {
        this._data = data ? JSON.parse(JSON.stringify(data)) : [];
      }
    }
  }, {
    key: 'filteredData',
    get: function get() {
      return this._filteredData;
    },
    set: function set(filteredData) {
      this._filteredData = filteredData;
    }
  }, {
    key: 'keyField',
    get: function get() {
      return this._keyField;
    },
    set: function set(keyField) {
      this._keyField = keyField;
    }
  }, {
    key: 'sortOrder',
    get: function get() {
      return this._sortOrder;
    },
    set: function set(sortOrder) {
      this._sortOrder = sortOrder;
    }
  }, {
    key: 'page',
    get: function get() {
      return this._page;
    },
    set: function set(page) {
      this._page = page;
    }
  }, {
    key: 'sizePerPage',
    get: function get() {
      return this._sizePerPage;
    },
    set: function set(sizePerPage) {
      this._sizePerPage = sizePerPage;
    }
  }, {
    key: 'sortField',
    get: function get() {
      return this._sortField;
    },
    set: function set(sortField) {
      this._sortField = sortField;
    }
  }, {
    key: 'selected',
    get: function get() {
      return this._selected;
    },
    set: function set(selected) {
      this._selected = selected;
    }
  }, {
    key: 'filters',
    get: function get() {
      return this._filters;
    },
    set: function set(filters) {
      this._filters = filters;
    }
  }]);

  return Store;
}();

exports.default = Store;