'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.track = exports.getFormStateKey = exports.getForm = exports.getField = exports.form = exports.batched = exports.modeled = exports.createFieldClass = exports.Fieldset = exports.Errors = exports.Form = exports.Control = exports.Field = exports.controls = exports.actionTypes = exports.actions = exports.initialFieldState = exports.createForms = exports.combineForms = exports.modelReducer = exports.formReducer = undefined;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _modelReducer = require('./reducers/model-reducer');

var _formReducer = require('./reducers/form-reducer');

var _formReducer2 = _interopRequireDefault(_formReducer);

var _modeledEnhancer = require('./enhancers/modeled-enhancer');

var _modelActions = require('./actions/model-actions');

var _controlPropsMap = require('./constants/control-props-map');

var _controlPropsMap2 = _interopRequireDefault(_controlPropsMap);

var _formsReducer = require('./reducers/forms-reducer');

var _errorsComponent = require('./components/errors-component');

var _controlComponent = require('./components/control-component');

var _formComponent = require('./components/form-component');

var _fieldActions = require('./actions/field-actions');

var _track = require('./utils/track');

var _fieldsetComponent = require('./components/fieldset-component');

var _fieldsetComponent2 = _interopRequireDefault(_fieldsetComponent);

var _batchActions = require('./actions/batch-actions');

var _batchActions2 = _interopRequireDefault(_batchActions);

var _getValue = require('./utils/get-value');

var _getValue2 = _interopRequireDefault(_getValue);

var _getFromImmutableState = require('./utils/get-from-immutable-state');

var _getFromImmutableState2 = _interopRequireDefault(_getFromImmutableState);

var _getForm = require('./utils/get-form');

var _getForm2 = _interopRequireDefault(_getForm);

var _isPlainObject = require('./utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _findKeyImmutable = require('./utils/find-key-immutable');

var _findKeyImmutable2 = _interopRequireDefault(_findKeyImmutable);

var _immutable = require('immutable');

var _immutable2 = _interopRequireDefault(_immutable);

var _reactDom = require('react-dom');

var _index = require('./index');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function immutableSet(state, path, value) {
  try {
    return state.setIn(path, value);
  } catch (error) {
    throw new Error('Unable to set path \'' + path.join('.') + '\' in state. Please make sure that state is an Immutable instance.');
  }
}

function immutableKeys(state) {
  if (_immutable2.default.Map.isMap(state)) {
    return state.keySeq();
  }
  return Object.keys(state);
}

var baseStrategy = {
  get: _getFromImmutableState2.default,
  set: immutableSet,
  getValue: _getValue2.default,
  keys: immutableKeys,
  splice: function splice(list) {
    for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      args[_key - 1] = arguments[_key];
    }

    return list.splice.apply(list, args);
  },
  merge: function merge(map) {
    for (var _len2 = arguments.length, args = Array(_len2 > 1 ? _len2 - 1 : 0), _key2 = 1; _key2 < _len2; _key2++) {
      args[_key2 - 1] = arguments[_key2];
    }

    return map.merge.apply(map, args);
  },
  remove: function remove(map) {
    for (var _len3 = arguments.length, args = Array(_len3 > 1 ? _len3 - 1 : 0), _key3 = 1; _key3 < _len3; _key3++) {
      args[_key3 - 1] = arguments[_key3];
    }

    return map.remove.apply(map, args);
  },
  push: function push(list) {
    for (var _len4 = arguments.length, args = Array(_len4 > 1 ? _len4 - 1 : 0), _key4 = 1; _key4 < _len4; _key4++) {
      args[_key4 - 1] = arguments[_key4];
    }

    return list.push.apply(list, args);
  },
  length: function length(list) {
    return list.size;
  },
  object: new _immutable2.default.Map(),
  array: new _immutable2.default.List(),
  isObject: function isObject(state) {
    return (0, _isPlainObject2.default)(state) || _immutable2.default.Map.isMap(state);
  }
};

