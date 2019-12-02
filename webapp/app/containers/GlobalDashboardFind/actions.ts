import {
  LOAD_GLOBAL_DASHBOARDS,
  LOAD_GLOBAL_DASHBOARDS_SUCCESS,
  LOAD_GLOBAL_DASHBOARDS_FAILURE,
} from './constants'


export function loadGlobalDashboards () {
  return {
    type: LOAD_GLOBAL_DASHBOARDS
  }
}
export function globalDashboardsLoaded (result) {
  return {
    type: LOAD_GLOBAL_DASHBOARDS_SUCCESS,
    payload: {
      result
    }
  }
}

export function loadGlobalDashboardsFail () {
  return {
    type: LOAD_GLOBAL_DASHBOARDS_FAILURE
  }
}
