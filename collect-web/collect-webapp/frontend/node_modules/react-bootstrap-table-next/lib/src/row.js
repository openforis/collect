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

var _utils = require('./utils');

var _utils2 = _interopRequireDefault(_utils);

var _cell = require('./cell');

var _cell2 = _interopRequireDefault(_cell);

var _selectionCell = require('./row-selection/selection-cell');

var _selectionCell2 = _interopRequireDefault(_selectionCell);

var _rowEventDelegater = require('./row-event-delegater');

var _rowEventDelegater2 = _interopRequireDefault(_rowEventDelegater);

var _const = require('./const');

var _const2 = _interopRequireDefault(_const);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; } /* eslint react/prop-types: 0 */
/* eslint react/no-array-index-key: 0 */


var Row = function (_eventDelegater) {
  _inherits(Row, _eventDelegater);

  function Row() {
    _classCallCheck(this, Row);

    return _possibleConstructorReturn(this, (Row.__proto__ || Object.getPrototypeOf(Row)).apply(this, arguments));
  }

  _createClass(Row, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          row = _props.row,
          columns = _props.columns,
          keyField = _props.keyField,
          rowIndex = _props.rowIndex,
          className = _props.className,
          style = _props.style,
          attrs = _props.attrs,
          cellEdit = _props.cellEdit,
          selected = _props.selected,
          selectRow = _props.selectRow,
          selectable = _props.selectable,
          editableRow = _props.editable;

      var mode = cellEdit.mode,
          onStart = cellEdit.onStart,
          EditingCell = cellEdit.EditingCell,
          editingRowIdx = cellEdit.ridx,
          editingColIdx = cellEdit.cidx,
          CLICK_TO_CELL_EDIT = cellEdit.CLICK_TO_CELL_EDIT,
          DBCLICK_TO_CELL_EDIT = cellEdit.DBCLICK_TO_CELL_EDIT,
          rest = _objectWithoutProperties(cellEdit, ['mode', 'onStart', 'EditingCell', 'ridx', 'cidx', 'CLICK_TO_CELL_EDIT', 'DBCLICK_TO_CELL_EDIT']);

      var key = _utils2.default.get(row, keyField);
      var hideSelectColumn = selectRow.hideSelectColumn;

      var trAttrs = this.delegate(attrs);

      return _react2.default.createElement(
        'tr',
        _extends({ style: style, className: className }, trAttrs),
        selectRow.mode !== _const2.default.ROW_SELECT_DISABLED && !hideSelectColumn ? _react2.default.createElement(_selectionCell2.default, _extends({}, selectRow, {
          rowKey: key,
          rowIndex: rowIndex,
          selected: selected,
          disabled: !selectable
        })) : null,
        columns.map(function (column, index) {
          if (!column.hidden) {
            var dataField = column.dataField;

            var content = _utils2.default.get(row, dataField);
            var editable = _utils2.default.isDefined(column.editable) ? column.editable : true;
            if (dataField === keyField || !editableRow) editable = false;
            if (_utils2.default.isFunction(column.editable)) {
              editable = column.editable(content, row, rowIndex, index);
            }
            if (rowIndex === editingRowIdx && index === editingColIdx) {
              var editCellstyle = column.editCellStyle || {};
              var editCellclasses = column.editCellClasses;
              if (_utils2.default.isFunction(column.editCellStyle)) {
                editCellstyle = column.editCellStyle(content, row, rowIndex, index);
              }
              if (_utils2.default.isFunction(column.editCellClasses)) {
                editCellclasses = column.editCellClasses(content, row, rowIndex, index);
              }
              return _react2.default.createElement(EditingCell, _extends({
                key: content + '-' + index,
                row: row,
                rowIndex: rowIndex,
                column: column,
                columnIndex: index,
                className: editCellclasses,
                style: editCellstyle
              }, rest));
            }
            return _react2.default.createElement(_cell2.default, {
              key: content + '-' + index,
              row: row,
              rowIndex: rowIndex,
              columnIndex: index,
              column: column,
              onStart: onStart,
              editable: editable,
              clickToEdit: mode === CLICK_TO_CELL_EDIT,
              dbclickToEdit: mode === DBCLICK_TO_CELL_EDIT
            });
          }
          return false;
        })
      );
    }
  }]);

  return Row;
}((0, _rowEventDelegater2.default)(_react.Component));

Row.propTypes = {
  row: _propTypes2.default.object.isRequired,
  rowIndex: _propTypes2.default.number.isRequired,
  columns: _propTypes2.default.array.isRequired,
  style: _propTypes2.default.object,
  className: _propTypes2.default.string,
  attrs: _propTypes2.default.object
};

Row.defaultProps = {
  editable: true,
  style: {},
  className: null,
  attrs: {}
};

exports.default = Row;