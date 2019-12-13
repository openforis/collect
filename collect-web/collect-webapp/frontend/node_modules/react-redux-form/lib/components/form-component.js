'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createFormClass = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _reactRedux = require('react-redux');

var _shallowEqual = require('../utils/shallow-equal');

var _shallowEqual2 = _interopRequireDefault(_shallowEqual);

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _omit = require('../utils/omit');

var _omit2 = _interopRequireDefault(_omit);

var _actions = require('../actions');

var _actions2 = _interopRequireDefault(_actions);

var _getValidity = require('../utils/get-validity');

var _getValidity2 = _interopRequireDefault(_getValidity);

var _invertValidators = require('../utils/invert-validators');

var _invertValidators2 = _interopRequireDefault(_invertValidators);

var _isValidityInvalid = require('../utils/is-validity-invalid');

var _isValidityInvalid2 = _interopRequireDefault(_isValidityInvalid);

var _isValid = require('../form/is-valid');

var _isValid2 = _interopRequireDefault(_isValid);

var _getForm = require('../utils/get-form');

var _getForm2 = _interopRequireDefault(_getForm);

var _getModel = require('../utils/get-model');

var _getModel2 = _interopRequireDefault(_getModel);

var _getField = require('../utils/get-field');

var _getField2 = _interopRequireDefault(_getField);

var _deepCompareChildren = require('../utils/deep-compare-children');

var _deepCompareChildren2 = _interopRequireDefault(_deepCompareChildren);

var _containsEvent = require('../utils/contains-event');

var _containsEvent2 = _interopRequireDefault(_containsEvent);

var _mergeValidity = require('../utils/merge-validity');

var _mergeValidity2 = _interopRequireDefault(_mergeValidity);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var propTypes = {
  component: _propTypes2.default.any,
  validators: _propTypes2.default.oneOfType([_propTypes2.default.object, _propTypes2.default.func]),
  errors: _propTypes2.default.object,
  validateOn: _propTypes2.default.oneOf(['change', 'submit']),
  model: _propTypes2.default.string.isRequired,
  modelValue: _propTypes2.default.any,
  formValue: _propTypes2.default.object,
  onSubmit: _propTypes2.default.func,
  onSubmitFailed: _propTypes2.default.func,
  dispatch: _propTypes2.default.func,
  children: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.node]),
  store: _propTypes2.default.shape({
    subscribe: _propTypes2.default.func,
    dispatch: _propTypes2.default.func,
    getState: _propTypes2.default.func
  }),
  storeSubscription: _propTypes2.default.any,
  onUpdate: _propTypes2.default.func,
  onChange: _propTypes2.default.func,
  getRef: _propTypes2.default.func,
  getDispatch: _propTypes2.default.func,
  onBeforeSubmit: _propTypes2.default.func,
  hideNativeErrors: _propTypes2.default.bool,

  // standard HTML attributes
  action: _propTypes2.default.string,
  noValidate: _propTypes2.default.bool
};

var htmlAttributes = ['action', 'noValidate'];
var disallowedPropTypeKeys = Object.keys(propTypes).filter(function (key) {
  return htmlAttributes.indexOf(key) === -1;
});

var defaultStrategy = {
  get: _get3.default,
  getForm: _getForm2.default,
  actions: _actions2.default
};

