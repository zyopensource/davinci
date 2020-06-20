import React from 'react'
import {compose} from 'redux'
import {connect} from 'react-redux'
import {makeSelectCostCenters} from "containers/Widget/selectors";
import injectReducer from "utils/injectReducer";
import reducer from "containers/Widget/reducer";
import injectSaga from "utils/injectSaga";
import saga from "containers/Widget/sagas";
import {createStructuredSelector} from 'reselect'
import TreeSelect, {ITreeNode} from "containers/Widget/components/Workbench/TreeSelect";

interface CostCenterFilterProps {
  value: string[]
  costCenters: Array<ICostCenter>
  onChange?: (v: string[]) => void

}

interface ICostCenter {
  departmentCode: string,
  departmentId: string,
  departmentLongName: string,
  departmentName: string,
  superiorDepartmentId: string,
}

const CostCenterFilterForm: React.FC<CostCenterFilterProps> = (props) => {
  const {costCenters, onChange, value} = props
  const treeData: Array<ITreeNode> = costCenters.map((costCenter: ICostCenter) => {
    return {
      key: costCenter.departmentId,
      title: costCenter.departmentName,
      parent: costCenter.superiorDepartmentId,
      longName: costCenter.departmentLongName
    }
  })
  return (
    <TreeSelect value={value} treeData={treeData} onChange={onChange}/>
  );
};

const mapStateToProps = createStructuredSelector({
  costCenters: makeSelectCostCenters(),
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
)(CostCenterFilterForm)
