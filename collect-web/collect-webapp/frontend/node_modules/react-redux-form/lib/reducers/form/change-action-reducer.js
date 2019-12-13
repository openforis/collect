'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.default = changeActionReducer;

var _actionTypes = require('../../action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

var _icepick = require('icepick');

var _icepick2 = _interopRequireDefault(_icepick);

var _get = require('../../utils/get');

var _get2 = _interopRequireDefault(_get);

var _shallowEqual = require('../../utils/shallow-equal');

var _shallowEqual2 = _interopRequireDefault(_shallowEqual);

var _isPlainObject = require('../../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _mapValues = require('../../utils/map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

var _formReducer = require('../form-reducer');

var _initialFieldState = require('../../constants/initial-field-state');

var _initialFieldState2 = _interopRequireDefault(_initialFieldState);

var _assocIn = require('../../utils/assoc-in');

var _assocIn2 = _interopRequireDefault(_assocIn);

var _getFormValue = require('../../utils/get-form-value');

var _getFormValue2 = _interopRequireDefault(_getFormValue);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function updateFieldValue(field, action) {
  var parentModel = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : undefined;
  var value = action.value,
      removeKeys = action.removeKeys,
      silent = action.silent,
      load = action.load,
      model = action.model,
      external = action.external;


  var fieldState = field && field.$form ? field.$form : field;

  var changedFieldProps = {
    validated: false,
    retouched: fieldState.submitted ? true : fieldState.retouched,
    // If change originated from Control component (not externally),
    // then there is no need to remind Control to validate itself.
    intents: external ? [{ type: 'validate' }] : [],
    pristine: silent ? fieldState.pristine : false,
    value: value,
    loadedValue: load ? value : fieldState.loadedValue
  };

  if ((0, _shallowEqual2.default)(field.value, value)) {
    return _icepick2.default.merge(field, changedFieldProps);
  }

  if (removeKeys) {
    (0, _invariant2.default)(field && field.$form, 'Unable to remove keys. ' + 'Field for "%s" in store is not an array/object.', model);

    var valueIsArray = Array.isArray(field.$form.value);
    var removeKeysArray = Array.isArray(removeKeys) ? removeKeys : [removeKeys];

    var result = void 0;

    if (valueIsArray) {
      result = [];

      Object.keys(field).forEach(function (key) {
        if (!!~removeKeysArray.indexOf(+key) || key === '$form') return;

        result[key] = _extends({}, field[key]);
      });

      var finalResult = _extends({}, result.filter(function (f) {
        return f;
      }).map(function (subField, index) {
        return _extends({}, subField, {
          model: model + '.' + index
        });
      }));

      finalResult.$form = field.$form;

      return finalResult;
    }

    result = _extends({}, field);

    Object.keys(field).forEach(function (key) {
      if (!!~removeKeysArray.indexOf(key)) {
        delete result['' + key];
      }
    });

    return result;
  }

  if (!Array.isArray(value) && !(0, _isPlainObject2.default)(value)) {
    return _icepick2.default.merge(field, _icepick2.default.set(changedFieldProps, 'value', value));
  }

  var updatedField = (0, _mapValues2.default)(value, function (subValue, index) {
    // TODO: refactor
    var subField = field[index] || (0, _formReducer.createInitialState)('' + (parentModel ? parentModel + '.' : '') + model + '.' + index, subValue);

    if (Object.hasOwnProperty.call(subField, '$form')) {
      return updateFieldValue(subField, {
        model: index,
        value: subValue,
        external: external,
        silent: silent,
        load: load
      }, parentModel ? parentModel + '.' + model : model);
    }

    if ((0, _shallowEqual2.default)(subValue, subField.value)) {
      return subField;
    }

    return _icepick2.default.merge(subField, _icepick2.default.assign(changedFieldProps, {
      value: subValue,
      loadedValue: load ? subValue : subField.loadedValue
    }));
  });

  var dirtyFormState = _icepick2.default.merge(field.$form || _initialFieldState2.default, _icepick2.default.set(changedFieldProps, 'retouched', field.submitted || field.$form && field.$form.retouched));

  return _icepick2.default.set(updatedField, '$form', dirtyFormState);
}

function changeActionReducer(state, action, localPath) {
  if (action.type !== _actionTypes2.default.CHANGE) return state;

  var field = (0, _get2.default)(state, localPath, (0, _formReducer.createInitialState)(action.model, action.value));

  var updatedField = updateFieldValue(field, action);

  if (!localPath.length) return updatedField;

  var updatedState = (0, _assocIn2.default)(state, localPath, updatedField, function (form) {
    if (!form.$form) return form;

    var formValue = (0, _getFormValue2.default)(form);

    var formUpdates = _extends({}, form.$form, {
      value: formValue
    });

    if (action.silent) {
      formUpdates.loadedValue = formValue;
    } else {
      formUpdates.pristine = false;
    }

    return _extends({}, form, {
      $form: formUpdates
    });
  });

  return updatedState;
}