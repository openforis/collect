import React from 'react'
import { Col, Label, Row } from 'reactstrap'

const CompositeAttributeFormItem = ({ field, label, inputField, labelWidth = 50 }) => {
  const widthPx = `${labelWidth}px`
  return (
    <Row key={field}>
      <Col style={{ width: widthPx, maxWidth: widthPx }}>
        <Label>{label}</Label>
      </Col>
      <Col>{inputField}</Col>
    </Row>
  )
}

export default CompositeAttributeFormItem
