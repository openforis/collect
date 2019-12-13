"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isPending;
function isPending(formState) {
  if (!formState) return false;

  // Field is pending
  if (!formState.$form) {
    return formState.pending;
  }

  // Any field in form is pending
  return Object.keys(formState).some(function (key) {
    return isPending(formState[key]);
  });
}