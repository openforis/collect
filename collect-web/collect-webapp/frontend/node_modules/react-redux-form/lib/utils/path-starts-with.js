'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = pathStartsWith;
exports.pathDifference = pathDifference;

var _lodash = require('lodash.topath');

var _lodash2 = _interopRequireDefault(_lodash);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function pathStartsWith(pathString, subPathString) {
  if (pathString === subPathString) return true;

  var path = (0, _lodash2.default)(pathString);
  var subPath = (0, _lodash2.default)(subPathString);

  var startsWithSubPath = subPath.every(function (segment, index) {
    return path[index] === segment;
  });

  return startsWithSubPath;
}

function pathDifference(pathString, subPathString) {
  if (pathString === subPathString) return [];

  var path = (0, _lodash2.default)(pathString);
  var subPath = (0, _lodash2.default)(subPathString);

  var difference = path.reduce(function (acc, segment, index) {
    if (segment === subPath[index]) return acc;

    acc.push(segment);

    return acc;
  }, []);

  return difference;
}