import React, { Component } from 'react';
import { SizePerPageDropDown } from 'react-bootstrap-table';

export default class Forms {

    static renderSizePerPageDropUp = (props) => {
        return (
            <SizePerPageDropDown
                className='size-per-page'
                btnContextual='btn-light'
                variation='dropup'
                onClick={() => props.toggleDropDown()}
                {...props} />
        );
    }
}