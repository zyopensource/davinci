
export enum SettingTypes {
  Dimension = 1 << 0,
  Indicator = 1 << 1,
  Color = 1 << 2,
  Filters = 1 << 3,
  Label = 1 << 4,
  Tip = 1 << 5,
  CustomFilters = 1 << 6,
  Drills = 1 << 7
}
export enum SettingChartTypes {
  Table = 1 << 0,
  Line = 1 << 1,
  Bar = 1 << 2,
  Pie = 1 << 3,
  DoubleYAxis = 1 << 4,
}

export enum ItemTypes {
  Category = 1 << 0,
  Value = 1 << 1
}

export enum ItemValueTypes {
  Number = 1 << 0,
  String = 1 << 1,
  Date = 1 << 2,
  GeoCountry = 1 << 3,
  GeoProvince = 1 << 4,
  GeoCity = 1 << 5
}

export interface ISettingItem {
  key: string
  name: string
  constrants: Array<{
    settingType: SettingTypes
    itemType: ItemTypes
    itemValueType: ItemValueTypes
    settingChartType?:SettingChartTypes
  }>
  sub: boolean
  items: Array<{
    [key: string]: string
  }>
}
