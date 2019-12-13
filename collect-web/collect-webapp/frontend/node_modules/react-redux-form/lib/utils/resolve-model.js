'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

exports.default = wrapWithModelResolver;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function resolveModel(model, parentModel) {
  if (parentModel) {
    if (model[0] === '.' || model[0] === '[') {
      return '' + parentModel + model;
    }

    if (typeof model === 'function') {
      return function (state) {
        return model(state, parentModel);
      };
    }
  }

  return model;
}

function wrapWithModelResolver(WrappedComponent) {
  var ResolvedModelWrapper = function (_PureComponent) {
    _inherits(ResolvedModelWrapper, _PureComponent);

    function ResolvedModelWrapper() {
      _classCallCheck(this, ResolvedModelWrapper);

      return _possibleConstructorReturn(this, (ResolvedModelWrapper.__proto__ || Object.getPrototypeOf(ResolvedModelWrapper)).apply(this, arguments));
    }

    _createClass(ResolvedModelWrapper, [{
      key: 'render',
      value: function render() {
        var _context = this.context,
            parentModel = _context.model,
            localStore = _context.localStore;

        var resolvedModel = resolveModel(this.props.model, parentModel);

        return _react2.default.createElement(WrappedComponent, _extends({}, this.props, {
          model: resolvedModel,
          store: localStore || undefined
        }));
      }
    }]);

    return ResolvedModelWrapper;
  }(_react.PureComponent);

  ResolvedModelWrapper.displayName = 'Modeled(' + WrappedComponent.displayName + ')';

  process.env.NODE_ENV !== "production" ? ResolvedModelWrapper.propTypes = {
    model: _propTypes2.default.any
  } : void 0;

  ResolvedModelWrapper.contextTypes = {
    model: _propTypes2.default.any,
    localStore: _propTypes2.default.shape({
      subscribe: _propTypes2.default.func,
      dispatch: _propTypes2.default.func,
      getState: _propTypes2.default.func
    })
  };

  return ResolvedModelWrapper;
}