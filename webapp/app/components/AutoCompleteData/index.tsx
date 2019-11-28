import React, {Component} from 'react';
import {AutoComplete, Input, Icon} from 'antd';

interface IContainerProps {
  dataSource?: any[]
  rowKey?: string
  placeholder?: string
  style: object
  onSelect: (data: Object) => void
  handleSearch?: (datas: Object) => void
}

export default class AutoCompleteData extends Component<IContainerProps, {}> {
  state = {
    dataSource: []
  }

  onSelect = value => {
    const {onSelect} = this.props;
    onSelect(value)
  }
  handleSearch = value => {
    const {dataSource,onSelect,handleSearch} = this.props;

    var dataList = []
    if (value) {
      for (var i in dataSource) {
        if (dataSource[i].indexOf(value) != -1) {
          dataList.push(dataSource[i])
        }
      }
    } else {
      onSelect(null)
      for (var i in dataSource) {
        dataList.push(dataSource[i])
      }
    }
    this.setState({
      dataSource: dataList
    });
    if(handleSearch != undefined){
      handleSearch(value)
    }
  };

  render() {
    const {style, placeholder} = this.props
    let {dataSource} = this.state
    if(dataSource.length == 0 ){dataSource = this.props.dataSource}
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
