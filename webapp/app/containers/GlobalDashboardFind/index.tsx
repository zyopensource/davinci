import React, {Component} from 'react';
import {Input, Modal, List, Avatar, Row, Icon} from 'antd';
import {compose} from 'redux'
import {connect} from 'react-redux'
import {createStructuredSelector} from 'reselect'
import {loadGlobalDashboards} from './actions'
import {makeSelectGlobalDashboards} from "./selectors";
import injectReducer from "utils/injectReducer";
import reducer from "./reducer";
import injectSaga from "utils/injectSaga";
import saga from "./sagas";
import AutoCompleteData from "components/AutoCompleteData";
import styles from "./index.less"


interface IContainerProps {
  dashboards?: any[],
  onLoadGlobalDashboards: () => void
}

class GlobalDashboardFind extends Component<IContainerProps, {}> {
  state = {
    visible: false,
    filterDashboards: [],
    content: ""
  }

  public componentWillMount() {
    const {onLoadGlobalDashboards} = this.props
    onLoadGlobalDashboards()
  }

  handleCancel = () => {
    this.setState({visible: false});
  };
  showModal = () => {
    this.setState({
      visible: true,
    });
  };
  onSelect = (value) => {
    this.setState({content: value})
    const {dashboards} = this.props
    let dataList = []
    dashboards.forEach(item => {
      let widgets = item.widgets
      if (widgets != undefined) {
        if (item.name == value) {
          dataList.push(item)
        } else {
          for (let i in widgets) {
            if (widgets[i].name == value) {
              dataList.push(item)
              break;
            }
          }
        }
      }
    });
    this.setState({filterDashboards: dataList})
  }
  handleSearch = value => {
    this.setState({content: value})
    const {dashboards} = this.props;
    var dataList = []
    if (value) {
      if (dashboards != undefined) {
        dashboards.forEach(item => {
          let widgets = item.widgets
          if (widgets != undefined) {
            if (item.name.indexOf(value) != -1) {
              dataList.push(item)
            } else {
              for (let i in widgets) {
                if (widgets[i].name.indexOf(value) != -1) {
                  dataList.push(item)
                  break;
                }
              }
            }
          }
        });
      }
      this.setState({
        filterDashboards: dataList
      });
    }
  };

  render() {
    const {visible, filterDashboards, content} = this.state
    let {dashboards} = this.props
    let dataSource = []
    if (dashboards != undefined) {
      dashboards.forEach(item => {
        if (item.widgets != undefined) {
          item.widgets.forEach(data => {
            if (!dataSource.includes(data.name)) {
              dataSource.push(data.name)
            }
          });
        }
      });
    }
    return (
      <div>
        <Modal
          visible={visible}
          title={<AutoCompleteData
            style={{width: '45%', marginBottom: '20px'}}
            placeholder="全局指标搜索"
            dataSource={dataSource}
            onSelect={this.onSelect}
            handleSearch={this.handleSearch}
          />}
          width='90%'
          onCancel={this.handleCancel}
          footer=""
        >
          <List
            className={styles.layout}
            itemLayout="horizontal"
            dataSource={filterDashboards}
            renderItem={item => (
              <List.Item>
                <List.Item.Meta
                  title={
                    <Row>
                      {/*<a*/}
                      {/*  href={`/#/project/${item.projectId}`} target="_blank">*/}
                      {/*  <Avatar*/}
                      {/*    className={styles.mar}*/}
                      {/*    src={`${require(`assets/images/bg${item.projectUrlId}.png`)}`}/>*/}
                      {/*  {item.projectName}*/}
                      {/*</a>*/}
                      {/*<Icon className={styles.mar} type="arrow-right"/>*/}
                      {/*<a*/}
                      {/*  target="_blank"*/}
                      {/*  href={`/#/project/${item.projectId}/portal/${item.dashboardPortalId}/portalName/${item.dashboardPortalName}`}>*/}
                      {/*  <Avatar*/}
                      {/*    className={styles.mar}*/}
                      {/*    src={`${require(`assets/images/bg${item.dashboardPortalUrlId}.png`)}`}/>{item.projectName}*/}
                      {/*  {item.dashboardPortalName}*/}
                      {/*</a>*/}
                      {/*<Icon className={styles.mar} type="arrow-right"/>*/}
                      <a
                        dangerouslySetInnerHTML={{__html: item.name.split(content).join('<span style="color:red;">' + content + '</span>')}}
                        className={styles.title}
                        target="_blank"
                        href={`/#/project/${item.projectId}/portal/${item.dashboardPortalId}/portalName/${item.dashboardPortalName}/dashboard/${item.id}`}>
                      </a>
                    </Row>
                  }
                  description={
                    item.widgets.map(function (widget) {
                      let widgetName = widget.name
                      let values = widgetName.split(content)
                      values = values.join('<span style="color:red;">' + content + '</span>');
                      return (
                        widget.name.indexOf(content) != -1 ?
                          <a
                            target="_blank"
                            dangerouslySetInnerHTML={{__html: values}}
                            className={styles.content}
                            href={`/#/project/${item.projectId}/portal/${item.dashboardPortalId}/portalName/${item.dashboardPortalName}/dashboard/${item.id}`}>
                          </a> : ""

                      )
                    })
                  }
                />
              </List.Item>
            )}
          />
        </Modal>
        <Input.Search value={content} placeholder="全局指标搜索" style={{width: 300}} onSearch={this.showModal} onClick={this.showModal}/>
      </div>
    )
  }
}

const mapStateToProps = createStructuredSelector({
  dashboards: makeSelectGlobalDashboards()
})

export function mapDispatchToProps(dispatch) {
  return {
    onLoadGlobalDashboards: () => dispatch(loadGlobalDashboards()),
  }
}

const withConnect = connect(mapStateToProps, mapDispatchToProps)
const withReducer = injectReducer({key: 'globals', reducer})
const withSaga = injectSaga({key: 'globals', saga})
export default compose(
  withReducer,
  withSaga,
  withConnect
)(GlobalDashboardFind)
