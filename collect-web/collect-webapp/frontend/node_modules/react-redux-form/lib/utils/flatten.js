'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
function flatten(data) {
  var result = {};
  var delimiter = '.';

  function recurse(cur) {
    var prop = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';

    if (Object(cur) !== cur) {
      result[prop] = cur;
    } else if (Array.isArray(cur)) {
      if (!cur.length) result[prop] = [];

      cur.forEach(function (item, i) {
        recurse(cur[i], prop ? [prop, i].join(delimiter) : '' + i);
      });
    } else {
      var isEmpty = true;

      Object.keys(cur).forEach(function (key) {
        isEmpty = false;
        recurse(cur[key], prop ? [prop, key].join(delimiter) : key);
      });

      if (isEmpty) result[prop] = {};
    }
  }

  recurse(data);

  return result;
}

exports.default = flatten;