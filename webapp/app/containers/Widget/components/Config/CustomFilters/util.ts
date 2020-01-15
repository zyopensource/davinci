import {IFieldCustomFilterConfig} from './types'
import {CustomFilterTypes} from './constants'

export function getDefaultFieldCustomFilterConfig (): IFieldCustomFilterConfig {
  return {
    formatType: CustomFilterTypes.Input
  }
}
