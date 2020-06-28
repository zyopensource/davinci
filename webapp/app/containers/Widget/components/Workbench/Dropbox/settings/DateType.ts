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
    ymd: '年月日',
    y: '年',
    yq: '年季',
    ym: '年月',
    yw: '年周',
  }]
}

export default DataType
