import {ISettingItem, ItemTypes, ItemValueTypes, SettingTypes} from './type'

const CustomFilters: ISettingItem = {
  key: 'customFilters',
  name: '筛选类型',
  constrants: [{
    settingType: SettingTypes.CustomFilters,
    itemType: ItemTypes.Category,
    itemValueType: ItemValueTypes.String
  }],
  sub: false,
  items: [{
    customFilters: '筛选类型'
  }]
}

export default CustomFilters
