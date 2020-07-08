import React from 'react'
import {compose} from 'redux'
import {connect} from 'react-redux'
import {makeSelectSubjects} from "containers/Widget/selectors";
import injectReducer from "utils/injectReducer";
import reducer from "containers/Widget/reducer";
import injectSaga from "utils/injectSaga";
import saga from "containers/Widget/sagas";
import {createStructuredSelector} from 'reselect'
import TreeSelect, {ITreeNode} from "containers/Widget/components/Workbench/TreeSelect";

interface SubjectFilterProps {
  value: string[]
  subjects: Array<ISubject>
  onChange?: (v: string[]) => void

}

interface ISubject {
  subjectNumber: string,
  subjectCode: string,
  subjectName: string,
  subjectLongName: string,
  subjectId: string,
  superiorSubjectsId: string,
}

const SubjectFilterForm: React.FC<SubjectFilterProps> = (props) => {
  const {subjects, onChange, value} = props
  const treeData: Array<ITreeNode> = subjects.map((costCenter: ISubject) => {
    return {
      key: costCenter.subjectId,
      title: costCenter.subjectName,
      parent: costCenter.superiorSubjectsId,
      longName: costCenter.subjectLongName
    }
  })
  return (
    <TreeSelect value={value} treeData={treeData} onChange={onChange}/>
  );
};

const mapStateToProps = createStructuredSelector({
  subjects: makeSelectSubjects(),
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
)(SubjectFilterForm)
