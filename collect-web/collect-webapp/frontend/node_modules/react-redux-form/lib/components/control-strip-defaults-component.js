'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _objectWithoutProperties(obj, keys) { var target = {}; for (var i in obj) { if (keys.indexOf(i) >= 0) continue; if (!Object.prototype.hasOwnProperty.call(obj, i)) continue; target[i] = obj[i]; } return target; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

// Prevents the defaultValue/defaultChecked fields from rendering with value/checked
var ComponentWrapper = function (_Component) {
  _inherits(ComponentWrapper, _Component);

  function ComponentWrapper() {
    _classCallCheck(this, ComponentWrapper);

    return _possibleConstructorReturn(this, (ComponentWrapper.__proto__ || Object.getPrototypeOf(ComponentWrapper)).apply(this, arguments));
  }

  _createClass(ComponentWrapper, [{
    key: 'render',
    value: function render() {
      /* eslint-disable no-unused-vars */
      var _props = this.props,
          defaultValue = _props.defaultValue,
          defaultChecked = _props.defaultChecked,
          component = _props.component,
          getRef = _props.getRef,
          otherProps = _objectWithoutProperties(_props, ['defaultValue', 'defaultChecked', 'component', 'getRef']);
      /* eslint-enable */

      if (getRef) {
        otherProps.ref = getRef;
      }
      var WrappedComponent = component;
      return _react2.default.createElement(WrappedComponent, otherProps);
    }
  }]);

  return ComponentWrapper;
}(_react.Component);

process.env.NODE_ENV !== "production" ? ComponentWrapper.propTypes = {
  component: _propTypes2.default.any,
  defaultValue: _propTypes2.default.any,
  defaultChecked: _propTypes2.default.any,
  getRef: _propTypes2.default.func
} : void 0;
exports.default = ComponentWrapper;