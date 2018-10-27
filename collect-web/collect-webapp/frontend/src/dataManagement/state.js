import { recordDataTableDefaultState } from "./recordDataTable/state";

export const defaultDataManagementState = {
    recordDataTable: recordDataTableDefaultState
}

export const getDataManagementState = (state) => state.dataManagement
