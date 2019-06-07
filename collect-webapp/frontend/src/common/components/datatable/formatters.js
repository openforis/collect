import React from 'react';

import Dates from '../../../utils/Dates';
import CheckedIconFormatter from './CheckedIconFormatter'

export const dateFormatter = (cell, row) =>
    cell > 0 ? Dates.format(new Date(cell)) : ''

export const dateTimeFormatter = (cell, row) =>
    cell > 0 ? Dates.formatDatetime(new Date(cell)) : ''

export const checkedIconFormatter = (cell, row) =>
    <CheckedIconFormatter checked={cell} />
