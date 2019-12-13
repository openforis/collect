"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isTouched;
function isTouched(formState) {
  if (!formState) return false;

  // Field is touched
  if (!formState.$form) {
    return formState.touched;
  }

  // Any field in form is touched
  return Object.keys(formState).some(function (key) {
    return isTouched(formState[key]);
  });
}