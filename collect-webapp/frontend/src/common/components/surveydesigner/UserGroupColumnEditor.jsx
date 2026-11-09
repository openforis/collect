import React from 'react'
import { Input } from 'reactstrap'

const UserGroupColumnEditor = (props) => {
  const { row, userGroups, onUpdate } = props
  const { userGroupId } = row

  const handleInputChange = (event) => {
    const userGroupId = parseInt(event.target.value, 10)
    onUpdate({ userGroupId })
  }

  const options = userGroups.map((u) => (
    <option key={u.id} value={u.id}>
      {u.systemDefined ? '---' + u.label + '---' : u.label}
    </option>
  ))

  return (
    <span>
      <Input type="select" value={userGroupId} onChange={handleInputChange}>
        {options}
      </Input>
    </span>
  )
}

export default UserGroupColumnEditor
