'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createFieldClass = exports.controlPropsMap = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _identity = require('../utils/identity');

var _identity2 = _interopRequireDefault(_identity);

var _omit = require('../utils/omit');

var _omit2 = _interopRequireDefault(_omit);

var _isPlainObject = require('../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _pick = require('../utils/pick');

var _pick2 = _interopRequireDefault(_pick);

var _reactRedux = require('react-redux');

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

var _actions = require('../actions');

var _actions2 = _interopRequireDefault(_actions);

var _controlComponent = require('./control-component');

var _controlComponent2 = _interopRequireDefault(_controlComponent);

var _controlPropsMap2 = require('../constants/control-props-map');

var _controlPropsMap3 = _interopRequireDefault(_controlPropsMap2);

var _deepCompareChildren = require('../utils/deep-compare-children');

var _deepCompareChildren2 = _interopRequireDefault(_deepCompareChildren);

var _shallowCompareWithoutChildren = require('../utils/shallow-compare-without-children');

var _shallowCompareWithoutChildren2 = _interopRequireDefault(_shallowCompareWithoutChildren);

var _getModel = require('../utils/get-model');

var _getModel2 = _interopRequireDefault(_getModel);

var _getFieldFromState = require('../utils/get-field-from-state');

var _getFieldFromState2 = _interopRequireDefault(_getFieldFromState);

var _resolveModel = require('../utils/resolve-model');

var _resolveModel2 = _interopRequireDefault(_resolveModel);

var _getValue = require('../utils/get-value');

var _initialFieldState = require('../constants/initial-field-state');

var _initialFieldState2 = _interopRequireDefault(_initialFieldState);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var fieldPropTypes = {
  model: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.string]).isRequired,
  component: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.string]),
  parser: _propTypes2.default.func,
  formatter: _propTypes2.default.func,
  updateOn: _propTypes2.default.oneOfType([_propTypes2.default.arrayOf(_propTypes2.default.string), _propTypes2.default.string]),
  changeAction: _propTypes2.default.func,
  validators: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.object]),
  asyncValidators: _propTypes2.default.object,
  validateOn: _propTypes2.default.oneOfType([_propTypes2.default.arrayOf(_propTypes2.default.string), _propTypes2.default.string]),
  asyncValidateOn: _propTypes2.default.oneOfType([_propTypes2.default.arrayOf(_propTypes2.default.string), _propTypes2.default.string]),
  errors: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.object]),
  mapProps: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.object]),
  componentMap: _propTypes2.default.object,
  dynamic: _propTypes2.default.bool,
  dispatch: _propTypes2.default.func,
  getRef: _propTypes2.default.func,

  // Calculated props
  fieldValue: _propTypes2.default.object,
  store: _propTypes2.default.shape({
    subscribe: _propTypes2.default.func,
    dispatch: _propTypes2.default.func,
    getState: _propTypes2.default.func
  }),
  storeSubscription: _propTypes2.default.any
};

function getControlType(control, props, options) {
  var _controlPropsMap = options.controlPropsMap;


  var controlDisplayNames = Object.keys(_controlPropsMap).filter(function (controlKey) {
    var propsMap = _controlPropsMap[controlKey];

    if ((0, _isPlainObject2.default)(propsMap) && propsMap.component) {
      return control.type === propsMap.component;
    }

    return false;
  });

  if (controlDisplayNames.length) return controlDisplayNames[0];

  try {
    var controlDisplayName = control.constructor.displayName || control.type.displayName || control.type.name || control.type;

    if (controlDisplayName === 'input') {
      controlDisplayName = _controlPropsMap[control.props.type] ? control.props.type : 'text';
    }

    return _controlPropsMap[controlDisplayName] ? controlDisplayName : null;
  } catch (error) {
    return undefined;
  }
}

var defaultStrategy = {
  Control: _controlComponent2.default,
  controlPropTypes: fieldPropTypes,
  getFieldFromState: _getFieldFromState2.default,
  actions: _actions2.default
};

