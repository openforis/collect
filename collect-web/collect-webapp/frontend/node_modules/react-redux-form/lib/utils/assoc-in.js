'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.assoc = assoc;
exports.default = assocIn;

var _identity = require('./identity');

var _identity2 = _interopRequireDefault(_identity);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function objClone(obj) {
  var keys = Object.keys(obj);
  var length = keys.length;
  var result = {};
  var index = 0;
  var key = void 0;

  for (; index < length; index += 1) {
    key = keys[index];
    result[key] = obj[key];
  }
  return result;
}

function assoc(state, key, value) {
  var newState = objClone(state);

  newState[key] = value;

  return newState;
}

function assocIn(state, path, value) {
  var fn = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : _identity2.default;

  if (!path.length) return value;

  var key0 = path[0];

  if (path.length === 1) {
    return fn(assoc(state, key0, value));
  }

  return fn(assoc(state, key0, assocIn(state[key0] || {}, path.slice(1), value, fn)));
}