import {ISettingItem, ItemTypes, ItemValueTypes, SettingTypes,SettingChartTypes} from './type'

const FastCalculate: ISettingItem = {
  key: 'fastCalculate',
  name: '快速计算',
  constrants: [{
    settingType: SettingTypes.Indicator,
    itemType: ItemTypes.Value,
    itemValueType: ItemValueTypes.Number,
    settingChartType: SettingChartTypes.Table | SettingChartTypes.DoubleYAxis
  }],
  sub: true,
  items: [{
    yoy: '同比',
    qoq: '环比'
  }]
}

export default FastCalculate
