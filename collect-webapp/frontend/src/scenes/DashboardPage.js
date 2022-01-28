import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Line } from 'react-chartjs-2'

import Dates from 'utils/Dates'
import L from 'utils/Labels'
import ServiceFactory from 'services/ServiceFactory'
import { DatePicker } from 'common/components/DatePicker'

//const DAYS_OF_WEEK_ABBREVIATED = ['S', 'M', 'T', 'W', 'T', 'F', 'S']
const MONTHS = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'Dicember',
]

const createdRecordsLineColor = '#993300'
const modifiedRecordsLineColor = '#63c2de'
const enteredRecordsLineColor = '#FF9900'
const cleansedRecordsLineColor = '#009900'

const DATE_PICKER_DATE_FORMAT = 'dd/MM/yyyy'

const Period = {
  DAY: 'DAY',
  MONTH: 'MONTH',
  YEAR: 'YEAR',
}

const getStatsByTimeUnit = (timeUnit) => (stats) => {
  switch (timeUnit) {
    case Period.DAY:
      return stats.dailyStats
    case Period.MONTH:
      return stats.monthlyStats
    case Period.YEAR:
    default:
      return stats.yearlyStats
  }
}

const compareDates = (timeUnit, date1, date2) => {
  switch (timeUnit) {
    case Period.DAY:
      return Dates.compare(date1, date2, Dates.DAYS)
    case Period.MONTH:
      return Dates.compare(date1, date2, Dates.MONTHS)
    case Period.YEAR:
    default:
      return Dates.compare(date1, date2, Dates.YEARS)
  }
}

const getPointKey = (timeUnit, date) => {
  switch (timeUnit) {
    case Period.DAY:
      return date.getFullYear() * 10000 + (date.getMonth() + 1) * 100 + date.getDate()
    case Period.MONTH:
      return date.getFullYear() * 100 + (date.getMonth() + 1)
    case Period.YEAR:
    default:
      return date.getFullYear()
  }
}

const incrementDate = (timeUnit, date, increment) => {
  if (!increment) {
    increment = 1
  }
  if (!date) {
    return null
  }
  const newDate = new Date(date.getTime())
  switch (timeUnit) {
    case Period.DAY:
      newDate.setDate(date.getDate() + increment)
      break
    case Period.MONTH:
      newDate.setMonth(date.getMonth() + increment)
      break
    case Period.YEAR:
    default:
      newDate.setFullYear(date.getFullYear() + increment)
  }
  return newDate
}

const formatDate = (timeUnit, date) => {
  switch (timeUnit) {
    case Period.DAY:
      return Dates.format(date)
    case Period.MONTH:
      return MONTHS[date.getMonth()] + ' ' + date.getFullYear()
    case Period.YEAR:
    default:
      return date.getFullYear()
  }
}

class DashboardPage extends Component {
  constructor(props) {
    super(props)

    this.toggle = this.toggle.bind(this)
    this.loadSurveyStats = this.loadSurveyStats.bind(this)
    this.handleTimeUnitChange = this.handleTimeUnitChange.bind(this)
    this.handlePeriodFromChange = this.handlePeriodFromChange.bind(this)
    this.handlePeriodToChange = this.handlePeriodToChange.bind(this)
    this.handleRecordTypeVisibilityChange = this.handleRecordTypeVisibilityChange.bind(this)

    this.state = {
      dropdownOpen: false,
      dailyChartData: null,
      monthlyChartData: null,
      yearlyChartData: null,
      periodFrom: null,
      minPeriodFrom: null,
      periodTo: null,
      maxPeriodTo: null,
      createdRecordsVisible: true,
      modifiedRecordsVisible: true,
      enteredRecordsVisible: true,
      cleansedRecordsVisible: true,
      timeUnit: Period.MONTH,
      noRecordsFound: false,
    }
  }

  toggle() {
    this.setState({
      dropdownOpen: !this.state.dropdownOpen,
    })
  }

  componentDidMount() {
    const survey = this.props.survey
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
    ServiceFactory.recordService.loadRecordsStats(survey).then((stats) => {
      if (stats === null || stats.period === null) {
        this.setState({
          noRecordsFound: true,
        })
      } else {
        const minPeriodFrom = stats.period[0]
        const maxPeriodTo = stats.period[1]
        const periodFrom = minPeriodFrom
        const periodTo = maxPeriodTo
        const dailyChartData = this.createChartData(Period.DAY, stats, periodFrom, periodTo)
        const monthlyChartData = this.createChartData(Period.MONTH, stats, periodFrom, periodTo)
        const yearlyChartData = this.createChartData(Period.YEAR, stats, periodFrom, periodTo)

        this.setState({
          noRecordsFound: false,
          stats: stats,
          dailyChartData: dailyChartData,
          monthlyChartData: monthlyChartData,
          yearlyChartData: yearlyChartData,
          minPeriodFrom: minPeriodFrom,
          maxPeriodTo: maxPeriodTo,
          periodFrom: periodFrom,
          periodTo: periodTo,
        })
      }
    })
  }

