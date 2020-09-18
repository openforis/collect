export const SIDEBAR_DROPDOWN_ITEM_TOGGLE = 'SIDEBAR_DROPDOWN_ITEM_TOGGLE'

export const toggleSidebarDropdownItem = (itemId) => ({
  type: SIDEBAR_DROPDOWN_ITEM_TOGGLE,
  itemId,
})
