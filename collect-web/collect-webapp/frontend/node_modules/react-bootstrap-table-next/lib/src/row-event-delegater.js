'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _utils = require('./utils');

var _utils2 = _interopRequireDefault(_utils);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var events = ['onClick', 'onDoubleClick', 'onMouseEnter', 'onMouseLeave'];

exports.default = function (ExtendBase) {
  return function (_ExtendBase) {
    _inherits(RowEventDelegater, _ExtendBase);

    function RowEventDelegater(props) {
      _classCallCheck(this, RowEventDelegater);

      var _this = _possibleConstructorReturn(this, (RowEventDelegater.__proto__ || Object.getPrototypeOf(RowEventDelegater)).call(this, props));

      _this.clickNum = 0;
      _this.createDefaultEventHandler = _this.createDefaultEventHandler.bind(_this);
      _this.createClickEventHandler = _this.createClickEventHandler.bind(_this);
      return _this;
    }

    _createClass(RowEventDelegater, [{
      key: 'createDefaultEventHandler',
      value: function createDefaultEventHandler(cb) {
        var _this2 = this;

        return function (e) {
          var _props = _this2.props,
              row = _props.row,
              rowIndex = _props.rowIndex;

          cb(e, row, rowIndex);
        };
      }
    }, {
      key: 'createClickEventHandler',
      value: function createClickEventHandler(cb) {
        var _this3 = this;

        return function (e) {
          var _props2 = _this3.props,
              row = _props2.row,
              selected = _props2.selected,
              keyField = _props2.keyField,
              selectable = _props2.selectable,
              rowIndex = _props2.rowIndex,
              _props2$selectRow = _props2.selectRow,
              onRowSelect = _props2$selectRow.onRowSelect,
              clickToEdit = _props2$selectRow.clickToEdit,
              _props2$cellEdit = _props2.cellEdit,
              mode = _props2$cellEdit.mode,
              DBCLICK_TO_CELL_EDIT = _props2$cellEdit.DBCLICK_TO_CELL_EDIT,
              DELAY_FOR_DBCLICK = _props2$cellEdit.DELAY_FOR_DBCLICK;


          var clickFn = function clickFn() {
            if (cb) {
              cb(e, row, rowIndex);
            }
            if (selectable) {
              var key = _utils2.default.get(row, keyField);
              onRowSelect(key, !selected, rowIndex, e);
            }
          };

          if (mode === DBCLICK_TO_CELL_EDIT && clickToEdit) {
            _this3.clickNum += 1;
            _utils2.default.debounce(function () {
              if (_this3.clickNum === 1) {
                clickFn();
              }
              _this3.clickNum = 0;
            }, DELAY_FOR_DBCLICK)();
          } else {
            clickFn();
          }
        };
      }
    }, {
      key: 'delegate',
      value: function delegate() {
        var _this4 = this;

        var attrs = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

        var newAttrs = {};
        if (this.props.selectRow && this.props.selectRow.clickToSelect) {
          newAttrs.onClick = this.createClickEventHandler(attrs.onClick);
        }
        Object.keys(attrs).forEach(function (attr) {
          if (!newAttrs[attr]) {
            if (events.includes(attr)) {
              newAttrs[attr] = _this4.createDefaultEventHandler(attrs[attr]);
            } else {
              newAttrs[attr] = attrs[attr];
            }
          }
        });
        return newAttrs;
      }
    }]);

    return RowEventDelegater;
  }(ExtendBase);
};