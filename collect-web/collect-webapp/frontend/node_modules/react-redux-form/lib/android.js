'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.track = exports.getField = exports.form = exports.batched = exports.modeled = exports.Fieldset = exports.Errors = exports.Form = exports.Control = exports.actionTypes = exports.actions = exports.initialFieldState = exports.combineForms = exports.modelReducer = exports.formReducer = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; }; /* eslint-disable react/prop-types */


var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _reactNative = require('react-native');

var _index = require('./index');

var _omit = require('./utils/omit');

var _omit2 = _interopRequireDefault(_omit);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getTextValue(value) {
  if (typeof value === 'string' || typeof value === 'number') {
    return '' + value;
  }

  return '';
}

var noop = function noop() {
  return undefined;
};

_index.Control.Picker = function (props) {
  return _react2.default.createElement(_index.Control, _extends({
    component: _reactNative.Picker,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref) {
        var onFocus = _ref.onFocus;
        return onFocus;
      },
      onResponderRelease: function onResponderRelease(_ref2) {
        var onBlur = _ref2.onBlur;
        return onBlur;
      },
      selectedValue: function selectedValue(_ref3) {
        var modelValue = _ref3.modelValue;
        return modelValue;
      },
      onValueChange: function onValueChange(_ref4) {
        var onChange = _ref4.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

_index.Control.Switch = function (props) {
  return _react2.default.createElement(_index.Control, _extends({
    component: _reactNative.Switch,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref5) {
        var onFocus = _ref5.onFocus;
        return onFocus;
      },
      onResponderRelease: function onResponderRelease(_ref6) {
        var onBlur = _ref6.onBlur;
        return onBlur;
      },
      value: function value(_ref7) {
        var modelValue = _ref7.modelValue;
        return !!modelValue;
      },
      onValueChange: function onValueChange(_ref8) {
        var onChange = _ref8.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

_index.Control.TextInput = function (props) {
  return _react2.default.createElement(_index.Control, _extends({
    component: _reactNative.TextInput,
    mapProps: _extends({
      onResponderGrant: function onResponderGrant(_ref9) {
        var onFocus = _ref9.onFocus;
        return onFocus;
      },
      value: function value(_props) {
        return !_props.defaultValue && !_props.hasOwnProperty('value') ? getTextValue(_props.viewValue) : _props.value;
      },
      onChangeText: function onChangeText(_ref10) {
        var onChange = _ref10.onChange;
        return onChange;
      },
      onChange: noop,
      onBlur: function onBlur(_ref11) {
        var _onBlur = _ref11.onBlur,
            viewValue = _ref11.viewValue;
        return function () {
          return _onBlur(viewValue);
        };
      },
      onFocus: function onFocus(_ref12) {
        var _onFocus = _ref12.onFocus;
        return _onFocus;
      }
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

_index.Control.Slider = function (props) {
  return _react2.default.createElement(_index.Control, _extends({
    component: _reactNative.Slider,
    mapProps: _extends({
      value: function value(_ref13) {
        var modelValue = _ref13.modelValue;
        return modelValue;
      },
      onResponderGrant: function onResponderGrant(_ref14) {
        var onFocus = _ref14.onFocus;
        return onFocus;
      },
      onSlidingComplete: function onSlidingComplete(_ref15) {
        var onBlur = _ref15.onBlur;
        return onBlur;
      },
      onValueChange: function onValueChange(_ref16) {
        var onChange = _ref16.onChange;
        return onChange;
      },
      onChange: noop
    }, props.mapProps)
  }, (0, _omit2.default)(props, 'mapProps')));
};

var NativeForm = function NativeForm(props) {
  return _react2.default.createElement(_index.Form, _extends({ component: _reactNative.View }, (0, _omit2.default)(props, 'mapProps')));
};
var NativeFieldset = function NativeFieldset(props) {
  return _react2.default.createElement(_index.Fieldset, _extends({ component: _reactNative.View }, (0, _omit2.default)(props, 'mapProps')));
};
var NativeErrors = function NativeErrors(props) {
  return _react2.default.createElement(_index.Errors, _extends({
    wrapper: _reactNative.View,
    component: _reactNative.Text
  }, props));
};

exports.formReducer = _index.formReducer;
exports.modelReducer = _index.modelReducer;
exports.combineForms = _index.combineForms;
exports.initialFieldState = _index.initialFieldState;
exports.actions = _index.actions;
exports.actionTypes = _index.actionTypes;
exports.Control = _index.Control;
exports.Form = NativeForm;
exports.Errors = NativeErrors;
exports.Fieldset = NativeFieldset;
exports.modeled = _index.modeled;
exports.batched = _index.batched;
exports.form = _index.form;
exports.getField = _index.getField;
exports.track = _index.track;