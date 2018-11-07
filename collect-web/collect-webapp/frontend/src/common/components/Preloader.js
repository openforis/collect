import React, { Component } from 'react';

export default class Preloader extends Component {

    render() {
        const { loading } = this.props
        
        return <div className={loading ? '' : 'loaded'}>
            <div className="loader-wrapper">
                <div className="loader"></div>
                <div className="loader-section section-left"></div>
                <div className="loader-section section-right"></div>
            </div>
            {! loading && this.props.children}
        </div>
    }
}