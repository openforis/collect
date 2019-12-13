'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports.createFormActionsReducer = createFormActionsReducer;

var _actionTypes = require('../action-types');

var _actionTypes2 = _interopRequireDefault(_actionTypes);

var _updateField = require('../utils/update-field');

var _updateField2 = _interopRequireDefault(_updateField);

var _updateParentForms = require('../utils/update-parent-forms');

var _updateParentForms2 = _interopRequireDefault(_updateParentForms);

var _updateSubFields = require('../utils/update-sub-fields');

var _updateSubFields2 = _interopRequireDefault(_updateSubFields);

var _getFieldForm = require('../utils/get-field-form');

var _getFieldForm2 = _interopRequireDefault(_getFieldForm);

var _isPristine = require('../form/is-pristine');

var _isPristine2 = _interopRequireDefault(_isPristine);

var _map = require('../utils/map');

var _map2 = _interopRequireDefault(_map);

var _isPlainObject = require('../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _mapValues = require('../utils/map-values');

var _mapValues2 = _interopRequireDefault(_mapValues);

var _inverse = require('../utils/inverse');

var _inverse2 = _interopRequireDefault(_inverse);

var _mergeValidity = require('../utils/merge-validity');

var _mergeValidity2 = _interopRequireDefault(_mergeValidity);

var _isValid = require('../form/is-valid');

var _isValid2 = _interopRequireDefault(_isValid);

var _isValidityValid = require('../utils/is-validity-valid');

var _isValidityValid2 = _interopRequireDefault(_isValidityValid);

var _isValidityInvalid = require('../utils/is-validity-invalid');

var _isValidityInvalid2 = _interopRequireDefault(_isValidityInvalid);

var _fieldActions = require('../actions/field-actions');

var _fieldActions2 = _interopRequireDefault(_fieldActions);

var _toPath = require('../utils/to-path');

var _toPath2 = _interopRequireDefault(_toPath);

var _initialFieldState = require('../constants/initial-field-state');

var _initialFieldState2 = _interopRequireDefault(_initialFieldState);

var _createField = require('../utils/create-field');

var _assocIn = require('../utils/assoc-in');

var _assocIn2 = _interopRequireDefault(_assocIn);

var _getFormValue = require('../utils/get-form-value');

var _getFormValue2 = _interopRequireDefault(_getFormValue);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

var resetFieldState = function resetFieldState(field, customInitialFieldState) {
  if (!(0, _isPlainObject2.default)(field)) return field;

  var intents = [{ type: 'reset' }];
  var resetValue = (0, _createField.getMeta)(field, 'initialValue');
  var loadedValue = (0, _createField.getMeta)(field, 'loadedValue');

  if (loadedValue && resetValue !== loadedValue) {
    intents.push({ type: 'load' });
    resetValue = loadedValue;
  }
  return (0, _createField.fieldOrForm)((0, _createField.getMeta)(field, 'model'), resetValue, _extends({}, customInitialFieldState, { intents: intents }));
};

var setInitialFieldState = function setInitialFieldState(customInitialFieldState) {
  return function (field) {
    if (!(0, _isPlainObject2.default)(field)) return field;

    if (field.$form) {
      // eslint-disable-next-line arrow-body-style
      return (0, _mapValues2.default)(field, function (fieldState, key) {
        return key === '$form' ? (0, _createField.updateFieldState)(customInitialFieldState, {
          value: field.value,
          model: field.model
        }) : resetFieldState(fieldState, customInitialFieldState);
      });
    }

    return (0, _createField.updateFieldState)(customInitialFieldState, {
      value: field.value,
      model: field.model
    });
  };
};

var addIntent = function addIntent(intents, newIntent) {
  if (!intents) return [newIntent];
  if (intents.some(function (intent) {
    return intent.type === newIntent.type;
  })) return intents;

  return intents.concat(newIntent);
};

var clearIntents = function clearIntents(intents, oldIntent) {
  if (!intents || typeof oldIntent === 'undefined') return [];
  return intents.filter(function (intent) {
    return intent.type !== oldIntent.type;
  });
};

var defaultOptions = {
  initialFieldState: _initialFieldState2.default
};

function createFormActionsReducer(options) {
  var formOptions = options ? _extends({}, defaultOptions, options, {
    initialFieldState: _extends({}, defaultOptions.initialFieldState, options.initialFieldState)
  }) : defaultOptions;

  var customInitialFieldState = formOptions.initialFieldState;

  return function formActionsReducer(state, action, localPath) {
    var _getFieldAndForm = (0, _updateField.getFieldAndForm)(state, localPath),
        _getFieldAndForm2 = _slicedToArray(_getFieldAndForm, 1),
        field = _getFieldAndForm2[0];

    var fieldState = field && field.$form ? field.$form : field;

    var intents = fieldState.intents;


    var fieldUpdates = {};
    var subFieldUpdates = {};
    var parentFormUpdates = void 0;

    switch (action.type) {
      case _actionTypes2.default.FOCUS:
        {
          fieldUpdates = {
            focus: true,
            intents: action.silent ? intents : addIntent(intents, action)
          };

          break;
        }

      case _actionTypes2.default.BLUR:
      case _actionTypes2.default.SET_TOUCHED:
        {
          var fieldForm = (0, _getFieldForm2.default)(state, localPath).$form;

          fieldUpdates = {
            focus: action.type === _actionTypes2.default.BLUR ? false : field.focus,
            touched: true,
            retouched: fieldForm ? !!(fieldForm.submitted || fieldForm.submitFailed) : false
          };

          parentFormUpdates = {
            touched: true,
            retouched: fieldUpdates.retouched
          };

          break;
        }

      case _actionTypes2.default.SET_UNTOUCHED:
        {
          fieldUpdates = {
            focus: false,
            touched: false
          };

          break;
        }

      case _actionTypes2.default.SET_PRISTINE:
      case _actionTypes2.default.SET_DIRTY:
        {
          var pristine = action.type === _actionTypes2.default.SET_PRISTINE;

          fieldUpdates = {
            pristine: pristine
          };

          subFieldUpdates = {
            pristine: pristine
          };

          parentFormUpdates = function parentFormUpdates(form) {
            return { pristine: (0, _isPristine2.default)(form) };
          };

          break;
        }

      case _actionTypes2.default.SET_VALIDATING:
        {
          fieldUpdates = {
            validating: action.validating,
            validated: !action.validating
          };

          break;
        }

      case _actionTypes2.default.SET_VALIDITY:
      case _actionTypes2.default.SET_ERRORS:
        {
          var _fieldUpdates;

          var isErrors = action.type === _actionTypes2.default.SET_ERRORS;
          var validity = void 0;
          if (isErrors) {
            validity = action.merge ? (0, _mergeValidity2.default)(fieldState.errors, action.errors) : action.errors;
          } else {
            validity = action.merge ? (0, _mergeValidity2.default)(fieldState.validity, action.validity) : action.validity;
          }

          var inverseValidity = (0, _isPlainObject2.default)(validity) ? (0, _mapValues2.default)(validity, _inverse2.default) : !validity;

          // If the field is a form, its validity is
          // also based on whether its fields are all valid.
          var areFieldsValid = field && field.$form ? (0, _isValid.fieldsValid)(field, { async: false }) : true;

          fieldUpdates = (_fieldUpdates = {}, _defineProperty(_fieldUpdates, isErrors ? 'errors' : 'validity', validity), _defineProperty(_fieldUpdates, isErrors ? 'validity' : 'errors', inverseValidity), _defineProperty(_fieldUpdates, 'validating', false), _defineProperty(_fieldUpdates, 'validated', true), _defineProperty(_fieldUpdates, 'valid', areFieldsValid && (isErrors ? !(0, _isValidityInvalid2.default)(validity) : (0, _isValidityValid2.default)(validity))), _fieldUpdates);

          if (action.async) {
            var actionValidity = isErrors ? action.errors : action.validity;

            fieldUpdates.asyncKeys = (0, _isPlainObject2.default)(actionValidity) || Array.isArray(actionValidity) ? Object.keys(actionValidity) : true;
          }

          parentFormUpdates = function parentFormUpdates(form) {
            return { valid: (0, _isValid2.default)(form) };
          };

          break;
        }

      case _actionTypes2.default.SET_FIELDS_VALIDITY:
        {
          return (0, _map2.default)(action.fieldsValidity, function (fieldValidity, subField) {
            return _fieldActions2.default.setValidity(subField, fieldValidity, action.options);
          }).reduce(function (accState, subAction) {
            return formActionsReducer(accState, subAction, localPath.concat((0, _toPath2.default)(subAction.model)));
          }, state);
        }

      case _actionTypes2.default.RESET_VALIDITY:
        {
          var _validity = _extends({}, fieldState.validity);
          var errors = void 0;
          var valid = void 0;

          if (action.omitKeys && typeof fieldState.errors !== 'string') {
            errors = _extends({}, fieldState.errors);
            action.omitKeys.forEach(function (key) {
              delete _validity[key];
              delete errors[key];
            });
            valid = (0, _isValidityValid2.default)(_validity);
          } else {
            _validity = customInitialFieldState.validity;
            errors = customInitialFieldState.errors;
            valid = customInitialFieldState.valid;
          }

          fieldUpdates = {
            valid: valid,
            validity: _validity,
            errors: errors
          };

          subFieldUpdates = {
            valid: customInitialFieldState.valid,
            validity: customInitialFieldState.validity,
            errors: customInitialFieldState.errors
          };

          break;
        }

      case _actionTypes2.default.SET_PENDING:
        {
          fieldUpdates = {
            pending: action.pending,
            submitted: false,
            submitFailed: false,
            retouched: false
          };

          subFieldUpdates = {
            pending: action.pending,
            submitted: false,
            submitFailed: false,
            retouched: false
          };

          parentFormUpdates = { pending: action.pending };

          break;
        }

      case _actionTypes2.default.SET_SUBMITTED:
        {
          var submitted = !!action.submitted;

          fieldUpdates = {
            pending: false,
            submitted: submitted,
            submitFailed: submitted ? false : fieldState && fieldState.submitFailed,
            touched: true,
            retouched: false
          };

          subFieldUpdates = {
            pending: false,
            submitted: submitted,
            submitFailed: submitted ? false : fieldUpdates.submitFailed,
            retouched: false
          };

          break;
        }

      case _actionTypes2.default.SET_SUBMIT_FAILED:
        {
          fieldUpdates = {
            pending: false,
            submitted: fieldState.submitted && !action.submitFailed,
            submitFailed: !!action.submitFailed,
            touched: true,
            retouched: false
          };

          subFieldUpdates = {
            pending: false,
            submitted: !action.submitFailed,
            submitFailed: !!action.submitFailed,
            touched: true,
            retouched: false
          };

          break;
        }

      case _actionTypes2.default.RESET:
        {
          return localPath.length ? (0, _assocIn2.default)(state, localPath, resetFieldState(field, customInitialFieldState)) : resetFieldState(field, customInitialFieldState);
        }

      case _actionTypes2.default.SET_INITIAL:
        {
          var setCustomInitialFieldState = setInitialFieldState(customInitialFieldState);

          return (0, _updateField2.default)(state, localPath, setCustomInitialFieldState, setCustomInitialFieldState);
        }

      case _actionTypes2.default.ADD_INTENT:
        {
          fieldUpdates = {
            intents: addIntent(intents, action.intent)
          };

          break;
        }

      case _actionTypes2.default.CLEAR_INTENTS:
        {
          fieldUpdates = {
            intents: clearIntents(intents, action.intent)
          };

          break;
        }

      case _actionTypes2.default.CHANGE:
        {
          return (0, _updateParentForms2.default)(state, localPath, function (parentForm) {
            var formModelValue = (0, _getFormValue2.default)(parentForm);

            if (!parentForm.$form) {
              return _extends({}, customInitialFieldState, {
                value: formModelValue,
                initialValue: formModelValue
              });
            }

            // If the form is invalid (due to async validity)
            // but its fields are valid and the value has changed,
            // the form should be "valid" again.
            if ((!parentForm.$form.validity || typeof parentForm.$form.validity === 'boolean' || !Object.keys(parentForm.$form.validity).length) && !parentForm.$form.valid && (0, _isValid2.default)(parentForm, { async: false })) {
              return {
                value: formModelValue,
                validity: true,
                errors: false,
                valid: true
              };
            }

            return {
              value: formModelValue
            };
          });
        }

      default:
        return state;
    }

    if (action.clearIntents) {
      fieldUpdates.intents = clearIntents(intents, action.clearIntents);
    }

    var updatedField = (0, _updateField2.default)(state, localPath, fieldUpdates);
    var updatedSubFields = Object.keys(subFieldUpdates).length ? (0, _updateSubFields2.default)(updatedField, localPath, subFieldUpdates) : updatedField;
    var updatedParentForms = parentFormUpdates ? (0, _updateParentForms2.default)(updatedSubFields, localPath, parentFormUpdates) : updatedSubFields;

    return updatedParentForms;
  };
}

exports.default = createFormActionsReducer();