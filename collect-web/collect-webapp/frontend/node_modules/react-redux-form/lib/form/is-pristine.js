'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isPristine;
function isPristine(formState) {
  if (!formState) return false;

  // Form is pristine
  if (!formState.$form) {
    return formState.pristine;
  }

  // Every field in form is pristine
  return Object.keys(formState).every(function (key) {
    if (key === '$form') return true;

    return isPristine(formState[key]);
  });
}