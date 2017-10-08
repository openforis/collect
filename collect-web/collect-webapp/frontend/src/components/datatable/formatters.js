import React, { Component } from 'react';
import Moment from 'moment';
import CheckedIconFormatter from './CheckedIconFormatter'

export function dateFormatter(cell, row) {
    if (cell > 0){
        return Moment(new Date(cell)).format('DD/MM/YYYY');
    }
}

export function dateTimeFormatter(cell, row) {
    if (cell > 0){
        return Moment(new Date(cell)).format('DD/MM/YYYY HH:mm');
    }
}

export function checkedIconFormatter(cell, row) {
    return <CheckedIconFormatter checked={cell} />
}
