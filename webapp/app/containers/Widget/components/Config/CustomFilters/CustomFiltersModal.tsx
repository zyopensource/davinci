import React from 'react'
import {CustomFilterTypesLocale, CustomFilterTypesSetting} from './constants'
import {FormComponentProps} from 'antd/lib/form/Form'
import {Button, Form, Modal, Radio} from 'antd'
import {getDefaultFieldCustomFilterConfig} from './util'
import {IFieldCustomFilterConfig} from './types'
import {fromJS} from 'immutable'
import {ICustomFiltersColumn} from "containers/Widget/components/Workbench/Dropbox";

const FormItem = Form.Item
const RadioGroup = Radio.Group

interface ICustomFiltersConfigFormProps extends FormComponentProps {
  visible: boolean
  customFiltersConfig: {}
  onCancel: () => void
  onSave: (formConfig: ICustomFiltersColumn) => void
}

interface ICustomFiltersConfigFormStates {
  localConfig: IFieldCustomFilterConfig
}

class CustomFiltersConfigForm extends React.PureComponent<ICustomFiltersConfigFormProps, ICustomFiltersConfigFormStates> {

  public constructor (props: ICustomFiltersConfigFormProps) {
    super(props)
    const { customFiltersConfig } = props
    this.state = {
      localConfig: customFiltersConfig ? fromJS(customFiltersConfig).toJS() : getDefaultFieldCustomFilterConfig()
    }
  }

  private renderFormatTypes () {
    const {form} = this.props
    const {getFieldDecorator} = form
    const {localConfig} = this.state
    const formatTypesGroup = (
      <FormItem>
        {getFieldDecorator('formType', {
          initialValue: localConfig.formType
        })(
          <RadioGroup>
            {CustomFilterTypesSetting.map((formatType) => (
              <Radio key={formatType} value={formatType}>{CustomFilterTypesLocale[formatType]}</Radio>
            ))}
          </RadioGroup>
        )}
      </FormItem>
    )
    return formatTypesGroup
  }

  private save = () => {
    const {form} = this.props
    form.validateFieldsAndScroll((err, fieldsValues) => {
      if (err) {
        return
      }
      this.props.onSave(fieldsValues)
    })
  }

  private cancel = () => {
    this.props.onCancel()
  }

  private modalFooter = [(
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

  public render () {
    const {visible} = this.props
    return (
      <Modal
        title="类型"
        wrapClassName="ant-modal-small"
        footer={this.modalFooter}
        visible={visible}
        onCancel={this.cancel}
        onOk={this.save}
      >
        <Form>
          {this.renderFormatTypes()}
        </Form>
      </Modal>
    )
  }
}

export default Form.create<ICustomFiltersConfigFormProps>()(CustomFiltersConfigForm)
