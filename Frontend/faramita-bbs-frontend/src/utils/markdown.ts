const PREVIEW_ROOT_SELECTOR = '.vditor-reset, .vditor-preview .vditor-reset, .vditor-preview__content, .vditor-ir__preview, .vditor-wysiwyg__preview'

const normalizeCodeLanguage = (className: string) => {
  const match = className.match(/(?:^|\s)language-([^\s]+)/i)
  if (!match || !match[1]) {
    return 'plain text'
  }

  const language = match[1].trim()
  if (!language || language.toLowerCase() === 'plaintext' || language.toLowerCase() === 'plain-text') {
    return 'plain text'
  }

  return language
}

const decorateCodeBlocks = (container: HTMLElement) => {
  container.querySelectorAll<HTMLPreElement>('pre > code').forEach((code) => {
    const pre = code.parentElement as HTMLPreElement | null
    if (!pre) {
      return
    }

    // Skip the editable source blocks and only decorate rendered markdown output.
    if (pre.closest('.vditor-wysiwyg__pre, .vditor-ir__marker--pre')) {
      return
    }

    pre.dataset.mdLanguage = normalizeCodeLanguage(code.className)
  })
}

const normalizeCaptionTitle = (value?: string | null) => {
  const title = value?.trim()
  if (!title) {
    return ''
  }

  return title.replace(/^["']+|["']+$/g, '').trim()
}

const unwrapCaptionHost = (host: HTMLElement) => {
  const parent = host.parentNode
  const image = host.querySelector<HTMLImageElement>('img')

  if (parent && image) {
    parent.insertBefore(image, host)
  }

  host.remove()
}

const decorateImageCaptions = (container: HTMLElement) => {
  if (container.isContentEditable) {
    return
  }

  const captionedHosts = new Set<HTMLElement>()

  container.querySelectorAll<HTMLImageElement>('img').forEach((img) => {
    const title = normalizeCaptionTitle(img.getAttribute('title'))
    if (!title) {
      return
    }

    let host = img.parentElement as HTMLElement | null
    if (!host) {
      return
    }

    if (host.dataset.mdCaptionHost !== 'image') {
      const wrapper = document.createElement('span')
      wrapper.dataset.mdCaptionHost = 'image'
      host.insertBefore(wrapper, img)
      wrapper.appendChild(img)
      host = wrapper
    }

    host.dataset.mdCaption = title
    captionedHosts.add(host)
  })

  container.querySelectorAll<HTMLElement>('[data-md-caption]').forEach((block) => {
    if (!captionedHosts.has(block)) {
      delete block.dataset.mdCaption

      if (block.dataset.mdCaptionHost === 'image') {
        unwrapCaptionHost(block)
      }
    }
  })
}

export const decorateMarkdownContent = (root?: ParentNode | null) => {
  if (!root) {
    return
  }

  const containers = new Set<HTMLElement>()

  if (root instanceof HTMLElement && root.matches(PREVIEW_ROOT_SELECTOR)) {
    containers.add(root)
  }

  root.querySelectorAll<HTMLElement>(PREVIEW_ROOT_SELECTOR).forEach((container) => {
    containers.add(container)
  })

  containers.forEach((container) => {
    decorateCodeBlocks(container)
    decorateImageCaptions(container)
  })
}
