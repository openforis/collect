"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isRetouched;
function isRetouched(formState) {
  if (!formState) return false;

  // Field is pending
  if (!formState.$form) {
    return formState.retouched;
  }

  // Any field in form is pending
  return Object.keys(formState).some(function (key) {
    return isRetouched(formState[key]);
  });
}