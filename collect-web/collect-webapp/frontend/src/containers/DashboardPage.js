import React, { Component } from 'react';
import { Bar, Line } from 'react-chartjs-2';
import { Dropdown, DropdownMenu, DropdownItem, Input, Progress } from 'reactstrap';
import { connect } from 'react-redux'
import Dates from 'utils/Dates';

import ServiceFactory from 'services/ServiceFactory'

const DAYS_OF_WEEK_ABBREVIATED = ['S', 'M', 'T', 'W', 'T', 'F', 'S']
const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'Dicember']

const createdRecordsLineColor = '#20a8d8';
const modifiedRecordsLineColor = '#4dbd74';
const enteredRecordsLineColor = '#63c2de';
const brandWarning = '#f8cb00';
const brandDanger = '#f86c6b';

class DashboardPage extends Component {

  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.loadSurveyStats = this.loadSurveyStats.bind(this)
    this.handleTimeUnitChange = this.handleTimeUnitChange.bind(this)

    this.state = {
      dropdownOpen: false,
      dailyChartData: null,
      monthlyChartData: null,
      yearlyChartData: null,
      periodStart: null,
      periodTo: null,
      timeUnit: 'MONTH',
      noRecordsFound: false
    };
  }

  toggle() {
    this.setState({
      dropdownOpen: !this.state.dropdownOpen
    });
  }

  componentDidMount() {
    const survey = this.props.survey;
    this.loadSurveyStats(survey)
  }

  componentWillReceiveProps(nextProps) {
    const survey = nextProps.survey
    this.loadSurveyStats(survey)
  }

  loadSurveyStats(survey) {
    if (survey == null) {
      this.setState({ dailyChartData: null, monthlyChartData: null, yearlyChartData: null })
      return
    }
    ServiceFactory.recordService.loadRecordsStats(survey).then(stats => {
      if (stats === null || stats.period === null) {
        this.setState({
          noRecordsFound: true
        })
      } else {
        const periodStart = stats.period[0]
        const periodTo = stats.period[1]
        const dailyChartData = this.createChartData('DAY', stats, periodStart, periodTo)
        const monthlyChartData = this.createChartData('MONTH', stats, periodStart, periodTo)
        const yearlyChartData = this.createChartData('YEAR', stats, periodStart, periodTo)
  
        this.setState({
          noRecordsFound: false,
          stats: stats,
          dailyChartData: dailyChartData,
          monthlyChartData: monthlyChartData,
          yearlyChartData: yearlyChartData,
          periodStart: periodStart,
          periodTo: periodTo
        })
      }
    })
  }

  updateChartsData() {
    this.setState({
      ailyChartData: this.createChartData('DAY'),
      monthlyChartData: this.createChartData('MONTH'),
      yearlyChartData: this.createChartData('YEAR')
    })
  }

  createChartData(timeUnit, stats, periodStart = this.state.periodStart, periodTo = this.state.periodTo) {
    const startDate = incrementDate(periodStart, -1) //consider one day/month/year less
    const endDate = incrementDate(periodTo, 1) //consider one day/month/year more

    const createdRecordsData = {
      label: 'Created records',
      backgroundColor: 'transparent',
      borderColor: createdRecordsLineColor,
      pointHoverBackgroundColor: '#fff',
      fill: false,
      data: []
    }

    const enteredRecordsData = {
      label: 'Entered records',
      backgroundColor: 'transparent',
      borderColor: enteredRecordsLineColor,
      pointHoverBackgroundColor: '#fff',
      fill: false,
      data: []
    }

    const modifiedRecordsData = {
      label: 'Modified records',
      backgroundColor: 'transparent',
      borderColor: modifiedRecordsLineColor,
      pointHoverBackgroundColor: '#fff',
      fill: false,
      data: []
    }

    let timeUnitStats
    switch (timeUnit) {
      case 'DAY':
        timeUnitStats = stats.dailyStats
        break
      case 'MONTH':
        timeUnitStats = stats.monthlyStats
        break
      case 'YEAR':
        timeUnitStats = stats.yearlyStats
        break
    }

    function getPointKey(date) {
      switch (timeUnit) {
        case 'DAY':
          return date.getFullYear() * 10000 + (date.getMonth() + 1) * 100 + date.getDate()
        case 'MONTH':
          return date.getFullYear() * 100 + (date.getMonth() + 1)
        case 'YEAR':
          return date.getFullYear()
      }
    }

    function incrementDate(date, increment) {
      if (! increment) {
        increment = 1
      }
      const newDate = new Date(date.getTime());
      switch (timeUnit) {
        case 'DAY':
          newDate.setDate(date.getDate() + increment)
          break
        case 'MONTH':
          newDate.setMonth(date.getMonth() + increment)
          break
        case 'YEAR':
          newDate.setFullYear(date.getFullYear() + increment)
          break
      }
      return newDate
    }

    function formatDate(date) {
      switch (timeUnit) {
        case 'DAY':
          return Dates.format(date)
        case 'MONTH':
          return MONTHS[date.getMonth()] + ' ' + date.getFullYear()
        case 'YEAR':
          return date.getFullYear()
      }
    }

    function compareDates(date1, date2) {
      switch (timeUnit) {
        case 'DAY':
          return Dates.compare(date1, date2, Dates.DAYS)
        case 'MONTH':
          return Dates.compare(date1, date2, Dates.MONTHS)
        case 'YEAR':
          return Dates.compare(date1, date2, Dates.YEARS)
      }
    }

    let currentDate = startDate

    const labels = [];
    let maxCreatedRecords = 0, maxModifiedRecords = 0, maxEnteredRecords = 0, maxCleansedRecords = 0

    while (compareDates(currentDate, endDate) <= 0) {
      const pointLabel = formatDate(currentDate)
      labels.push(pointLabel)
      let currentPointKey = getPointKey(currentDate)
      let pointStats = timeUnitStats[currentPointKey]
      if (pointStats == null) {
        createdRecordsData.data.push(0);
        modifiedRecordsData.data.push(0);
        enteredRecordsData.data.push(0);
      } else {
        const created = pointStats.created
        maxCreatedRecords = Math.max(created, maxCreatedRecords)
        createdRecordsData.data.push(created)

        const modified = pointStats.modified
        maxModifiedRecords = Math.max(modified, maxModifiedRecords)
        modifiedRecordsData.data.push(modified)

        const entered = pointStats.entered
        maxEnteredRecords = Math.max(entered, maxEnteredRecords)
        enteredRecordsData.data.push(entered)
      }
      currentDate = incrementDate(currentDate)
    }

    const opts = {
      responsive: true,
      maintainAspectRatio: false,
      legend: {
        display: false
      },
      scales: {
        xAxes: [{
          gridLines: {
            drawOnChartArea: false,
          }
        }],
        yAxes: [{
          ticks: {
            suggestedMax: Math.max(maxCreatedRecords, maxModifiedRecords, maxEnteredRecords) + 20
          }
        }]
      },
      elements: {
        point: {
          radius: 0,
          hitRadius: 10,
          hoverRadius: 4,
          hoverBorderWidth: 3,
        }
      }
    }

    return {
      data: {
        labels: labels,
        datasets: [createdRecordsData, modifiedRecordsData, enteredRecordsData]
      },
      opts: opts
    }
  }

  handleTimeUnitChange(event) {
    this.setState({ timeUnit: event.target.value })
  }

  handlePeriodStartChange(event) {
    this.setState({periodStart: new Date(event.target.value)})
  }

  handlePeriodToChange(event) {
    this.setState({periodTo: new Date(event.target.value)})
  }
   
  render() {
    if (this.state.noRecordsFound) {
      return <div>No records found</div>
    }
    const timeUnit = this.state.timeUnit

    let chartData
    switch (timeUnit) {
      case 'DAY':
        chartData = this.state.dailyChartData
        break
      case 'MONTH':
        chartData = this.state.monthlyChartData
        break
      case 'YEAR':
        chartData = this.state.yearlyChartData
        break
    }

    const timeUnitChecks = ['DAY', 'MONTH', 'YEAR'].map(u =>
      <label key={u} className={'btn btn-outline-secondary ' + (timeUnit == u ? 'active' : '')}>
        <input type="radio" name="options" value={u} checked={timeUnit == u}
          onChange={this.handleTimeUnitChange} /> {u}
      </label>
    )

    if (chartData == null) {
      return <div>Select a survey first</div>
    }

    const lineChart = <Line data={chartData.data} options={chartData.opts} height={500} />

    return (
      <div className="animated fadeIn">
        <div className="card">
          <div className="card-block">
            <div className="row">
              <div className="col-sm-5">
                <h4 className="card-title mb-0">Records statistics</h4>
                <div>
                    <Input type="date" name="periodStart" id="periodStart" placeholder="from" onChange={this.handlePeriodStartChange} />
                    <Input type="date" name="periodTo" id="periodTo" placeholder="to" onChange={this.handlePeriodToChange} />
                </div>
              </div>
              {/*
              <div className="col-sm-7 hidden-sm-down">
                <div className="btn-toolbar float-right" role="toolbar" aria-label="Toolbar with button groups">
                  <div className="btn-group mr-3" data-toggle="buttons" aria-label="First group">
                    {timeUnitChecks}
                  </div>
                </div>
              </div>
              */}
            </div>
            <div className="chart-wrapper" style={{ height: 300 + 'px', marginTop: 40 + 'px' }}>
              <h5>By day</h5>
              <Line data={this.state.dailyChartData.data} options={this.state.dailyChartData.opts} height={500} />
            </div>
            <div className="chart-wrapper" style={{ height: 300 + 'px', marginTop: 40 + 'px' }}>
              <h5>By month</h5>
              <Line data={this.state.monthlyChartData.data} options={this.state.monthlyChartData.opts} height={500} />
            </div>
            <div className="chart-wrapper" style={{ height: 300 + 'px', marginTop: 40 + 'px' }}>
              <h5>By year</h5>
              <Line data={this.state.yearlyChartData.data} options={this.state.yearlyChartData.opts} height={500} />
            </div>
          </div>
        </div>
      </div>
    )
  }
}

const mapStateToProps = state => {
  return {
    survey: state.preferredSurvey ? state.preferredSurvey.survey : null
  }
}

export default connect(mapStateToProps)(DashboardPage)