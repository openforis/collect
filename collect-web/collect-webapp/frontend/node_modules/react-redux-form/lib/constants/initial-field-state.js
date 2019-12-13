"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var initialFieldState = {
  focus: false,
  pending: false,
  pristine: true,
  submitted: false,
  submitFailed: false,
  retouched: false,
  touched: false,
  valid: true,
  validating: false,
  validated: false,
  validity: {},
  errors: {},
  intents: []
};

exports.default = initialFieldState;