  updateChartsData() {
    this.setState({
      dailyChartData: this.createChartData(Period.DAY),
      monthlyChartData: this.createChartData(Period.MONTH),
      yearlyChartData: this.createChartData(Period.YEAR),
    })
  }

  createChartData(
    timeUnit,
    stats = this.state.stats,
    periodFrom = this.state.periodFrom,
    periodTo = this.state.periodTo,
    createdRecordsVisible = this.state.createdRecordsVisible,
    modifiedRecordsVisible = this.state.modifiedRecordsVisible,
    enteredRecordsVisible = this.state.enteredRecordsVisible,
    cleansedRecordsVisible = this.state.cleansedRecordsVisible
  ) {
    const startDate = incrementDate(timeUnit, periodFrom, -1) //consider one day/month/year less
    const endDate = incrementDate(timeUnit, periodTo, 1) //consider one day/month/year more

    const createdRecordsData = {
      label: 'Created records',
      backgroundColor: 'transparent',
      borderColor: createdRecordsLineColor,
      pointHoverBackgroundColor: '#fff',
      fill: false,
      data: [],
    }

    const enteredRecordsData = {
      label: 'Entered records',
      backgroundColor: 'transparent',
      borderColor: enteredRecordsLineColor,
      pointHoverBackgroundColor: '#fff',
      fill: false,
      data: [],
    }

    const cleansedRecordsData = {
      label: 'Cleansed records',
      backgroundColor: 'transparent',
      borderColor: cleansedRecordsLineColor,
      pointHoverBackgroundColor: '#fff',
      fill: false,
      data: [],
    }

    const modifiedRecordsData = {
      label: 'Modified records',
      backgroundColor: 'transparent',
      borderColor: modifiedRecordsLineColor,
      pointHoverBackgroundColor: '#fff',
      fill: false,
      data: [],
    }

    let timeUnitStats = getStatsByTimeUnit(timeUnit)(stats)

    let currentDate = startDate

    const labels = []
    let maxCreatedRecords = 0,
      maxModifiedRecords = 0,
      maxEnteredRecords = 0,
      maxCleansedRecords = 0

    while (compareDates(timeUnit, currentDate, endDate) <= 0) {
      const pointLabel = formatDate(timeUnit, currentDate)
      labels.push(pointLabel)
      let currentPointKey = getPointKey(timeUnit, currentDate)
      let pointStats = timeUnitStats[currentPointKey]
      if (pointStats == null) {
        createdRecordsData.data.push(0)
        modifiedRecordsData.data.push(0)
        enteredRecordsData.data.push(0)
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

        const cleansed = pointStats.cleansed
        maxCleansedRecords = Math.max(cleansed, maxCleansedRecords)
        cleansedRecordsData.data.push(cleansed)
      }
      currentDate = incrementDate(timeUnit, currentDate)
    }

    const opts = {
      responsive: true,
      maintainAspectRatio: false,
      legend: {
        display: false,
      },
      scales: {
        xAxes: [
          {
            gridLines: {
              drawOnChartArea: false,
            },
          },
        ],
        yAxes: [
          {
            ticks: {
              suggestedMax: Math.max(maxCreatedRecords, maxModifiedRecords, maxEnteredRecords) + 20,
            },
          },
        ],
      },
      elements: {
        point: {
          radius: 0,
          hitRadius: 10,
          hoverRadius: 4,
          hoverBorderWidth: 3,
        },
      },
    }

    const datasets = []
    if (createdRecordsVisible) {
      datasets.push(createdRecordsData)
    }
    if (modifiedRecordsVisible) {
      datasets.push(modifiedRecordsData)
    }
    if (enteredRecordsVisible) {
      datasets.push(enteredRecordsData)
    }
    if (cleansedRecordsVisible) {
      datasets.push(cleansedRecordsData)
    }

    return {
      data: {
        labels: labels,
        datasets: datasets,
      },
      opts: opts,
    }
  }

  handleTimeUnitChange(event) {
    this.setState({ timeUnit: event.target.value }, this.updateChartsData)
  }

  handlePeriodFromChange(date) {
    this.setState({ periodFrom: date }, this.updateChartsData)
  }

  handlePeriodToChange(date) {
    this.setState({ periodTo: date }, this.updateChartsData)
  }

