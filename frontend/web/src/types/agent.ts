import type { PageQuery } from './api'

export interface AgentTokenVO {
  token: string
  name: string
  expire: number
}

export interface AgentTokenPageQuery extends PageQuery {}

export interface AgentTokenSaveDTO {
  name: string
  expire: number
}
