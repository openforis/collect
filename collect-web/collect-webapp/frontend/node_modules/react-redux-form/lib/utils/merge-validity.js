'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = mergeValidity;

var _isPlainObject = require('./is-plain-object');

var _icepick = require('icepick');

function mergeValidity(fieldValidity, actionValidity) {
  if (!(0, _isPlainObject.isObjectLike)(fieldValidity) || !(0, _isPlainObject.isObjectLike)(actionValidity)) {
    // can't merge string/boolean validity with keyed validity
    return actionValidity;
  }

  return (0, _icepick.merge)(fieldValidity, actionValidity);
}