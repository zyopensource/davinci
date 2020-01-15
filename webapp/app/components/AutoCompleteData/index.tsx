import React, {Component} from 'react'
import {AutoComplete, Input, Icon} from 'antd'

interface IContainerProps {
  dataSource?: any[]
  rowKey?: string
  placeholder?: string
  style: object
  onSelect: (data: any) => void
  handleSearch?: (datas: any) => void
}

export default class AutoCompleteData extends Component<IContainerProps, {}> {
  public state = {
    dataSource: []
  }

  public onSelect = (value) => {
    const {onSelect} = this.props
    onSelect(value)
  }
  public handleSearch = (value) => {
    const {dataSource, onSelect, handleSearch} = this.props

    const dataList = []
    if (value) {
      for (const i in dataSource) {
        if (dataSource[i].indexOf(value) !== -1) {
          dataList.push(dataSource[i])
        }
      }
    } else {
      onSelect(null)
      // tslint:disable-next-line:forin
      for (const i in dataSource) {
        dataList.push(dataSource[i])
      }
    }
    this.setState({
      dataSource: dataList
    })
    if (handleSearch !== undefined) {
      handleSearch(value)
    }
  }

  public render () {
    const {style, placeholder} = this.props
    let {dataSource} = this.state
    if (dataSource.length === 0) {
      dataSource = this.props.dataSource
    }
    return (
      <AutoComplete
        dataSource={dataSource}
        style={style}
        onSelect={this.onSelect}
        onSearch={this.handleSearch}
        size="large"
        placeholder={placeholder}
        filterOption={(inputValue: string, option: any) =>
          option.props.children.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1
        }
      >
        <Input suffix={<Icon type="search"/>}/>
      </AutoComplete>
    )
  }
}
