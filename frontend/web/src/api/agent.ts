import type { AgentTokenPageQuery, AgentTokenSaveDTO, AgentTokenVO, PageResult } from '@/types'
import { normalizePageResult } from '@/utils/page'
import request from '@/utils/request'

export async function getAgentTokenPage(query: AgentTokenPageQuery): Promise<PageResult<AgentTokenVO>> {
  const page = await request<PageResult<AgentTokenVO>>({
    url: '/agent/page',
    method: 'get',
    params: query,
  })
  return normalizePageResult(page)
}

export function createAgentToken(data: AgentTokenSaveDTO): Promise<string> {
  return request<string>({
    url: '/agent',
    method: 'post',
    data,
  })
}

export function deleteAgentToken(name: string): Promise<void> {
  return request<void>({
    url: '/agent',
    method: 'delete',
    data: name,
  })
}
