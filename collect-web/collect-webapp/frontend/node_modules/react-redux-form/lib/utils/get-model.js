'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = getModel;
function getModel(model, state) {
  return typeof model === 'function' && state ? model(state) : model;
}