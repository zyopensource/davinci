import React, {useState, useEffect} from 'react'
import {compose} from 'redux'
import {connect} from 'react-redux'
import {Card, Input, Tag, Tree} from 'antd';
import {DataNode} from 'rc-tree/lib/interface';
import {AntTreeNode} from 'antd/lib/tree/Tree'
import {makeSelectDepartments} from "containers/Widget/selectors";
import {WidgetActions} from "containers/Widget/actions";
import injectReducer from "utils/injectReducer";
import reducer from "containers/Widget/reducer";
import injectSaga from "utils/injectSaga";
import saga from "containers/Widget/sagas";
import {createStructuredSelector} from 'reselect'

const styles = require("./Workbench.less")
const {loadDepartments} = WidgetActions


const {Search} = Input

interface DepartmentFilterProps {
  value: string[]
  departments: any[]
  onChange?: (v: string[]) => void

}

interface ITreeNode {
  key: string,
  title: string,
  parent: string,
  children?: Array<ITreeNode>
}

const DepartmentFilterForm: React.FC<DepartmentFilterProps> = (props) => {
  const {departments, onChange, value} = props

  const treeData: Array<ITreeNode> = departments.map((department: any) => {
    const enabled = department.enabled
    let name = department.name
    if (enabled == 0) {
      name = `${name}(已废弃)`
    }
    return {key: department.serialNo, title: name, parent: department.parentSerialNo}
  })

  const searchTreeById = (pId) => {
    const children = treeData.filter(v => v.parent == pId)
    if (children) {
      children.map(v => {
        const children1 = searchTreeById(v.key)
        v.children = children1
        return v
      })
    }
    return children
  }
  //已选择的部门key
  const [departmentKeys, setDepartmentKeys] = useState<Array<string>>([]);
  //已选择的部门最root节点集合
  const [checkedRootNodesPositions, setCheckedRootNodesPositions] = useState<Array<any>>([]);
  const initRootTreeData = () => {
    const roots = treeData.filter((tree: any) => !tree.parent || tree.parent == '').map((root) => {
      root.children = searchTreeById(root.key)
      return root;
    })
    return roots
  }
  const [rootTreeData, setRootTreeData] = useState<Array<any>>([]);
  useEffect(() => {
    if (value && value instanceof Array) {
      const values = departments.filter(d => value.includes(d.displayName)).map(v => v.serialNo)
      setDepartmentKeys(values)
      const displayNames = departments.filter(d => values.includes(d.serialNo)).map(v => v.displayName)
      onChange(displayNames)
    }
  }, [])
  useEffect(() => {
    if (rootTreeData.length == 0) {
      setRootTreeData(initRootTreeData())
    }
  }, [treeData])
  useEffect(() => {
    const displayNames = departments.filter(d => departmentKeys.includes(d.serialNo)).map(v => v.displayName)
    onChange(displayNames)
  }, [departmentKeys])

  const updateTreeData = (list: DataNode[], key: React.Key, children: DataNode[]): DataNode[] => {
    return list.map(node => {
      if (node.key === key) {
        return {
          ...node,
          children,
        };
      } else if (node.children) {
        return {
          ...node,
          children: updateTreeData(node.children, key, children),
        };
      }
      return node;
    });
  }
  const onCheck = (checkedKeys: any, {checkedNodesPositions}: any) => {
    const rootNodesPositions =
      checkedNodesPositions.filter(({pos}) => checkedValFilter(pos, checkedNodesPositions))
    setCheckedRootNodesPositions(rootNodesPositions)
    setDepartmentKeys(rootNodesPositions.map(v => v.node.key))
  };
  const onClose = (key: any) => {
    const arr = departmentKeys.map(v => v)
    arr.splice(arr.findIndex(v => v === key), 1);
    setDepartmentKeys(arr)
  }
  const onSearch = (value: any) => {
    if (value == '') {
      setRootTreeData(initRootTreeData())
      return
    }
    setRootTreeData(treeData.filter((tree: any) => tree.title && tree.title.indexOf(value) != -1))
  }
  /**
   * 校验是否包含子节点
   * @param l_pos
   */
  const checkedValFilter = (l_pos: string, checkedNodesPositions: any) => {
    const checks = checkedNodesPositions.filter(({pos}: any) => l_pos.indexOf(`${pos}-`) == 0 && l_pos != pos)
    return checks.length == 0
  }
  return (
    <div style={{display: 'flex', minHeight: 300}} className={styles.departmentFilter}>
      <Card
        // title={
        //   <Search
        //     style={{borderRadius: 16}}
        //     placeholder="搜索部门" size='small'
        //     onSearch={onSearch}
        //     onChange={(e) => {
        //       if (e.target.value == '') {
        //         setRootTreeData(initRootTreeData())
        //       }
        //     }}/>
        // }
        style={{width: 600}}
        size="small">
        <Tree
          checkedKeys={departmentKeys}
          defaultCheckedKeys={departmentKeys}
          checkable
          onCheck={onCheck}
          treeData={rootTreeData}
        />
      </Card>
      <Card title="已选择" style={{width: 400}} size="small">
        {departmentKeys.map(key => {
          const departmentData = treeData.filter(tree => tree.key === key)[0]
          if (!departmentData) {
            return
          }
          return <Tag color="green" key={key} closable onClose={() => onClose(key)}>{departmentData.title}</Tag>
        })}
      </Card>
    </div>
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
