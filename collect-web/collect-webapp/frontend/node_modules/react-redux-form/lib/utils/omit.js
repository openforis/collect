'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = omit;
function omit(object, props) {
  if (object == null) {
    return {};
  }
  var newObject = _extends({}, object);

  if (typeof props === 'string') {
    delete newObject[props];
  } else {
    props.forEach(function (prop) {
      delete newObject[prop];
    });
  }

  return newObject;
}