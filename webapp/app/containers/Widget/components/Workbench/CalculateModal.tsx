import * as React from 'react'
import {FormComponentProps} from 'antd/lib/form/Form'
import {Form, Input, Checkbox, Select, Button, Modal} from 'antd'
import {CalculateColumn} from './Dropbox'
import {getAggregatorLocale} from '../../components/util'


interface ICalculateConfigFormProps {
  visible: boolean
  fields: any
  calculate: CalculateColumn
  onCancel: () => void
  onClear: () => void
  onSave: (config: CalculateColumn) => void
}


export class CalculateForm extends React.PureComponent<ICalculateConfigFormProps & FormComponentProps> {
  state = {isExpression: false}

  componentWillMount() {
    var {calculate} = this.props
    if (calculate != undefined && calculate.isExpression != undefined && calculate.isExpression != null) {
      this.setState({isExpression: calculate.isExpression})
    }
  }

  private save = () => {
    const {form} = this.props
    var calculate: CalculateColumn
    form.validateFieldsAndScroll((err, fieldsValues) => {
      if (err) {
        return
      }
      const {isExpression} = this.state
      console.log(fieldsValues)
      var expression
      if (isExpression) {
        expression = fieldsValues.expression
      }
      else {
        expression = fieldsValues.agg + "(" + fieldsValues.field + ")"
      }
      var value = {
        ...calculate,
        symbol: fieldsValues.symbol,
        expression: expression,
        isExpression: isExpression
      }
      this.props.onSave(value)
    })
  }

  private cancel = () => {
    this.props.onCancel()
  }
  private clear = () => {
    this.props.onClear()
  }

  private modalFooter = [(
    <Button
      key="clear"
      size="large"
      type='danger'
      style={{float: 'left'}}
      onClick={this.clear}
    >
      清除运算
    </Button>
  ), (
    <Button
      key="cancel"
      size="large"
      onClick={this.cancel}
    >
      取 消
    </Button>
  ), (
    <Button
      key="submit"
      size="large"
      type="primary"
      onClick={this.save}
    >
      保 存
    </Button>
  )]

  private handleChange = e => {
    this.setState({isExpression: e.target.checked});
  };

  public render() {
    var {visible, form, calculate, fields} = this.props
    var {isExpression} = this.state
    var c: CalculateColumn = {symbol: "", expression: "", isExpression: false}
    if (calculate == undefined) {
      calculate = c
    }
    const aggs = ['sum', 'avg', 'count', 'max', 'min',]
    const aggregators = []
    for (var i in aggs) {
      var aggregator = {name: "", value: ""}
      aggregator.value = aggs[i]
      aggregator.name = getAggregatorLocale(aggs[i])
      aggregators.push(aggregator)
    }
    var {symbol, expression} = calculate
    var agg = ""
    var field = ""
    if (!calculate.isExpression && expression != "") {
      var expressions = expression.split(")")[0].split("(")
      agg = expressions[0]
      field = expressions[1]
    }
    const {getFieldDecorator} = form;
    const formLayout = 'horizontal'
    return (
      <Modal
        title="数据运算"
        wrapClassName="ant-modal-small"
        footer={this.modalFooter}
        visible={visible}
        onCancel={this.cancel}
        onOk={this.save}
      >
        <Form layout={formLayout}>
          <Form.Item label="运算符">
            {getFieldDecorator('symbol', {
              rules: [{required: true, message: '请选择'}],
              initialValue: symbol
            })(
              <Select placeholder="请选择" style={{width: 80}}>
                <Select.Option value="+">+</Select.Option>
                <Select.Option value="-">-</Select.Option>
                <Select.Option value="*">*</Select.Option>
                <Select.Option value="/">/</Select.Option>
                <Select.Option value="%">%</Select.Option>
              </Select>,
            )}
          </Form.Item>
          <Form.Item>
            {getFieldDecorator('isExpression', {})(
              <Checkbox onChange={this.handleChange} checked={isExpression}>
                表达式
              </Checkbox>)
            }
          </Form.Item>
          {!isExpression ?
            <Form.Item label="规则函数">
              <Input.Group compact>
                {getFieldDecorator('agg', {
                  rules: [{required: true, message: '请选择函数'}],
                  initialValue: agg
                })(
                  <Select placeholder="请选择函数" style={{width: 100}}>
                    {
                      aggregators.map(function (aggregator, idx) {
                        return <Select.Option key={idx}
                                              value={aggregator.value}>{aggregator.name}</Select.Option>
                      })
                    }
                  </Select>)
                }
                {getFieldDecorator('field', {
                  rules: [{required: true, message: '请选择字段'}],
                  initialValue: field
                })(
                  <Select placeholder="请选择字段" style={{width: 250}}>
                    {
                      fields.map(function (field, idx) {
                        return <Select.Option key={idx} value={field.name}>{field.name}</Select.Option>
                      })
                    }
                  </Select>)
                }
              </Input.Group>
            </Form.Item> :
            <Form.Item label="待运算表达式">
              {getFieldDecorator('expression', {
                rules: [
                  {
                    required: true,
                    message: '不能为空',
                  },
                ],
                initialValue: expression
              })(<Input placeholder=""/>)}
            </Form.Item>
          }
        </Form>
      </Modal>
    )
  }
}

export default Form
  .create()
  (
    CalculateForm
  )
