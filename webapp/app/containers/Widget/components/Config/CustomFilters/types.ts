import {CustomFilterTypes} from './constants'

export interface IFieldCustomFilterConfig {
  formType: CustomFilterTypes
  [CustomFilterTypes.Select]?: {
  }
}
