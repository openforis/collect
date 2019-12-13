'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _const = require('../const');

var _const2 = _interopRequireDefault(_const);

var _selection = require('../store/selection');

var _rows = require('../store/rows');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; } /* eslint no-param-reassign: 0 */


exports.default = function (Base) {
  var _class, _temp;

  return _temp = _class = function (_Component) {
    _inherits(RowSelectionWrapper, _Component);

    function RowSelectionWrapper(props) {
      _classCallCheck(this, RowSelectionWrapper);

      var _this = _possibleConstructorReturn(this, (RowSelectionWrapper.__proto__ || Object.getPrototypeOf(RowSelectionWrapper)).call(this, props));

      _this.handleRowSelect = _this.handleRowSelect.bind(_this);
      _this.handleAllRowsSelect = _this.handleAllRowsSelect.bind(_this);

      props.store.selected = props.selectRow.selected || [];
      _this.state = {
        selectedRowKeys: props.store.selected
      };
      return _this;
    }

    _createClass(RowSelectionWrapper, [{
      key: 'componentWillReceiveProps',
      value: function componentWillReceiveProps(nextProps) {
        nextProps.store.selected = nextProps.selectRow.selected || [];
        this.setState(function () {
          return {
            selectedRowKeys: nextProps.store.selected
          };
        });
      }

      /**
       * row selection handler
       * @param {String} rowKey - row key of what was selected.
       * @param {Boolean} checked - next checked status of input button.
       */

    }, {
      key: 'handleRowSelect',
      value: function handleRowSelect(rowKey, checked, rowIndex, e) {
        var _props = this.props,
            _props$selectRow = _props.selectRow,
            mode = _props$selectRow.mode,
            onSelect = _props$selectRow.onSelect,
            store = _props.store;
        var ROW_SELECT_SINGLE = _const2.default.ROW_SELECT_SINGLE;


        var currSelected = [].concat(_toConsumableArray(store.selected));

        if (mode === ROW_SELECT_SINGLE) {
          // when select mode is radio
          currSelected = [rowKey];
        } else if (checked) {
          // when select mode is checkbox
          currSelected.push(rowKey);
        } else {
          currSelected = currSelected.filter(function (value) {
            return value !== rowKey;
          });
        }

        store.selected = currSelected;

        if (onSelect) {
          var row = (0, _rows.getRowByRowId)(store)(rowKey);
          onSelect(row, checked, rowIndex, e);
        }

        this.setState(function () {
          return {
            selectedRowKeys: currSelected
          };
        });
      }

      /**
       * handle all rows selection on header cell by store.selected
       */

    }, {
      key: 'handleAllRowsSelect',
      value: function handleAllRowsSelect(e) {
        var _props2 = this.props,
            store = _props2.store,
            _props2$selectRow = _props2.selectRow,
            onSelectAll = _props2$selectRow.onSelectAll,
            nonSelectable = _props2$selectRow.nonSelectable;

        var selected = (0, _selection.isAnySelectedRow)(store)(nonSelectable);

        var result = !selected;

        var currSelected = result ? (0, _selection.selectableKeys)(store)(nonSelectable) : (0, _selection.unSelectableKeys)(store)(nonSelectable);

        store.selected = currSelected;

        if (onSelectAll) {
          onSelectAll(result, (0, _selection.getSelectedRows)(store), e);
        }

        this.setState(function () {
          return {
            selectedRowKeys: currSelected
          };
        });
      }
    }, {
      key: 'render',
      value: function render() {
        return _react2.default.createElement(Base, _extends({}, this.props, {
          onRowSelect: this.handleRowSelect,
          onAllRowsSelect: this.handleAllRowsSelect
        }));
      }
    }]);

    return RowSelectionWrapper;
  }(_react.Component), _class.propTypes = {
    store: _propTypes2.default.object.isRequired,
    selectRow: _propTypes2.default.object.isRequired
  }, _temp;
};