  handleRecordTypeVisibilityChange(event) {
    const checked = event.target.checked
    let stateUpdated = null
    switch (event.target.id) {
      case 'createdRecordsVisibilityCheckBox':
        stateUpdated = { createdRecordsVisible: checked }
        break
      case 'modifiedRecordsVisibilityCheckBox':
        stateUpdated = { modifiedRecordsVisible: checked }
        break
      case 'enteredRecordsVisibilityCheckBox':
        stateUpdated = { enteredRecordsVisible: checked }
        break
      case 'cleansedRecordsVisibilityCheckBox':
        stateUpdated = { cleansedRecordsVisible: checked }
        break
      default:
    }
    this.setState(stateUpdated, this.updateChartsData)
  }

  render() {
    const {
      noRecordsFound,
      periodFrom,
      minPeriodFrom,
      periodTo,
      maxPeriodTo,
      createdRecordsVisible,
      modifiedRecordsVisible,
      enteredRecordsVisible,
      cleansedRecordsVisible,
      dailyChartData,
      monthlyChartData,
      yearlyChartData,
      timeUnit,
    } = this.state
    if (noRecordsFound) {
      return <div>No records found</div>
    }
    let chartData
    switch (timeUnit) {
      case Period.DAY:
        chartData = dailyChartData
        break
      case Period.MONTH:
        chartData = monthlyChartData
        break
      case Period.YEAR:
      default:
        chartData = yearlyChartData
    }

    if (chartData == null) {
      return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
    }

    return (
      <div className="animated fadeIn">
        <div className="card">
          <div className="card-block">
            <div className="row">
              <div className="col-sm-12">
                <h4 className="card-title mb-0">Records statistics</h4>
              </div>
              <div className="row">
                <fieldset>
                  <legend>Period</legend>
                  <form className="form-inline">
                    <label>From: </label>
                    <DatePicker
                      minDate={minPeriodFrom}
                      maxDate={maxPeriodTo}
                      value={periodFrom}
                      onChange={this.handlePeriodFromChange}
                    />
                    <label>To: </label>
                    <DatePicker
                      minDate={minPeriodFrom}
                      maxDate={maxPeriodTo}
                      value={periodFrom}
                      onChange={this.handlePeriodToChange}
                    />
                  </form>
                </fieldset>
                <fieldset>
                  <legend>Visible Record Types</legend>
                  <form className="form-inline">
                    <label htmlFor="createdRecordsVisibilityCheckBox" style={{ color: createdRecordsLineColor }}>
                      <input
                        id="createdRecordsVisibilityCheckBox"
                        type="checkbox"
                        className="form-control form-check-input"
                        checked={createdRecordsVisible}
                        onChange={this.handleRecordTypeVisibilityChange}
                      />
                      Created
                    </label>
                    <span style={{ width: '40px' }}></span>
                    <label htmlFor="modifiedRecordsVisibilityCheckBox" style={{ color: modifiedRecordsLineColor }}>
                      <input
                        id="modifiedRecordsVisibilityCheckBox"
                        type="checkbox"
                        className="form-control form-check-input"
                        checked={modifiedRecordsVisible}
                        onChange={this.handleRecordTypeVisibilityChange}
                      />
                      Modified
                    </label>
                    <span style={{ width: '40px' }}></span>
                    <label htmlFor="enteredRecordsVisibilityCheckBox" style={{ color: enteredRecordsLineColor }}>
                      <input
                        id="enteredRecordsVisibilityCheckBox"
                        type="checkbox"
                        className="form-control form-check-input"
                        checked={enteredRecordsVisible}
                        onChange={this.handleRecordTypeVisibilityChange}
                      />
                      Entered
                    </label>
                    <span style={{ width: '40px' }}></span>
                    <label htmlFor="cleansedRecordsVisibilityCheckBox" style={{ color: cleansedRecordsLineColor }}>
                      <input
                        id="cleansedRecordsVisibilityCheckBox"
                        type="checkbox"
                        className="form-control form-check-input"
                        checked={cleansedRecordsVisible}
                        onChange={this.handleRecordTypeVisibilityChange}
                      />
                      Cleansed
                    </label>
                  </form>
                </fieldset>
              </div>
            </div>
            <div className="chart-wrapper" style={{ height: 300 + 'px', marginTop: 40 + 'px' }}>
              <h5>By day</h5>
              <Line data={dailyChartData.data} options={dailyChartData.opts} height={500} />
            </div>
            <div className="chart-wrapper" style={{ height: 300 + 'px', marginTop: 40 + 'px' }}>
              <h5>By month</h5>
              <Line data={monthlyChartData.data} options={monthlyChartData.opts} height={500} />
            </div>
            <div className="chart-wrapper" style={{ height: 300 + 'px', marginTop: 40 + 'px' }}>
              <h5>By year</h5>
              <Line data={yearlyChartData.data} options={yearlyChartData.opts} height={500} />
            </div>
          </div>
        </div>
      </div>
    )
  }
}

const mapStateToProps = (state) => {
  return {
    survey: state.activeSurvey ? state.activeSurvey.survey : null,
  }
}

export default connect(mapStateToProps)(DashboardPage)
