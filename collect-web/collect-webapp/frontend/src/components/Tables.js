import React from 'react';
import { SizePerPageDropDown } from 'react-bootstrap-table';

export default class Tables {

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