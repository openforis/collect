'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _utils = require('../utils');

var _utils2 = _interopRequireDefault(_utils);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

exports.default = function (ExtendBase) {
  return function (_ExtendBase) {
    _inherits(RemoteResolver, _ExtendBase);

    function RemoteResolver() {
      _classCallCheck(this, RemoteResolver);

      return _possibleConstructorReturn(this, (RemoteResolver.__proto__ || Object.getPrototypeOf(RemoteResolver)).apply(this, arguments));
    }

    _createClass(RemoteResolver, [{
      key: 'getNewestState',
      value: function getNewestState() {
        var state = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

        var store = this.store || this.props.store;
        return _extends({
          page: store.page,
          sizePerPage: store.sizePerPage,
          filters: store.filters,
          sortField: store.sortField,
          sortOrder: store.sortOrder,
          data: store.getAllData()
        }, state);
      }
    }, {
      key: 'isRemotePagination',
      value: function isRemotePagination() {
        var remote = this.props.remote;

        return remote === true || _utils2.default.isObject(remote) && remote.pagination;
      }
    }, {
      key: 'isRemoteFiltering',
      value: function isRemoteFiltering() {
        var remote = this.props.remote;

        return remote === true || _utils2.default.isObject(remote) && remote.filter;
      }
    }, {
      key: 'isRemoteSort',
      value: function isRemoteSort() {
        var remote = this.props.remote;

        return remote === true || _utils2.default.isObject(remote) && remote.sort;
      }
    }, {
      key: 'isRemoteCellEdit',
      value: function isRemoteCellEdit() {
        var remote = this.props.remote;

        return remote === true || _utils2.default.isObject(remote) && remote.cellEdit;
      }
    }, {
      key: 'handleRemotePageChange',
      value: function handleRemotePageChange() {
        this.props.onTableChange('pagination', this.getNewestState());
      }
    }, {
      key: 'handleRemoteFilterChange',
      value: function handleRemoteFilterChange() {
        var newState = {};
        if (this.isRemotePagination()) {
          var options = this.props.pagination.options || {};
          newState.page = _utils2.default.isDefined(options.pageStartIndex) ? options.pageStartIndex : 1;
        }
        this.props.onTableChange('filter', this.getNewestState(newState));
      }
    }, {
      key: 'handleSortChange',
      value: function handleSortChange() {
        this.props.onTableChange('sort', this.getNewestState());
      }
    }, {
      key: 'handleCellChange',
      value: function handleCellChange(rowId, dataField, newValue) {
        var cellEdit = { rowId: rowId, dataField: dataField, newValue: newValue };
        this.props.onTableChange('cellEdit', this.getNewestState({ cellEdit: cellEdit }));
      }
    }]);

    return RemoteResolver;
  }(ExtendBase);
};