import { createSelector } from 'reselect'

const selectDashboards = (state) => state.get('globals')

const makeSelectGlobalDashboards = () => createSelector(
  selectDashboards,
  (state) => state.get('globals_dashboards')
)
export {
  makeSelectGlobalDashboards
}
