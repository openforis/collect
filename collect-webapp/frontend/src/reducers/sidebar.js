import { SIDEBAR_DROPDOWN_ITEM_TOGGLE } from 'actions/sidebar'

const sidebar = (state = { openNavItems: {} }, action) => {
  switch (action.type) {
    case SIDEBAR_DROPDOWN_ITEM_TOGGLE:
      const { itemId } = action
      const { openNavItems } = state
      const open = Boolean(openNavItems[itemId])
      return { openNavItems: { ...openNavItems, [itemId]: !open } }
    default:
      return state
  }
}

export default sidebar
