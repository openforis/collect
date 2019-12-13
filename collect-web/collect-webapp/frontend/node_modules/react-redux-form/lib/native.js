'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.track = exports.getField = exports.form = exports.batched = exports.modeled = exports.Fieldset = exports.Errors = exports.Form = exports.Control = exports.actionTypes = exports.actions = exports.initialFieldState = exports.combineForms = exports.modelReducer = exports.formReducer = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _reactNative = require('react-native');

var _reactNativeSegmentedControlTab = require('react-native-segmented-control-tab');

var _reactNativeSegmentedControlTab2 = _interopRequireDefault(_reactNativeSegmentedControlTab);

var _modelReducer = require('./reducers/model-reducer');

var _modelReducer2 = _interopRequireDefault(_modelReducer);

var _formReducer = require('./reducers/form-reducer');

var _formReducer2 = _interopRequireDefault(_formReducer);

var _modeledEnhancer = require('./enhancers/modeled-enhancer');

var _modeledEnhancer2 = _interopRequireDefault(_modeledEnhancer);

var _actions = require('./actions');

var _actions2 = _interopRequireDefault(_actions);

var _formsReducer = require('./reducers/forms-reducer');

var _formsReducer2 = _interopRequireDefault(_formsReducer);

var _initialFieldState = require('./constants/initial-field-state');

var _initialFieldState2 = _interopRequireDefault(_initialFieldState);

var _actionTypes = require('./action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

var _formComponent = require('./components/form-component');

var _formComponent2 = _interopRequireDefault(_formComponent);

var _fieldsetComponent = require('./components/fieldset-component');

var _fieldsetComponent2 = _interopRequireDefault(_fieldsetComponent);

var _errorsComponent = require('./components/errors-component');

var _errorsComponent2 = _interopRequireDefault(_errorsComponent);

var _batchedEnhancer = require('./enhancers/batched-enhancer');

var _batchedEnhancer2 = _interopRequireDefault(_batchedEnhancer);

var _form = require('./form');

var _form2 = _interopRequireDefault(_form);

var _track = require('./utils/track');

var _track2 = _interopRequireDefault(_track);

var _omit = require('./utils/omit');

var _omit2 = _interopRequireDefault(_omit);

var _get2 = require('./utils/get');

var _get3 = _interopRequireDefault(_get2);

var _getFieldFromState = require('./utils/get-field-from-state');

var _getFieldFromState2 = _interopRequireDefault(_getFieldFromState);

var _controlComponentFactory = require('./components/control-component-factory');

var _controlComponentFactory2 = _interopRequireDefault(_controlComponentFactory);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; } /* eslint-disable react/prop-types */


function getTextValue(value) {
  if (typeof value === 'string' || typeof value === 'number') {
    return '' + value;
  }

  return '';
}

var DatePickerAndroid = function DatePickerAndroid() {
  for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
    args[_key] = arguments[_key];
  }

  return {
    open: function () {
      var _ref = _asyncToGenerator( /*#__PURE__*/regeneratorRuntime.mark(function _callee() {
        var _ref2, action, year, month, day, dismissed;

        return regeneratorRuntime.wrap(function _callee$(_context) {
          while (1) {
            switch (_context.prev = _context.next) {
              case 0:
                _context.next = 2;
                return _reactNative.DatePickerAndroid.open.apply(_reactNative.DatePickerAndroid, args);

              case 2:
                _ref2 = _context.sent;
                action = _ref2.action;
                year = _ref2.year;
                month = _ref2.month;
                day = _ref2.day;
                dismissed = action === _reactNative.DatePickerAndroid.dismissedAction;
                return _context.abrupt('return', { dismissed: dismissed, action: action, year: year, month: month, day: day });

              case 9:
              case 'end':
                return _context.stop();
            }
          }
        }, _callee, undefined);
      }));

      return function open() {
        return _ref.apply(this, arguments);
      };
    }()
  };
};

var noop = function noop() {
  return undefined;
};

var Control = (0, _controlComponentFactory2.default)({
  get: _get3.default,
  getFieldFromState: _getFieldFromState2.default,
  actions: _actions2.default
});

Control.MapView = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNative.MapView,
    updateOn: 'blur',
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref3) {
        var onFocus = _ref3.onFocus;
        return onFocus;
      },
      onRegionChange: function onRegionChange(_ref4) {
        var onChange = _ref4.onChange;
        return onChange;
      },
      onRegionChangeComplete: function onRegionChangeComplete(_ref5) {
        var onBlur = _ref5.onBlur;
        return onBlur;
      },
      region: function region(_ref6) {
        var modelValue = _ref6.modelValue;
        return modelValue;
      }
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

