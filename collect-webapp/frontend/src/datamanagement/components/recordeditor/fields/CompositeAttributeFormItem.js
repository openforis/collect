import React from 'react'
import { Col, Label, Row } from 'reactstrap'

const CompositeAttributeFormItem = ({ field, label, inputField, labelWidth = 50 }) => (
  <Row key={field}>
    <Col style={{ maxWidth: `${labelWidth}px` }}>
      <Label>{label}</Label>
    </Col>
    <Col>{inputField}</Col>
  </Row>
)

export default CompositeAttributeFormItem
