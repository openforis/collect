import { useCallback, useEffect, useRef, useState } from 'react'

import { useRecordEvent, useWindowResize } from 'common/hooks'

import { NodeRelevanceUpdatedEvent } from 'model/event/RecordEvent'
import Arrays from 'utils/Arrays'

const useTabSetContent = (props) => {
  const { tabSetDef, parentEntity } = props
  const { tabs: tabsAll } = tabSetDef

  const version = parentEntity.record.version
  const tabsInVersion = tabsAll.filter((tabDef) => tabDef.isInVersion(version))

  const determineTabsVisible = useCallback(
    () =>
      tabsInVersion.filter((tab) =>
        // check if all items in each tab is relevant or is not empty
        tab.items.some((itemDef) => {
          const { nodeDefinition } = itemDef
          const { alwaysRelevant, hideWhenNotRelevant, id: nodeDefinitionId } = nodeDefinition
          return (
            alwaysRelevant ||
            !hideWhenNotRelevant ||
            parentEntity.childrenRelevanceByDefinitionId[nodeDefinitionId] ||
            parentEntity.hasSomeDescendantNotEmpty({ nodeDefinition })
          )
        })
      ),
    [parentEntity]
  )

  const adjustSize = () => {
    const wrapper = wrapperRef.current
    if (wrapper) {
      const totalHeight = wrapper.parentElement.clientHeight
      wrapper.style.height = `${totalHeight}px`

      const [navTabEl, tabContentEl] = wrapper.children
      if (tabContentEl) {
        const height = totalHeight - navTabEl.clientHeight
        tabContentEl.style.height = `${height}px`
      }
    }
  }

  const getFirstTabId = (tabs) => {
    const firstTab = Arrays.head(tabs)
    return firstTab ? firstTab.id : null
  }

  const [state, setState] = useState({ tabs: [], activeTab: null })
  const { tabs, activeTab } = state
  const wrapperRef = useRef()

  useEffect(() => {
    const tabsVisible = determineTabsVisible()
    setState({ tabs: tabsVisible, activeTab: getFirstTabId(tabsVisible) })
  }, [parentEntity])

  useEffect(() => {
    adjustSize()
  }, [wrapperRef])

  // keep track of last active tab: it will be selected on tabs visibility changes
  const lastActiveTabRef = useRef(activeTab)

  const setActiveTab = (activeTabNew) => {
    setState({ ...state, activeTab: activeTabNew })
    lastActiveTabRef.current = activeTabNew
  }

  useRecordEvent({
    parentEntity,
    onEvent: (event) => {
      if (event instanceof NodeRelevanceUpdatedEvent && event.isRelativeToNode(parentEntity)) {
        const tabsNew = determineTabsVisible()
        const activeTabNew = tabsNew.some((tab) => tab.id === lastActiveTabRef.current)
          ? lastActiveTabRef.current
          : getFirstTabId(tabsNew)
        setState({ tabs: tabsNew, activeTab: activeTabNew })
      }
    },
  })

  useWindowResize(adjustSize)

  return { activeTab, setActiveTab, tabs, wrapperRef }
}

export default useTabSetContent