Control.Picker = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNative.Picker,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref7) {
        var onFocus = _ref7.onFocus;
        return onFocus;
      },
      onResponderRelease: function onResponderRelease(_ref8) {
        var onBlur = _ref8.onBlur;
        return onBlur;
      },
      selectedValue: function selectedValue(_ref9) {
        var modelValue = _ref9.modelValue;
        return modelValue;
      },
      onValueChange: function onValueChange(_ref10) {
        var onChange = _ref10.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

Control.Switch = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNative.Switch,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref11) {
        var onFocus = _ref11.onFocus;
        return onFocus;
      },
      onResponderRelease: function onResponderRelease(_ref12) {
        var onBlur = _ref12.onBlur;
        return onBlur;
      },
      value: function value(_ref13) {
        var modelValue = _ref13.modelValue;
        return !!modelValue;
      },
      onValueChange: function onValueChange(_ref14) {
        var onChange = _ref14.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

Control.TextInput = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNative.TextInput,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref15) {
        var onFocus = _ref15.onFocus;
        return onFocus;
      },
      value: function value(_props) {
        return !_props.defaultValue && !_props.hasOwnProperty('value') ? getTextValue(_props.viewValue) : _props.value;
      },
      onChangeText: function onChangeText(_ref16) {
        var onChange = _ref16.onChange;
        return onChange;
      },
      onChange: noop,
      onBlur: function onBlur(_ref17) {
        var _onBlur = _ref17.onBlur,
            viewValue = _ref17.viewValue;
        return function () {
          return _onBlur(viewValue);
        };
      },
      onFocus: function onFocus(_ref18) {
        var _onFocus = _ref18.onFocus;
        return _onFocus;
      }
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

Control.DatePickerIOS = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNative.DatePickerIOS,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref19) {
        var onFocus = _ref19.onFocus;
        return onFocus;
      },
      onResponderRelease: function onResponderRelease(_ref20) {
        var onBlur = _ref20.onBlur;
        return onBlur;
      },
      date: function date(_ref21) {
        var modelValue = _ref21.modelValue;
        return modelValue;
      },
      onDateChange: function onDateChange(_ref22) {
        var onChange = _ref22.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

Control.DatePickerAndroid = DatePickerAndroid;

Control.SegmentedControlIOS = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNative.SegmentedControlIOS,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref23) {
        var onFocus = _ref23.onFocus;
        return onFocus;
      },
      selectedIndex: function selectedIndex(_ref24) {
        var values = _ref24.values,
            modelValue = _ref24.modelValue;
        return values.indexOf(modelValue);
      },
      onValueChange: function onValueChange(_ref25) {
        var onChange = _ref25.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

Control.SegmentedControlAndroid = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNativeSegmentedControlTab2.default,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref26) {
        var onFocus = _ref26.onFocus;
        return onFocus;
      },
      selectedIndex: function selectedIndex(_ref27) {
        var values = _ref27.values,
            modelValue = _ref27.modelValue;
        return values.indexOf(modelValue);
      },
      onValueChange: function onValueChange(_ref28) {
        var onChange = _ref28.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

Control.Slider = function (props) {
  return _react2.default.createElement(Control, _extends({
    component: _reactNative.Slider,
    mapProps: _extends({
      value: function value(_ref29) {
        var modelValue = _ref29.modelValue;
        return modelValue;
      },
      onResponderGrant: function onResponderGrant(_ref30) {
        var onFocus = _ref30.onFocus;
        return onFocus;
      },
      onSlidingComplete: function onSlidingComplete(_ref31) {
        var onBlur = _ref31.onBlur;
        return onBlur;
      },
      onValueChange: function onValueChange(_ref32) {
        var onChange = _ref32.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

var NativeForm = function NativeForm(props) {
  return _react2.default.createElement(_formComponent2.default, _extends({ component: _reactNative.View }, (0, _omit2.default)(props, 'mapProps')));
};
var NativeFieldset = function NativeFieldset(props) {
  return _react2.default.createElement(_fieldsetComponent2.default, _extends({ component: _reactNative.View }, (0, _omit2.default)(props, 'mapProps')));
};
var NativeErrors = function NativeErrors(props) {
  return _react2.default.createElement(_errorsComponent2.default, _extends({
    wrapper: _reactNative.View,
    component: _reactNative.Text
  }, props));
};

exports.formReducer = _formReducer2.default;
exports.modelReducer = _modelReducer2.default;
exports.combineForms = _formsReducer2.default;
exports.initialFieldState = _initialFieldState2.default;
exports.actions = _actions2.default;
exports.actionTypes = _actionTypes2.default;
exports.Control = Control;
exports.Form = NativeForm;
exports.Errors = NativeErrors;
exports.Fieldset = NativeFieldset;
exports.modeled = _modeledEnhancer2.default;
exports.batched = _batchedEnhancer2.default;
exports.form = _form2.default;
exports.getField = _getFieldFromState2.default;
exports.track = _track2.default;