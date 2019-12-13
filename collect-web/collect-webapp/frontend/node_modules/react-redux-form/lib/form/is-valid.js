'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isValid;
exports.fieldsValid = fieldsValid;

var _isPlainObject = require('../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function isValid(formState) {
  var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : { async: true };

  if (!formState) return true;

  if (!formState.$form) {
    var errors = formState.errors;


    if (!Array.isArray(errors) && !(0, _isPlainObject2.default)(errors)) {
      // If asyncKeys = true and the error is not an
      // array or object (e.g. string), form errors are entirely async
      // and should be ignored when async = true.
      return !options.async && formState.asyncKeys || !errors;
    }

    return Object.keys(formState.errors).every(function (errorKey) {
      // if specified to ignore async validator keys and
      // current error key is an async validator key,
      // treat key as valid
      if (!options.async && Array.isArray(formState.asyncKeys) && !!~formState.asyncKeys.indexOf(errorKey)) {
        return true;
      }

      var valid = !formState.errors[errorKey];

      return valid;
    });
  }

  return Object.keys(formState).every(function (key) {
    return isValid(formState[key], options);
  });
}

function fieldsValid(formState) {
  var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : { async: true };

  return Object.keys(formState).every(function (key) {
    return key === '$form' || isValid(formState[key], options);
  });
}