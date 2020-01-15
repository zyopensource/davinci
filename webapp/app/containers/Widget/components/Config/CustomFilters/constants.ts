export enum CustomFilterTypes {
  Input = 'input',
  Select = 'select'
}


export const CustomFilterTypesLocale = {
  [CustomFilterTypes.Input]: '文本输入',
  [CustomFilterTypes.Select]: '下拉选择'
}

export const CustomFilterTypesSetting = [
  CustomFilterTypes.Input,
  CustomFilterTypes.Select
]