function createFieldClass() {
  var customControlPropsMap = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
  var s = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : defaultStrategy;

  // Use the fieldPropTypes if no controlProptypes have been defined to
  // maintain backwards compatibiltiy.
  var controlPropTypes = s.controlPropTypes || fieldPropTypes;

  function mapStateToProps(state, props) {
    var model = props.model;


    var modelString = (0, _getModel2.default)(model, state);
    var fieldValue = s.getFieldFromState(state, modelString) || _initialFieldState2.default;

    return {
      model: modelString,
      fieldValue: fieldValue
    };
  }

  var options = {
    controlPropsMap: _extends({}, _controlPropsMap3.default, customControlPropsMap)
  };

  // TODO: refactor
  var defaultControlPropsMap = {
    checkbox: {
      changeAction: s.actions.checkWithValue,
      getValue: _getValue.getCheckboxValue,
      isToggle: true
    },
    radio: {
      isToggle: true
    }
  };

  var Field = function (_Component) {
    _inherits(Field, _Component);

    function Field() {
      _classCallCheck(this, Field);

      return _possibleConstructorReturn(this, (Field.__proto__ || Object.getPrototypeOf(Field)).apply(this, arguments));
    }

    _createClass(Field, [{
      key: 'shouldComponentUpdate',
      value: function shouldComponentUpdate(nextProps, nextState) {
        var dynamic = this.props.dynamic;


        if (dynamic) {
          return (0, _deepCompareChildren2.default)(this, nextProps, nextState);
        }

        return (0, _shallowCompareWithoutChildren2.default)(this, nextProps);
      }
    }, {
      key: 'createControlComponent',
      value: function createControlComponent(control) {
        var props = this.props;


        if (!control || !control.props || control instanceof _controlComponent2.default) {
          return control;
        }

        var controlType = getControlType(control, props, options);
        var _props$mapProps = props.mapProps,
            mapProps = _props$mapProps === undefined ? options.controlPropsMap[controlType] : _props$mapProps;


        var controlProps = (0, _pick2.default)(props, Object.keys(controlPropTypes));

        if (!mapProps) {
          return _react2.default.cloneElement(control, null, this.mapChildrenToControl(control.props.children));
        }

        return _react2.default.createElement(s.Control, _extends({}, controlProps, {
          control: control,
          controlProps: control.props,
          component: control.type,
          mapProps: mapProps
        }, defaultControlPropsMap[controlType] || {}));
      }
    }, {
      key: 'mapChildrenToControl',
      value: function mapChildrenToControl(children) {
        var _this2 = this;

        if (_react2.default.Children.count(children) > 1) {
          return _react2.default.Children.map(children, function (child) {
            return _this2.createControlComponent(child);
          });
        }

        return this.createControlComponent(children);
      }
    }, {
      key: 'render',
      value: function render() {
        var _props = this.props,
            component = _props.component,
            children = _props.children,
            fieldValue = _props.fieldValue;


        var allowedProps = (0, _omit2.default)(this.props, Object.keys(controlPropTypes));
        var renderableChildren = typeof children === 'function' ? children(fieldValue) : children;

        if (!component) {
          (0, _invariant2.default)(_react2.default.Children.count(renderableChildren) === 1, 'Empty wrapper components for <Field> are only possible' + 'when there is a single child. Please check the children' + ('passed into <Field model="' + this.props.model + '">.'));

          return this.createControlComponent(renderableChildren);
        }

        return _react2.default.createElement(component, allowedProps, this.mapChildrenToControl(renderableChildren));
      }
    }]);

    return Field;
  }(_react.Component);

  if (process.env.NODE_ENV !== 'production') {
    process.env.NODE_ENV !== "production" ? Field.propTypes = fieldPropTypes : void 0;
  }

  Field.defaultProps = {
    updateOn: 'change',
    asyncValidateOn: 'blur',
    parser: _identity2.default,
    formatter: _identity2.default,
    changeAction: _actions2.default.change,
    dynamic: true,
    component: 'div'
  };

  return (0, _resolveModel2.default)((0, _reactRedux.connect)(mapStateToProps)(Field));
}

exports.controlPropsMap = _controlPropsMap3.default;
exports.createFieldClass = createFieldClass;
exports.default = createFieldClass(_controlPropsMap3.default);