import React, {useEffect, useState} from 'react'
import {Card, Tag, Tree} from 'antd';

const styles = require("./Workbench.less")


interface DepartmentFilterProps {
  value: string[]
  treeData: Array<ITreeNode>
  onChange: (v: string[]) => void

}

export interface ITreeNode {
  key: string,
  title: string,
  parent: string,
  longName: string,
  children?: Array<ITreeNode>
}

const TreeSelect: React.FC<DepartmentFilterProps> = (props) => {
  const {treeData, value, onChange} = props

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
  //已选择的key
  const [selectKeys, setSelectKeys] = useState<Array<string>>([]);
  const [rootTreeData, setRootTreeData] = useState<Array<any>>([]);
  const initRootTreeData = () => {
    const roots = treeData.filter((tree: any) => !tree.parent || tree.parent == '').map((root) => {
      root.children = searchTreeById(root.key)
      return root;
    })
    return roots
  }
  /**
   * 初始化选中的值
   */
  useEffect(() => {
    if (value && value instanceof Array) {
      const values = treeData.filter(d => value.includes(d.longName)).map(v => v.key)
      setSelectKeys(values)
      const displayNames = treeData.filter(d => values.includes(d.key)).map(v => v.longName)
      onChange(displayNames)
    }
  }, [])
  /**
   * 初始化树
   */
  useEffect(() => {
    console.log(value)
    if (rootTreeData.length == 0) {
      setRootTreeData(initRootTreeData())
    }
  }, [treeData])
  /**
   * 保存每次树的选中值
   */
  useEffect(() => {
    const longNames = treeData.filter(d => selectKeys.includes(d.key)).map(v => v.longName)
    onChange(longNames)
  }, [selectKeys])

  /**
   * 树的选中
   * @param checkedKeys
   * @param checkedNodesPositions
   */
  const onCheck = (checkedKeys: any, {checkedNodesPositions}: any) => {
    const rootNodesPositions =
      checkedNodesPositions.filter(({pos}) => checkedValFilter(pos, checkedNodesPositions))
    setSelectKeys(rootNodesPositions.map(v => v.node.key))
  };
  /**
   * 选中面板tab关闭
   * @param key
   */
  const onClose = (key: any) => {
    const arr = selectKeys.map(v => v)
    arr.splice(arr.findIndex(v => v === key), 1);
    setSelectKeys(arr)
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
        style={{width: 600}}
        size="small">
        <Tree
          checkedKeys={selectKeys}
          defaultCheckedKeys={selectKeys}
          checkable
          onCheck={onCheck}
          treeData={rootTreeData}
        />
      </Card>
      <Card title="已选择" style={{width: 400}} size="small">
        {selectKeys.map(key => {
          const data = treeData.filter(tree => tree.key === key)[0]
          if (!data) {
            return
          }
          return <Tag color="green" key={key} closable onClose={() => onClose(key)}>{data.title}</Tag>
        })}
      </Card>
    </div>
  );
};

export default TreeSelect;
