import update from 'immutability-helper'

import Arrays from 'utils/Arrays'

import {
  REQUEST_SURVEY_SUMMARIES,
  RECEIVE_SURVEY_SUMMARIES,
  INVALIDATE_SURVEY_SUMMARIES,
  SURVEY_CREATED,
  SURVEY_UPDATED,
  SURVEY_DELETED,
} from 'actions/surveys'
import { SHOW_SURVEY_VALIDATION, HIDE_SURVEY_VALIDATION } from '../../actions/surveys'

const defaultState = {
  initialized: false,
  isFetching: false,
  didInvalidate: false,
  items: [],
  validationResultShown: false,
  validationResult: null,
  validationSurvey: null,
}

function surveysList(state = defaultState, action) {
  switch (action.type) {
    case INVALIDATE_SURVEY_SUMMARIES:
      return Object.assign({}, state, {
        didInvalidate: true,
      })
    case REQUEST_SURVEY_SUMMARIES:
      return Object.assign({}, state, {
        isFetching: true,
        didInvalidate: false,
      })
    case RECEIVE_SURVEY_SUMMARIES:
      return Object.assign({}, state, {
        isFetching: false,
        initialized: true,
        didInvalidate: false,
        items: action.summaries,
        lastUpdated: action.receivedAt,
      })
    case SURVEY_CREATED:
    case SURVEY_UPDATED: {
      const { surveySummary } = action
      const { items } = state

      const oldSurveyIndex = items.findIndex((s) => s.uri === surveySummary.uri)
      const newItems =
        oldSurveyIndex < 0 ? [surveySummary].concat(items) : Arrays.replaceItemAt(items, oldSurveyIndex, surveySummary)

      return Object.assign({}, state, {
        items: newItems,
        lastUpdated: action.receivedAt,
      })
    }
    case SURVEY_DELETED: {
      const { surveySummary } = action
      const { items } = state

      const oldItem = items.find((s) => s.uri === surveySummary.uri)
      if (oldItem) {
        let newItems = Arrays.removeItem(items, oldItem)
        return Object.assign({}, state, {
          items: newItems,
          lastUpdated: action.receivedAt,
        })
      } else {
        return state
      }
    }
    case SHOW_SURVEY_VALIDATION:
      const { validationResult, survey } = action
      return {
        ...state,
        validationResultShown: true,
        validationResult,
        validationSurvey: survey,
      }
    case HIDE_SURVEY_VALIDATION:
      return {
        ...state,
        validationResultShown: false,
        validationResult: null,
        validationSurvey: null,
      }
    default:
      return state
  }
}

export default surveysList
