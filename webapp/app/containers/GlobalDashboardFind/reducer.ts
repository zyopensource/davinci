import {
  LOAD_GLOBAL_DASHBOARDS,
  LOAD_GLOBAL_DASHBOARDS_SUCCESS,
  LOAD_GLOBAL_DASHBOARDS_FAILURE,

} from './constants'
import {fromJS} from 'immutable'

const initialState = fromJS({
  dashboards: []
})

function globalsReducer(state = initialState, action) {
  const {type, payload} = action
  switch (type) {
    case LOAD_GLOBAL_DASHBOARDS:
      return state
    case LOAD_GLOBAL_DASHBOARDS_SUCCESS:
      return state.set('globals_dashboards', payload.result)
    case LOAD_GLOBAL_DASHBOARDS_FAILURE:
      return state
    default:
      return state
  }
}

export default globalsReducer
