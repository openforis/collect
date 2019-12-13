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

var _formComponent = require('./form-component');

var _formComponent2 = _interopRequireDefault(_formComponent);

var _formsReducer = require('../reducers/forms-reducer');

var _formsReducer2 = _interopRequireDefault(_formsReducer);

var _redux = require('redux');

var _omit = require('../utils/omit');

var _omit2 = _interopRequireDefault(_omit);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var LocalForm = function (_React$Component) {
  _inherits(LocalForm, _React$Component);

  function LocalForm(props) {
    _classCallCheck(this, LocalForm);

    var _this = _possibleConstructorReturn(this, (LocalForm.__proto__ || Object.getPrototypeOf(LocalForm)).call(this, props));

    _this.store = props.store || (0, _redux.createStore)((0, _formsReducer2.default)(_defineProperty({}, props.model, props.initialState)));

    _this.dispatch = function (action) {
      if (typeof action === 'function') {
        return action(_this.store.dispatch, _this.store.getState);
      }

      return _this.store.dispatch(action);
    };
    return _this;
  }

  _createClass(LocalForm, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      if (this.props.getDispatch) {
        this.props.getDispatch(this.dispatch);
      }
    }
  }, {
    key: 'render',
    value: function render() {
      var allowedProps = (0, _omit2.default)(this.props, ['store', 'initialState']);

      return _react2.default.createElement(_formComponent2.default, _extends({ store: this.store }, allowedProps));
    }
  }]);

  return LocalForm;
}(_react2.default.Component);

LocalForm.displayName = 'LocalForm';

process.env.NODE_ENV !== "production" ? LocalForm.propTypes = {
  store: _propTypes2.default.shape({
    subscribe: _propTypes2.default.func,
    dispatch: _propTypes2.default.func,
    getState: _propTypes2.default.func
  }),

  // provided props
  initialState: _propTypes2.default.any,
  model: _propTypes2.default.string.isRequired,
  getDispatch: _propTypes2.default.func
} : void 0;

LocalForm.defaultProps = {
  initialState: {},
  model: 'local'
};

exports.default = LocalForm;