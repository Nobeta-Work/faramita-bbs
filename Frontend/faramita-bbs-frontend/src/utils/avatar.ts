import defaultAvatarUrl from '@/assets/images/default-avatar.png'

const DEV_AVATAR_BASE_URL = 'http://localhost:8080/bbs/i/'
const PROD_AVATAR_BASE_URL = 'https://faramita.online/bbs/i/'

const LEGACY_DEFAULT_AVATARS = new Set([
  '',
  'null',
  'undefined',
  'default_avator.jpg',
  'default_avator',
  'default_avatar.png',
  'default_avatar',
  'src/assets/images/default-avatar.png',
])

const normalizeBaseUrl = (baseUrl: string) => (baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`)

const getAvatarBaseUrl = () => {
  const envBaseUrl = import.meta.env.VITE_AVATAR_BASE_URL as string | undefined
  if (envBaseUrl?.trim()) {
    return normalizeBaseUrl(envBaseUrl.trim())
  }

  return normalizeBaseUrl(import.meta.env.DEV ? DEV_AVATAR_BASE_URL : PROD_AVATAR_BASE_URL)
}

export const AVATAR_BASE_URL = getAvatarBaseUrl()
export const DEFAULT_AVATAR_URL = defaultAvatarUrl

export const resolveAvatarUrl = (avatar?: string | null) => {
  const value = avatar?.trim()

  if (!value || LEGACY_DEFAULT_AVATARS.has(value.toLowerCase())) {
    return DEFAULT_AVATAR_URL
  }

  if (/^(https?:|blob:|data:)/i.test(value)) {
    return value
  }

  const normalized = value.replace(/^\/+/, '')
  if (normalized.toLowerCase().startsWith('avatar/')) {
    return `${AVATAR_BASE_URL}${normalized}`
  }

  return `${AVATAR_BASE_URL}avatar/${normalized}`
}
