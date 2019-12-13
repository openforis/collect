'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.clearGetFormCache = undefined;
exports.getFormStateKey = getFormStateKey;

var _get = require('../utils/get');

var _get2 = _interopRequireDefault(_get);

var _isPlainObject = require('../utils/is-plain-object');

var _isPlainObject2 = _interopRequireDefault(_isPlainObject);

var _pathStartsWith = require('../utils/path-starts-with');

var _pathStartsWith2 = _interopRequireDefault(_pathStartsWith);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var defaultStrategy = {
  get: _get2.default,
  keys: function keys(state) {
    return Object.keys(state);
  },
  isObject: function isObject(state) {
    return (0, _isPlainObject2.default)(state);
  }
};

function joinPaths() {
  for (var _len = arguments.length, paths = Array(_len), _key = 0; _key < _len; _key++) {
    paths[_key] = arguments[_key];
  }

  return paths.filter(function (path) {
    return !!path && path.length;
  }).join('.');
}

function getFormStateKey(state, model) {
  var s = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : defaultStrategy;
  var currentPath = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : '';

  var deepCandidateKeys = [];
  var result = null;

  s.keys(state).some(function (key) {
    if (key === '') {
      console.warn('react-redux-form skipped over an empty property key: %s', currentPath);
      return false;
    }
    var subState = s.get(state, key);

    if (subState && s.get(subState, '$form')) {
      var subStateModel = s.get(subState, '$form.model');

      if ((0, _pathStartsWith2.default)(model, subStateModel) || subStateModel === '') {
        var localPath = (0, _pathStartsWith.pathDifference)(model, subStateModel);

        var resultPath = [currentPath, key];
        var currentState = subState;

        localPath.every(function (segment) {
          if (s.get(currentState, segment) && s.get(currentState, segment + '.$form')) {
            currentState = s.get(currentState, segment);
            resultPath.push(segment);

            return true;
          }

          return false;
        });

        result = joinPaths.apply(undefined, resultPath);

        return true;
      }

      return false;
    }

    if (s.isObject(subState)) {
      deepCandidateKeys.push(key);
    }

    return false;
  });

  if (result) return result;

  deepCandidateKeys.some(function (key) {
    result = getFormStateKey(s.get(state, key), model, s, joinPaths(currentPath, key));

    return !!result;
  });

  if (result) return result;

  return null;
}

var formStateKeyCache = {};

var clearGetFormCache = exports.clearGetFormCache = function clearGetFormCache() {
  return formStateKeyCache = {};
}; // eslint-disable-line no-return-assign

var getFormStateKeyCached = function () {
  return function (state, modelString) {
    var s = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : defaultStrategy;

    if (formStateKeyCache[modelString]) return formStateKeyCache[modelString];

    var result = getFormStateKey(state, modelString, s);

    formStateKeyCache[modelString] = result; // eslint-disable-line no-return-assign

    return result;
  };
}();

function getForm(state, modelString) {
  var s = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : defaultStrategy;

  var formStateKey = getFormStateKeyCached(state, modelString, s);

  if (!formStateKey) {
    return null;
  }

  var form = s.get(state, formStateKey);

  return form;
}

exports.default = getForm;