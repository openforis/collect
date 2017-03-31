import React from 'react'
import ReactDOM from 'react-dom'

import Hello from './Hello.jsx'

import './app.less'

ReactDOM.render(
    <Hello who="React"/>,
    document.getElementById('app')
)