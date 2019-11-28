import {all, call, put, takeLatest} from 'redux-saga/effects'
import {LOAD_GLOBAL_DASHBOARDS,} from './constants'
import {globalDashboardsLoaded, loadGlobalDashboardsFail,} from './actions'

import request from 'utils/request'
import api from 'utils/api'
import {errorHandler} from 'utils/util'

export function* getGlobalDashboards() {
  try {
    const asyncData = yield call(request, `${api.portal}/global/dashboards`)
    const dashboards = asyncData.payload
    yield put(globalDashboardsLoaded(dashboards))
  } catch (err) {
    yield put((loadGlobalDashboardsFail()))
    errorHandler(err)
  }
}

export default function* rootGlobalSaga(): IterableIterator<any> {
  yield all([
    takeLatest(LOAD_GLOBAL_DASHBOARDS, getGlobalDashboards)
  ])
}
