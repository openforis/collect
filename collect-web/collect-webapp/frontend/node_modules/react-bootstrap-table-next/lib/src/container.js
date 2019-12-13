'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _store = require('./store');

var _store2 = _interopRequireDefault(_store);

var _wrapper = require('./sort/wrapper');

var _wrapper2 = _interopRequireDefault(_wrapper);

var _wrapper3 = require('./row-selection/wrapper');

var _wrapper4 = _interopRequireDefault(_wrapper3);

var _remoteResolver2 = require('./props-resolver/remote-resolver');

var _remoteResolver3 = _interopRequireDefault(_remoteResolver2);

var _utils = require('./utils');

var _utils2 = _interopRequireDefault(_utils);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; } /* eslint no-return-assign: 0 */
/* eslint react/prop-types: 0 */


var withDataStore = function withDataStore(Base) {
  return function (_remoteResolver) {
    _inherits(BootstrapTableContainer, _remoteResolver);

    function BootstrapTableContainer(props) {
      _classCallCheck(this, BootstrapTableContainer);

      var _this = _possibleConstructorReturn(this, (BootstrapTableContainer.__proto__ || Object.getPrototypeOf(BootstrapTableContainer)).call(this, props));

      _this.store = new _store2.default(props.keyField);
      _this.store.data = props.data;
      _this.wrapComponents();
      return _this;
    }

    _createClass(BootstrapTableContainer, [{
      key: 'componentWillReceiveProps',
      value: function componentWillReceiveProps(nextProps) {
        this.store.setAllData(nextProps.data);
      }
    }, {
      key: 'wrapComponents',
      value: function wrapComponents() {
        this.BaseComponent = Base;
        var _props = this.props,
            pagination = _props.pagination,
            columns = _props.columns,
            filter = _props.filter,
            selectRow = _props.selectRow,
            cellEdit = _props.cellEdit;

        if (pagination) {
          var wrapperFactory = pagination.wrapperFactory;

          this.BaseComponent = wrapperFactory(this.BaseComponent, {
            remoteResolver: _remoteResolver3.default
          });
        }

        if (columns.filter(function (col) {
          return col.sort;
        }).length > 0) {
          this.BaseComponent = (0, _wrapper2.default)(this.BaseComponent);
        }

        if (filter) {
          var _wrapperFactory = filter.wrapperFactory;

          this.BaseComponent = _wrapperFactory(this.BaseComponent, {
            _: _utils2.default,
            remoteResolver: _remoteResolver3.default
          });
        }

        if (cellEdit) {
          var _wrapperFactory2 = cellEdit.wrapperFactory;

          this.BaseComponent = _wrapperFactory2(this.BaseComponent, {
            _: _utils2.default,
            remoteResolver: _remoteResolver3.default
          });
        }

        if (selectRow) {
          this.BaseComponent = (0, _wrapper4.default)(this.BaseComponent);
        }
      }
    }, {
      key: 'render',
      value: function render() {
        var baseProps = _extends({}, this.props, {
          store: this.store
        });

        return _react2.default.createElement(this.BaseComponent, baseProps);
      }
    }]);

    return BootstrapTableContainer;
  }((0, _remoteResolver3.default)(_react.Component));
};

exports.default = withDataStore;