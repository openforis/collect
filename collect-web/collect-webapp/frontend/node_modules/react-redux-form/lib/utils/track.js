'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.trackable = exports.createTrack = undefined;

var _findKey2 = require('../utils/find-key');

var _findKey3 = _interopRequireDefault(_findKey2);

var _get2 = require('../utils/get');

var _get3 = _interopRequireDefault(_get2);

var _iteratee = require('../utils/iteratee');

var _isMulti = require('../utils/is-multi');

var _isMulti2 = _interopRequireDefault(_isMulti);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toArray(arr) { return Array.isArray(arr) ? arr : Array.from(arr); }

var defaultStrategy = {
  findKey: _findKey3.default,
  get: _get3.default
};

function createTrack() {
  var s = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : defaultStrategy;

  var iteratee = (0, _iteratee.createIteratee)(s);

  return function track(model) {
    for (var _len = arguments.length, predicates = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      predicates[_key - 1] = arguments[_key];
    }

    var isPartial = model[0] === '.';

    return function (fullState, parentModel) {
      var childModel = isPartial ? model.slice(1) : model;
      var state = isPartial ? s.get(fullState, parentModel) : fullState;

      var _childModel$split = childModel.split(/\[\]\.?/),
          _childModel$split2 = _toArray(_childModel$split),
          parentModelPath = _childModel$split2[0],
          childModelPaths = _childModel$split2.slice(1);

      var fullPath = parentModelPath;
      var subState = s.get(state, fullPath);

      predicates.forEach(function (predicate, i) {
        var childModelPath = childModelPaths[i];
        var predicateIteratee = iteratee(predicate);

        var subPath = childModelPath ? s.findKey(subState, predicateIteratee) + '.' + childModelPath : '' + s.findKey(subState, predicateIteratee);

        subState = s.get(subState, subPath);
        fullPath += '.' + subPath;
      });

      if ((0, _isMulti2.default)(childModel) && predicates.length < childModelPaths.length) {
        fullPath += '[]';
      }

      return isPartial ? parentModel + '.' + fullPath : fullPath;
    };
  };
}

var track = createTrack();

function trackable(actionCreator) {
  return function (model) {
    for (var _len2 = arguments.length, args = Array(_len2 > 1 ? _len2 - 1 : 0), _key2 = 1; _key2 < _len2; _key2++) {
      args[_key2 - 1] = arguments[_key2];
    }

    if (typeof model === 'function') {
      return function (dispatch, getState) {
        var modelPath = model(getState());

        dispatch(actionCreator.apply(undefined, [modelPath].concat(args)));
      };
    }

    return actionCreator.apply(undefined, [model].concat(args));
  };
}

exports.default = track;
exports.createTrack = createTrack;
exports.trackable = trackable;