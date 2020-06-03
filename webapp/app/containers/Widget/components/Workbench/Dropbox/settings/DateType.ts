import {ISettingItem, ItemTypes, ItemValueTypes, SettingTypes} from './type'

const DataType: ISettingItem = {
  key: 'dataType',
  name: '类型',
  constrants: [{
    settingType: SettingTypes.Dimension,
    itemType: ItemTypes.Category,
    itemValueType: ItemValueTypes.Date
  }],
  sub: true,
  items: [{
    YMD: '年月日',
    Y: '年',
    YQ: '年季',
    YM: '年月',
    YW: '年周',
  }]
}

export default DataType
