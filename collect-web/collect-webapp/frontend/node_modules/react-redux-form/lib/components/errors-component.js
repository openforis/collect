'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createErrorsClass = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _reactRedux = require('react-redux');

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _map = require('../utils/map');

var _map2 = _interopRequireDefault(_map);

var _iteratee = require('../utils/iteratee');

var _iteratee2 = _interopRequireDefault(_iteratee);

var _isPlainObject = require('../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _omit = require('../utils/omit');

var _omit2 = _interopRequireDefault(_omit);

var _getForm = require('../utils/get-form');

var _getForm2 = _interopRequireDefault(_getForm);

var _getFieldFromState = require('../utils/get-field-from-state');

var _getFieldFromState2 = _interopRequireDefault(_getFieldFromState);

var _getModel = require('../utils/get-model');

var _getModel2 = _interopRequireDefault(_getModel);

var _isValid = require('../form/is-valid');

var _isValid2 = _interopRequireDefault(_isValid);

var _resolveModel = require('../utils/resolve-model');

var _resolveModel2 = _interopRequireDefault(_resolveModel);

var _initialFieldState = require('../constants/initial-field-state');

var _initialFieldState2 = _interopRequireDefault(_initialFieldState);

var _shallowEqual = require('../utils/shallow-equal');

var _shallowEqual2 = _interopRequireDefault(_shallowEqual);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var defaultStrategy = {
  get: _get3.default,
  getForm: _getForm2.default,
  getFieldFromState: _getFieldFromState2.default
};

var propTypes = {
  // Computed props
  modelValue: _propTypes2.default.any,
  formValue: _propTypes2.default.object,
  fieldValue: _propTypes2.default.object,

  // Provided props
  model: _propTypes2.default.string.isRequired,
  messages: _propTypes2.default.objectOf(_propTypes2.default.oneOfType([_propTypes2.default.node, _propTypes2.default.func, _propTypes2.default.bool])),
  show: _propTypes2.default.any,
  wrapper: _propTypes2.default.oneOfType([_propTypes2.default.string, _propTypes2.default.func, _propTypes2.default.element]),
  component: _propTypes2.default.oneOfType([_propTypes2.default.string, _propTypes2.default.func, _propTypes2.default.element]),
  dispatch: _propTypes2.default.func,
  dynamic: _propTypes2.default.oneOfType([_propTypes2.default.bool, _propTypes2.default.arrayOf(_propTypes2.default.string)]),
  store: _propTypes2.default.shape({
    subscribe: _propTypes2.default.func,
    dispatch: _propTypes2.default.func,
    getState: _propTypes2.default.func
  }),
  storeSubscription: _propTypes2.default.any
};

function showErrors(field, form) {
  var show = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

  if (typeof show === 'function') {
    return show(field, form);
  }

  if (!Array.isArray(show) && (typeof show === 'undefined' ? 'undefined' : _typeof(show)) !== 'object' && typeof show !== 'string') {
    return !!show;
  }

  return (0, _iteratee2.default)(show)(field);
}

function createErrorsClass() {
  var s = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategy;

  var Errors = function (_Component) {
    _inherits(Errors, _Component);

    function Errors() {
      _classCallCheck(this, Errors);

      return _possibleConstructorReturn(this, (Errors.__proto__ || Object.getPrototypeOf(Errors)).apply(this, arguments));
    }

    _createClass(Errors, [{
      key: 'shouldComponentUpdate',
      value: function shouldComponentUpdate(nextProps) {
        var fieldValue = nextProps.fieldValue,
            formValue = nextProps.formValue;
        var dynamic = this.props.dynamic;


        if (dynamic) {
          return !(0, _shallowEqual2.default)(this.props, nextProps);
        }

        return fieldValue !== this.props.fieldValue || formValue !== this.props.formValue;
      }
    }, {
      key: 'mapErrorMessages',
      value: function mapErrorMessages(errors) {
        var _this2 = this;

        var messages = this.props.messages;


        if (typeof errors === 'string') {
          return this.renderError(errors, 'error');
        }

        if (!errors) return null;

        return (0, _map2.default)(errors, function (error, key) {
          var message = messages[key];

          if (error) {
            if (message || typeof error === 'string') {
              return _this2.renderError(message || error, key);
            } else if ((0, _isPlainObject2.default)(error)) {
              return _this2.mapErrorMessages(error);
            }
          }

          return false;
        }).reduce(function (a, b) {
          return b ? a.concat(b) : a;
        }, []);
      }
    }, {
      key: 'renderError',
      value: function renderError(message, key) {
        var _props = this.props,
            component = _props.component,
            model = _props.model,
            modelValue = _props.modelValue,
            fieldValue = _props.fieldValue,
            errors = _props.fieldValue.errors;


        var errorProps = {
          key: key,
          model: model,
          modelValue: modelValue,
          fieldValue: fieldValue
        };

        var messageString = typeof message === 'function' ? message(modelValue, errors[key]) : message;

        if (!messageString) return null;

        var allowedProps = typeof component === 'function' ? errorProps : { key: key };

        return _react2.default.createElement(component, allowedProps, messageString);
      }
    }, {
      key: 'render',
      value: function render() {
        var _props2 = this.props,
            fieldValue = _props2.fieldValue,
            formValue = _props2.formValue,
            show = _props2.show,
            wrapper = _props2.wrapper;


        var allowedProps = typeof wrapper === 'function' ? this.props : (0, _omit2.default)(this.props, Object.keys(propTypes));

        if (!showErrors(fieldValue, formValue, show)) {
          return null;
        }

        var errorMessages = (0, _isValid2.default)(fieldValue) ? null : this.mapErrorMessages(fieldValue.errors);

        if (!errorMessages) return null;

        return _react2.default.createElement(wrapper, allowedProps, errorMessages);
      }
    }]);

    return Errors;
  }(_react.Component);

  process.env.NODE_ENV !== "production" ? Errors.propTypes = propTypes : void 0;

  Errors.defaultProps = {
    wrapper: 'div',
    component: 'span',
    messages: {},
    show: true,
    dynamic: true
  };

  function mapStateToProps(state, _ref) {
    var model = _ref.model;

    var modelString = (0, _getModel2.default)(model, state);

    var form = s.getForm(state, modelString);
    (0, _invariant2.default)(form, 'Unable to retrieve errors. ' + 'Could not find form for "%s" in the store.', modelString);

    var formValue = form.$form;
    var fieldValue = s.getFieldFromState(state, modelString) || _initialFieldState2.default;

    return {
      model: modelString,
      modelValue: s.get(state, modelString),
      formValue: formValue,
      fieldValue: fieldValue
    };
  }

  return (0, _resolveModel2.default)((0, _reactRedux.connect)(mapStateToProps)(Errors));
}

exports.createErrorsClass = createErrorsClass;
exports.default = createErrorsClass();