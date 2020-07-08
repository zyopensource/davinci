import React from 'react'
import {compose} from 'redux'
import {connect} from 'react-redux'
import {makeSelectDepartments} from "containers/Widget/selectors";
import injectReducer from "utils/injectReducer";
import reducer from "containers/Widget/reducer";
import injectSaga from "utils/injectSaga";
import saga from "containers/Widget/sagas";
import {createStructuredSelector} from 'reselect'
import TreeSelect, {ITreeNode} from "containers/Widget/components/Workbench/TreeSelect";

interface DepartmentFilterProps {
  value: string[]
  departments: Array<IDepartment>
  onChange?: (v: string[]) => void

}

interface IDepartment {
  id: string,
  serialNo: string,
  name: string,
  displayName: string,
  parentSerialNo: string,
  enabled: number,
}

const DepartmentFilterForm: React.FC<DepartmentFilterProps> = (props) => {
  const {departments, onChange, value} = props

  const treeData: Array<ITreeNode> = departments.map((department) => {
    let name = department.name
    if (department.enabled == 0) {
      name = `${name}(已废弃)`
    }
    return {
      key: department.serialNo,
      title: name,
      parent: department.parentSerialNo,
      longName: department.displayName
    }
  })

  return (
    <TreeSelect value={value} treeData={treeData} onChange={onChange}/>
  );
};

const mapStateToProps = createStructuredSelector({
  departments: makeSelectDepartments(),
})

export function mapDispatchToProps(dispatch) {
  return {}
}

const withConnect = connect<{}, {}>(mapStateToProps, mapDispatchToProps)

const withReducerWidget = injectReducer({key: 'widget', reducer})
const withSagaWidget = injectSaga({key: 'widget', saga})

export default compose(
  withReducerWidget,
  withSagaWidget,
  withConnect
)(DepartmentFilterForm)