function immutableGetForm(state, modelString) {
  return (0, _getForm2.default)(state, modelString, baseStrategy);
}

function immutableGetFormStateKey(state, model) {
  return (0, _getForm.getFormStateKey)(state, model, baseStrategy);
}

function immutableGetFieldFromState(state, modelString) {
  return (0, _index.getField)(state, modelString, { getForm: immutableGetForm });
}

var immutableStrategy = _extends({}, baseStrategy, {
  getForm: immutableGetForm,
  getFieldFromState: immutableGetFieldFromState,
  findKey: _findKeyImmutable2.default
});

function transformAction(action) {
  if (action.value && action.value.toJS) {
    return _extends({}, action, {
      value: action.value.toJS()
    });
  }

  if (action.actions) {
    return _extends({}, action, {
      actions: action.actions.map(transformAction)
    });
  }

  return action;
}

function immutableFormReducer(model) {
  var initialState = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : new _immutable2.default.Map();
  var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

  var _initialState = initialState && initialState.toJS ? initialState.toJS() : initialState;

  return (0, _formReducer2.default)(model, _initialState, _extends({}, options, {
    transformAction: transformAction
  }));
}

var immutableModelActions = (0, _modelActions.createModelActions)(immutableStrategy);
var immutableFieldActions = (0, _fieldActions.createFieldActions)(immutableStrategy);

var immutableActions = _extends({}, immutableModelActions, immutableFieldActions, {
  batch: _batchActions2.default
});

var immutableModelReducer = (0, _modelReducer.createModeler)(immutableStrategy);
var immutableModelReducerEnhancer = (0, _modeledEnhancer.createModelReducerEnhancer)(immutableModelReducer);
var ImmutableControl = (0, _controlComponent.createControlClass)({
  get: _getFromImmutableState2.default,
  getFieldFromState: immutableGetFieldFromState,
  actions: immutableModelActions,
  findDOMNode: _reactDom.findDOMNode
});
var ImmutableField = (0, _index.createFieldClass)(_controlPropsMap2.default, {
  Control: ImmutableControl,
  getter: _getFromImmutableState2.default,
  getFieldFromState: immutableGetFieldFromState,
  changeAction: immutableModelActions.change,
  actions: immutableModelActions
});
var ImmutableErrors = (0, _errorsComponent.createErrorsClass)(immutableStrategy);
var ImmutableForm = (0, _formComponent.createFormClass)(_extends({}, immutableStrategy, {
  actions: immutableActions
}));

var immutableFormCombiner = (0, _formsReducer.createFormCombiner)({
  modelReducer: immutableModelReducer,
  formReducer: immutableFormReducer,
  modeled: immutableModelReducerEnhancer,
  toJS: function toJS(val) {
    return val && val.toJS ? val.toJS() : val;
  }
});

var immutableCombineForms = immutableFormCombiner.combineForms;
var immutableCreateForms = immutableFormCombiner.createForms;

var immutableTrack = (0, _track.createTrack)(immutableStrategy);

exports.formReducer = immutableFormReducer;
exports.modelReducer = immutableModelReducer;
exports.combineForms = immutableCombineForms;
exports.createForms = immutableCreateForms;
exports.initialFieldState = _index.initialFieldState;
exports.actions = immutableActions;
exports.actionTypes = _index.actionTypes;
exports.controls = _controlPropsMap2.default;
exports.Field = ImmutableField;
exports.Control = ImmutableControl;
exports.Form = ImmutableForm;
exports.Errors = ImmutableErrors;
exports.Fieldset = _fieldsetComponent2.default;
exports.createFieldClass = _index.createFieldClass;
exports.modeled = immutableModelReducerEnhancer;
exports.batched = _index.batched;
exports.form = _index.form;
exports.getField = immutableGetFieldFromState;
exports.getForm = immutableGetForm;
exports.getFormStateKey = immutableGetFormStateKey;
exports.track = immutableTrack;