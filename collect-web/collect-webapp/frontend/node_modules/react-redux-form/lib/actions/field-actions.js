'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.createFieldActions = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _mapValues = require('../utils/map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

var _actionTypes = require('../action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

var _batchActions = require('./batch-actions');

var _batchActions2 = _interopRequireDefault(_batchActions);

var _getValidity = require('../utils/get-validity');

var _getValidity2 = _interopRequireDefault(_getValidity);

var _isValidityValid = require('../utils/is-validity-valid');

var _isValidityValid2 = _interopRequireDefault(_isValidityValid);

var _isValidityInvalid = require('../utils/is-validity-invalid');

var _isValidityInvalid2 = _interopRequireDefault(_isValidityInvalid);

var _invertValidity = require('../utils/invert-validity');

var _invertValidity2 = _interopRequireDefault(_invertValidity);

var _track = require('../utils/track');

var _getForm = require('../utils/get-form');

var _getForm2 = _interopRequireDefault(_getForm);

var _getFieldFromState = require('../utils/get-field-from-state');

var _getFieldFromState2 = _interopRequireDefault(_getFieldFromState);

var _nullAction = require('../constants/null-action');

var _nullAction2 = _interopRequireDefault(_nullAction);

var _invariant = require('invariant');

var _invariant2 = _interopRequireDefault(_invariant);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

var defaultStrategies = {
  get: _get3.default,
  getForm: _getForm2.default,
  getFieldFromState: _getFieldFromState2.default
};

function createFieldActions() {
  var s = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategies;

  var addIntent = function addIntent(model, intent) {
    return {
      type: _actionTypes2.default.ADD_INTENT,
      model: model,
      intent: intent
    };
  };

  var clearIntents = function clearIntents(model, intents) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return {
      type: _actionTypes2.default.CLEAR_INTENTS,
      model: model,
      intents: intents,
      options: options
    };
  };

  var focus = function focus(model, value) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return _extends({
      type: _actionTypes2.default.FOCUS,
      model: model,
      value: value
    }, options);
  };

  var silentFocus = function silentFocus(model, value) {
    return focus(model, value, {
      silent: true
    });
  };

  var blur = function blur(model) {
    return {
      type: _actionTypes2.default.BLUR,
      model: model
    };
  };

  var setPristine = function setPristine(model) {
    return {
      type: _actionTypes2.default.SET_PRISTINE,
      model: model
    };
  };

  var setDirty = function setDirty(model) {
    return {
      type: _actionTypes2.default.SET_DIRTY,
      model: model
    };
  };

  var setInitial = function setInitial(model) {
    return {
      type: _actionTypes2.default.SET_INITIAL,
      model: model
    };
  };

  var setPending = function setPending(model) {
    var pending = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
    var options = arguments[2];
    return _extends({
      type: _actionTypes2.default.SET_PENDING,
      model: model,
      pending: pending
    }, options);
  };

  var setValidating = function setValidating(model) {
    var validating = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
    return {
      type: _actionTypes2.default.SET_VALIDATING,
      model: model,
      validating: validating
    };
  };

  var setValidity = function setValidity(model, validity) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return _extends({
      type: options.errors ? _actionTypes2.default.SET_ERRORS : _actionTypes2.default.SET_VALIDITY,
      model: model
    }, options, _defineProperty({}, options.errors ? 'errors' : 'validity', validity));
  };

  var resetValidity = function resetValidity(model) {
    var omitKeys = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
    return {
      type: _actionTypes2.default.RESET_VALIDITY,
      model: model,
      omitKeys: omitKeys
    };
  };

  var setFieldsValidity = function setFieldsValidity(model, fieldsValidity) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return {
      type: _actionTypes2.default.SET_FIELDS_VALIDITY,
      model: model,
      fieldsValidity: fieldsValidity,
      options: options
    };
  };

  var setErrors = function setErrors(model, errors) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return setValidity(model, errors, _extends({}, options, {
      errors: true
    }));
  };

  var setFieldsErrors = function setFieldsErrors(model, fieldsErrors, options) {
    return setFieldsValidity(model, fieldsErrors, _extends({}, options, {
      errors: true
    }));
  };

  var resetErrors = resetValidity;

  var setTouched = function setTouched(model) {
    return {
      type: _actionTypes2.default.SET_TOUCHED,
      model: model
    };
  };

  var setUntouched = function setUntouched(model) {
    return {
      type: _actionTypes2.default.SET_UNTOUCHED,
      model: model
    };
  };

  var asyncSetValidity = function asyncSetValidity(model, validator) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return function (dispatch, getState) {
      var value = s.get(getState(), model);

      dispatch(setValidating(model, true));

      var done = function done(validity) {
        dispatch(setValidity(model, validity, _extends({
          async: true
        }, options)));
      };

      var immediateResult = validator(value, done);

      if (typeof immediateResult !== 'undefined') {
        done(immediateResult);
      }
    };
  };

  var asyncSetErrors = function asyncSetErrors(model, validator) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return asyncSetValidity(model, validator, _extends({
      errors: true
    }, options));
  };

  var setSubmitted = function setSubmitted(model) {
    var submitted = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
    return {
      type: _actionTypes2.default.SET_SUBMITTED,
      model: model,
      submitted: submitted
    };
  };

  var setSubmitFailed = function setSubmitFailed(model) {
    var submitFailed = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;
    var options = arguments[2];
    return _extends({
      type: _actionTypes2.default.SET_SUBMIT_FAILED,
      model: model,
      submitFailed: submitFailed
    }, options);
  };

  var submit = function submit(model, promise) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

    if (typeof promise === 'undefined') {
      return addIntent(model, { type: 'submit' });
    }

    return function (dispatch, getState) {
      if (options.validate) {
        var form = s.getForm(getState(), model);

        (0, _invariant2.default)(form, 'Unable to submit form with validation. ' + 'Could not find form for "%s" in the store.', model);

        if (!form.$form.valid) {
          return dispatch(_nullAction2.default);
        }

        dispatch(setPending(model, true));
      } else if (options.validators || options.errors) {
        var validators = options.validators || options.errors;
        var isErrors = options.errors;
        var value = s.get(getState(), model);
        var validity = (0, _getValidity2.default)(validators, value);
        var valid = options.errors ? !(0, _isValidityInvalid2.default)(validity) : (0, _isValidityValid2.default)(validity);

        if (!valid) {
          return dispatch(isErrors ? setErrors(model, validity) : setValidity(model, validity));
        }

        dispatch((0, _batchActions2.default)(model, [setValidity(model, isErrors ? (0, _invertValidity2.default)(validity) : validity), setPending(model, true)]));
      } else {
        dispatch(setPending(model, true));
      }

      var errorsAction = options.fields ? setFieldsErrors : setErrors;

      promise.then(function (response) {
        dispatch((0, _batchActions2.default)(model, [setSubmitted(model, true), setValidity(model, response)]));
      }).catch(function (rejection) {
        var error = rejection instanceof Error ? rejection.message : rejection;

        dispatch((0, _batchActions2.default)(model, [setSubmitFailed(model), errorsAction(model, error, { async: true })]));
      });

      return promise;
    };
  };

  var submitFields = function submitFields(model, promise) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return submit(model, promise, _extends({}, options, {
      fields: true
    }));
  };

  var validSubmit = function validSubmit(model, promise) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return submit(model, promise, _extends({}, options, {
      validate: true
    }));
  };

  var validate = function validate(model, validators) {
    return function (dispatch, getState) {
      var value = s.get(getState(), model);
      var validity = (0, _getValidity2.default)(validators, value);

      dispatch(setValidity(model, validity));
    };
  };

  var validateErrors = function validateErrors(model, errorValidators) {
    return function (dispatch, getState) {
      var value = s.get(getState(), model);
      var errors = (0, _getValidity2.default)(errorValidators, value);

      dispatch(setValidity(model, errors, { errors: true }));
    };
  };

  var validateFields = function validateFields(model, fieldValidators) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return function (dispatch, getState) {
      var modelValue = s.get(getState(), model);

      var fieldsValidity = (0, _mapValues2.default)(fieldValidators, function (validator, field) {
        var fieldValue = field ? s.get(modelValue, field) : modelValue;

        var fieldValidity = (0, _getValidity2.default)(validator, fieldValue);

        return fieldValidity;
      });

      var fieldsValiditySetter = options.errors ? setFieldsErrors : setFieldsValidity;

      dispatch(fieldsValiditySetter(model, fieldsValidity));
    };
  };

  var validateFieldsErrors = function validateFieldsErrors(model, fieldErrorsValidators) {
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
    return validateFields(model, fieldErrorsValidators, _extends({}, options, {
      errors: true
    }));
  };

  return (0, _mapValues2.default)({
    blur: blur,
    focus: focus,
    silentFocus: silentFocus,
    submit: submit,
    submitFields: submitFields,
    validSubmit: validSubmit,
    setDirty: setDirty,
    setErrors: setErrors,
    setInitial: setInitial,
    setPending: setPending,
    setValidating: setValidating,
    setPristine: setPristine,
    setSubmitted: setSubmitted,
    setSubmitFailed: setSubmitFailed,
    setTouched: setTouched,
    setUntouched: setUntouched,
    setValidity: setValidity,
    setFieldsValidity: setFieldsValidity,
    setFieldsErrors: setFieldsErrors,
    resetValidity: resetValidity,
    resetErrors: resetErrors,
    validate: validate,
    validateErrors: validateErrors,
    validateFields: validateFields,
    validateFieldsErrors: validateFieldsErrors,
    asyncSetValidity: asyncSetValidity,
    asyncSetErrors: asyncSetErrors,
    addIntent: addIntent,
    clearIntents: clearIntents
  }, _track.trackable);
}

exports.createFieldActions = createFieldActions;
exports.default = createFieldActions();