function createFormClass() {
  var s = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategy;

  var Form = function (_Component) {
    _inherits(Form, _Component);

    function Form(props) {
      _classCallCheck(this, Form);

      var _this = _possibleConstructorReturn(this, (Form.__proto__ || Object.getPrototypeOf(Form)).call(this, props));

      _this.handleSubmit = _this.handleSubmit.bind(_this);
      _this.handleReset = _this.handleReset.bind(_this);
      _this.handleValidSubmit = _this.handleValidSubmit.bind(_this);
      _this.handleInvalidSubmit = _this.handleInvalidSubmit.bind(_this);
      _this.attachNode = _this.attachNode.bind(_this);

      _this.state = {
        lastSubmitEvent: null
      };
      return _this;
    }

    _createClass(Form, [{
      key: 'getChildContext',
      value: function getChildContext() {
        return {
          model: this.props.model,
          localStore: this.props.store
        };
      }
    }, {
      key: 'componentDidMount',
      value: function componentDidMount() {
        if ((0, _containsEvent2.default)(this.props.validateOn, 'change')) {
          this.validate(this.props, true);
        }

        if (this.props.getDispatch) {
          this.props.getDispatch(this.props.dispatch);
        }
      }
    }, {
      key: 'componentWillReceiveProps',
      value: function componentWillReceiveProps(nextProps) {
        if ((0, _containsEvent2.default)(nextProps.validateOn, 'change')) {
          this.validate(nextProps);
        }
      }
    }, {
      key: 'shouldComponentUpdate',
      value: function shouldComponentUpdate(nextProps, nextState) {
        return (0, _deepCompareChildren2.default)(this, nextProps, nextState);
      }
    }, {
      key: 'componentDidUpdate',
      value: function componentDidUpdate(prevProps) {
        this.handleIntents();

        if (!(0, _shallowEqual2.default)(prevProps.formValue, this.props.formValue)) {
          this.handleUpdate();
        }

        if (!(0, _shallowEqual2.default)(prevProps.modelValue, this.props.modelValue)) {
          this.handleChange();
        }
      }
    }, {
      key: 'handleUpdate',
      value: function handleUpdate() {
        if (this.props.onUpdate) {
          this.props.onUpdate(this.props.formValue);
        }
      }
    }, {
      key: 'handleChange',
      value: function handleChange() {
        if (this.props.onChange) {
          this.props.onChange(this.props.modelValue);
        }
      }
    }, {
      key: 'attachNode',
      value: function attachNode(node) {
        if (!node) return;

        this._node = node;

        this._node.submit = this.handleSubmit;
        if (this.props.getRef) this.props.getRef(node);
      }
    }, {
      key: 'validate',
      value: function validate(nextProps) {
        var _this2 = this;

        var initial = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
        var submit = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
        var _props = this.props,
            model = _props.model,
            dispatch = _props.dispatch,
            formValue = _props.formValue,
            modelValue = _props.modelValue;
        var validators = nextProps.validators,
            errors = nextProps.errors;


        if (!formValue) return;

        if (!validators && !errors && modelValue !== nextProps.modelValue) {
          return;
        }

        var validatorsChanged = validators !== this.props.validators || errors !== this.props.errors;
        var fieldKeys = (validators ? Object.keys(validators) : []).concat(errors ? Object.keys(errors) : []);

        var fieldsErrors = {};
        var validityChanged = false;

        var keysToValidate = [];

        fieldKeys.forEach(function (key) {
          if (!!~keysToValidate.indexOf(key)) return;

          var valuesChanged = key === '' ? modelValue !== nextProps.modelValue : s.get(modelValue, key) !== s.get(nextProps.modelValue, key);

          if (submit || initial || valuesChanged || validators && _this2.props.validators[key] !== validators[key] || errors && _this2.props.errors[key] !== errors[key] || !!~key.indexOf('[]')) {
            keysToValidate.push(key);
          }
        });

        var validateField = function validateField(field, errorValidator) {
          if (!!~field.indexOf('[]')) {
            var _field$split = field.split('[]'),
                _field$split2 = _slicedToArray(_field$split, 2),
                parentModel = _field$split2[0],
                childModel = _field$split2[1];

            var nextValue = parentModel ? s.get(nextProps.modelValue, parentModel) : nextProps.modelValue;

            nextValue.forEach(function (subValue, index) {
              validateField(parentModel + '[' + index + ']' + childModel, errorValidator);
            });
          } else {
            var _nextValue = field ? s.get(nextProps.modelValue, field) : nextProps.modelValue;

            var currentErrors = (0, _getField2.default)(formValue, field).errors;
            var fieldErrors = (0, _getValidity2.default)(errorValidator, _nextValue);

            if (!validityChanged && !(0, _shallowEqual2.default)(fieldErrors, currentErrors)) {
              validityChanged = true;
            }

            fieldsErrors[field] = (0, _mergeValidity2.default)(fieldsErrors[field], fieldErrors);
          }
        };

        keysToValidate.forEach(function (field) {
          if (validators && validators[field]) {
            validateField(field, (0, _invertValidators2.default)(validators[field]));
          }
          if (errors && errors[field]) {
            validateField(field, errors[field]);
          }
        });

        if (typeof validators === 'function') {
          var nextValue = nextProps.modelValue;
          var currentValue = modelValue;

          if (!submit && !initial && !validatorsChanged && nextValue === currentValue) {
            // If neither the validators nor the values have changed,
            // the validity didn't change.
            return;
          }

          var multiFieldErrors = (0, _getValidity2.default)(validators, nextValue);

          if (multiFieldErrors) {
            Object.keys(multiFieldErrors).forEach(function (key) {
              // key will be the model value to apply errors to.
              var fieldErrors = multiFieldErrors[key];
              var currentErrors = (0, _getField2.default)(formValue, key).errors;

              // Invert validators
              Object.keys(fieldErrors).forEach(function (validationName) {
                fieldErrors[validationName] = !fieldErrors[validationName];
              });

              if (!validityChanged && !(0, _shallowEqual2.default)(fieldErrors, currentErrors)) {
                validityChanged = true;
              }

              fieldsErrors[key] = (0, _mergeValidity2.default)(fieldsErrors[key], fieldErrors);
            });
          }
        }

        // Compute form-level validity
        if (!fieldsErrors.hasOwnProperty('') && !~fieldKeys.indexOf('')) {
          fieldsErrors[''] = false;
          validityChanged = validityChanged || (0, _isValidityInvalid2.default)(formValue.$form.errors);
        }

        if (validityChanged) {
          dispatch(s.actions.setFieldsErrors(model, fieldsErrors, { merge: true }));
        }

        if (submit) {
          dispatch(s.actions.addIntent(model, { type: 'submit' }));
        }
      }
    }, {
      key: 'handleValidSubmit',
      value: function handleValidSubmit(options) {
        var _props2 = this.props,
            dispatch = _props2.dispatch,
            model = _props2.model,
            modelValue = _props2.modelValue,
            onSubmit = _props2.onSubmit;


        dispatch(s.actions.setPending(model, true, options));

        if (onSubmit) onSubmit(modelValue, this.state.lastSubmitEvent);
      }
    }, {
      key: 'handleInvalidSubmit',
      value: function handleInvalidSubmit(options) {
        var _props3 = this.props,
            onSubmitFailed = _props3.onSubmitFailed,
            formValue = _props3.formValue,
            dispatch = _props3.dispatch;


        if (onSubmitFailed) {
          onSubmitFailed(formValue);
        }

        dispatch(s.actions.setSubmitFailed(this.props.model, true, options));
      }
    }, {
      key: 'handleReset',
      value: function handleReset(e) {
        if (e) {
          e.preventDefault();
          e.stopPropagation();
        }

        this.props.dispatch(s.actions.reset(this.props.model));
      }
    }, {
      key: 'handleIntents',
      value: function handleIntents() {
        var _this3 = this;

        var _props4 = this.props,
            formValue = _props4.formValue,
            noValidate = _props4.noValidate;


        formValue.$form.intents.forEach(function (intent) {
          switch (intent.type) {
            case 'submit':
              {
                if (noValidate || (0, _isValid2.default)(formValue, { async: false })) {
                  _this3.handleValidSubmit({ clearIntents: intent });
                } else {
                  _this3.handleInvalidSubmit({ clearIntents: intent });
                }

                return;
              }

            default:
              return;
          }
        });
      }
    }, {
      key: 'handleSubmit',
      value: function handleSubmit(e) {
        if (e) {
          if (!this.props.action) e.preventDefault();
          e.stopPropagation();
        }
        if (e && e.persist) e.persist();

        var _props5 = this.props,
            modelValue = _props5.modelValue,
            formValue = _props5.formValue,
            onSubmit = _props5.onSubmit,
            validators = _props5.validators,
            onBeforeSubmit = _props5.onBeforeSubmit;


        if (onBeforeSubmit) onBeforeSubmit(e);

        var formValid = formValue ? formValue.$form.valid : true;

        if (!validators && onSubmit && formValid) {
          onSubmit(modelValue, e);

          return modelValue;
        }

        this.setState({ lastSubmitEvent: e });

        this.validate(this.props, false, true);

        return modelValue;
      }
    }, {
      key: 'render',
      value: function render() {
        var _props6 = this.props,
            component = _props6.component,
            children = _props6.children,
            formValue = _props6.formValue,
            hideNativeErrors = _props6.hideNativeErrors,
            noValidate = _props6.noValidate;


        var allowedProps = (0, _omit2.default)(this.props, disallowedPropTypeKeys);
        var renderableChildren = typeof children === 'function' ? children(formValue) : children;

        return _react2.default.createElement(component, _extends({}, allowedProps, {
          onSubmit: this.handleSubmit,
          onReset: this.handleReset,
          ref: this.attachNode,
          noValidate: hideNativeErrors || noValidate
        }), renderableChildren);
      }
    }]);

    return Form;
  }(_react.Component);

  process.env.NODE_ENV !== "production" ? Form.propTypes = propTypes : void 0;

  Form.defaultProps = {
    validateOn: 'change',
    component: 'form'
  };

  Form.childContextTypes = {
    model: _propTypes2.default.any,
    localStore: _propTypes2.default.shape({
      subscribe: _propTypes2.default.func,
      dispatch: _propTypes2.default.func,
      getState: _propTypes2.default.func
    })
  };

  function mapStateToProps(state, _ref) {
    var model = _ref.model;

    var modelString = (0, _getModel2.default)(model, state);
    var form = s.getForm(state, modelString);

    (0, _invariant2.default)(form, 'Unable to create Form component. ' + 'Could not find form for "%s" in the store.', modelString);

    return {
      model: modelString,
      modelValue: s.get(state, modelString),
      formValue: form
    };
  }

  return (0, _reactRedux.connect)(mapStateToProps)(Form);
}

exports.createFormClass = createFormClass;
exports.default = createFormClass();