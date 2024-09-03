import { useCallback } from 'react'
import { Col, FormGroup, Input, Label } from 'reactstrap'
import { Accordion, AccordionDetails, AccordionSummary, Typography } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

import { SessionSelectors } from 'store/session'
import { SurveySelectors } from 'store/survey'
import L from 'utils/Labels'

export const DataExportFilterAccordion = (props) => {
  const { filterObject, onPropChange } = props

  const loggedUser = SessionSelectors.useLoggedInUser()
  const survey = SurveySelectors.useSurvey()

  const createAttributeFormGroup = useCallback(
    (attr, prefix, index) => {
      const name = prefix + index
      const value = filterObject[name] ?? ''
      return (
        <FormGroup row key={name}>
          <Label md={4}>{attr.labelOrName}</Label>
          <Col md={8}>
            <Input
              name={name}
              value={value}
              onChange={(e) => {
                onPropChange({ prop: name, value: e.target.value })
              }}
            />
          </Col>
        </FormGroup>
      )
    },
    [filterObject, onPropChange]
  )

  if (!survey) return null

  const roleInSurvey = survey.userInGroupRole
  const rootEntityDef = survey.schema?.firstRootEntityDefinition
  const keyAttributes = rootEntityDef?.keyAttributeDefinitions
  const summaryAttributes = rootEntityDef?.attributeDefinitionsShownInRecordSummaryList

  const filteredSummaryAttributes = summaryAttributes.filter((a) =>
    loggedUser.canFilterRecordsBySummaryAttribute(a, roleInSurvey)
  )

  const keyAttributeFormGroups = keyAttributes.map((attr, i) => createAttributeFormGroup(attr, 'key', i))

  const summaryFormGroups = filteredSummaryAttributes.map((attr, i) => createAttributeFormGroup(attr, 'summary', i))

  return (
    <Accordion>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography>{L.l('dataManagement.export.filter')}</Typography>
      </AccordionSummary>
      <AccordionDetails>
        <div>
          <FormGroup check row>
            <Label check>
              <Input
                type="checkbox"
                onChange={(e) => onPropChange({ prop: 'exportOnlyOwnedRecords', value: e.target.checked })}
                checked={filterObject['exportOnlyOwnedRecords']}
              />
              {L.l('dataManagement.export.onlyOwnedRecords')}
            </Label>
          </FormGroup>
          <FormGroup row>
            <Label md={3} for="modifiedSince">
              {L.l('dataManagement.export.modifiedSince')}:
            </Label>
            <Col md={4}>
              <Input
                type="date"
                name="modifiedSince"
                id="modifiedSince"
                value={filterObject['modifiedSince']}
                onChange={(e) => onPropChange({ prop: 'modifiedSince', value: e.target.value })}
              />
            </Col>
            <Label md={1} for="modifiedUntil">
              {L.l('dataManagement.export.modifiedUntil')}:
            </Label>
            <Col md={4}>
              <Input
                type="date"
                name="modifiedUntil"
                id="modifiedUntil"
                value={filterObject['modifiedUntil']}
                onChange={(e) => onPropChange({ prop: 'modifiedUntil', value: e.target.value })}
              />
            </Col>
          </FormGroup>
          {keyAttributeFormGroups}
          {summaryFormGroups}
          <FormGroup row>
            <Label md={4}>{L.l('dataManagement.export.filterExpression')}</Label>
            <Col md={8}>
              <Input
                onChange={(e) => onPropChange({ prop: 'filterExpression', value: e.target.value })}
                value={filterObject['filterExpression']}
              />
            </Col>
          </FormGroup>
        </div>
      </AccordionDetails>
    </Accordion>
  )
}
