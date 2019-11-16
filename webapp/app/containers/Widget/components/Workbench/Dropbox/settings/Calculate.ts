import {SettingTypes, ItemTypes, ISettingItem, ItemValueTypes} from './type'

const Calculate: ISettingItem = {
  key: 'calculate',
  name: '运算',
  constrants: [{
    settingType: SettingTypes.Indicator,
    itemType: null,
    itemValueType: null
  }],
  sub: false,
  items: [{
    calculate: '运算'
  }]
}

export default Calculate
