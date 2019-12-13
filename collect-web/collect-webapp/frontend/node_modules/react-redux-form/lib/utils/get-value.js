'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getValue;
exports.getCheckboxValue = getCheckboxValue;

var _isMulti = require('./is-multi');

var _isMulti2 = _interopRequireDefault(_isMulti);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function isEvent(event) {
  return !!(event && event.stopPropagation && event.preventDefault);
}

function getEventValue(event) {
  var target = event.target;


  if (!target) {
    if (!event.nativeEvent) {
      return undefined;
    }

    return event.nativeEvent.text;
  }

  if (target.type === 'file') {
    return [].concat(_toConsumableArray(target.files)) || target.dataTransfer && [].concat(_toConsumableArray(target.dataTransfer.files));
  }

  if (target.multiple) {
    return [].concat(_toConsumableArray(target.selectedOptions)).map(function (option) {
      return option.value;
    });
  }

  return target.value;
}

function getValue(value) {
  return isEvent(value) ? getEventValue(value) : value;
}

function getCheckboxValue(_, props) {
  var controlProps = props.controlProps;


  if ((0, _isMulti2.default)(props.model)) {
    return controlProps.value;
  }

  return !props.modelValue;
}