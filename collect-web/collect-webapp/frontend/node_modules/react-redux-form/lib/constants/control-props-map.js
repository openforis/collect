'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _isMulti = require('../utils/is-multi');

var _isMulti2 = _interopRequireDefault(_isMulti);

var _iteratee = require('../utils/iteratee');

var _actions = require('../actions');

var _actions2 = _interopRequireDefault(_actions);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getTextValue(value) {
  if (typeof value === 'string') {
    return '' + value;
  }

  return value;
}

function isChecked(props) {
  if ((0, _isMulti2.default)(props.model)) {
    if (!props.modelValue) return false;

    return props.modelValue.some(function (item) {
      return item === props.value;
    });
  }
  if (typeof props.modelValue === 'undefined') {
    if (typeof props.defaultChecked !== 'undefined') {
      return props.defaultChecked;
    }
  }

  return !!props.modelValue;
}

var standardPropsMap = {
  name: function name(props) {
    return props.name || props.model;
  },
  disabled: function disabled(_ref) {
    var fieldValue = _ref.fieldValue,
        _disabled = _ref.disabled;
    return (0, _iteratee.iterateeValue)(fieldValue, _disabled);
  },
  onChange: function onChange(_ref2) {
    var _onChange = _ref2.onChange;
    return _onChange;
  },
  onBlur: function onBlur(_ref3) {
    var _onBlur = _ref3.onBlur;
    return _onBlur;
  },
  onFocus: function onFocus(_ref4) {
    var _onFocus = _ref4.onFocus;
    return _onFocus;
  },
  onKeyPress: function onKeyPress(_ref5) {
    var _onKeyPress = _ref5.onKeyPress;
    return _onKeyPress;
  }
};

var textPropsMap = _extends({}, standardPropsMap, {
  // the value passed into the control is either the original control's
  // value prop (if the control is controlled) or the value controlled by
  // <Control>.
  value: function value(props) {
    if (props.hasOwnProperty('value')) {
      return props.value;
    }

    var value = getTextValue(props.viewValue);

    return value === undefined ? '' : value;
  }
});

var getModelValue = function getModelValue(_ref6) {
  var modelValue = _ref6.modelValue;
  return modelValue;
};
var getViewValue = function getViewValue(props) {
  return props.hasOwnProperty('value') ? props.value : props.viewValue;
};

var controlPropsMap = {
  default: _extends({}, standardPropsMap, {
    value: getViewValue
  }),
  checkbox: _extends({}, standardPropsMap, {
    checked: isChecked
  }),
  radio: _extends({}, standardPropsMap, {
    checked: function checked(props) {
      return props.defaultChecked ? props.checked : props.modelValue === props.value;
    },
    value: function value(props) {
      return props.value;
    }
  }),
  select: _extends({}, standardPropsMap, {
    value: getViewValue
  }),
  text: textPropsMap,
  textarea: textPropsMap,
  file: standardPropsMap,
  button: _extends({}, standardPropsMap, {
    value: getModelValue
  }),
  reset: _extends({}, standardPropsMap, {
    onClick: function onClick(props) {
      return function (event) {
        event.preventDefault();
        props.dispatch(_actions2.default.reset(props.model));
      };
    }
  })
};

exports.default = controlPropsMap;