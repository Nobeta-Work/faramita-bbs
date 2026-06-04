import { computed, nextTick, ref, watch } from 'vue'

export interface TocItem {
    id: string
    text: string
    level: number
    parentId?: string
    hasChildren?: boolean
}

export function useToc() {
    const tocItems = ref<TocItem[]>([])
    const activeTocId = ref('')
    const expandedTocIds = ref<Set<string>>(new Set())
    let scrollSpyCleanup: (() => void) | null = null

    const visibleTocItems = computed(() => {
        return tocItems.value.filter((item) => {
            if (item.level === 1) {
                return true
            }

            let currentParentId = item.parentId
            while (currentParentId) {
                if (!expandedTocIds.value.has(currentParentId)) {
                    return false
                }

                currentParentId = tocItems.value.find((tocItem) => tocItem.id === currentParentId)?.parentId
            }

            return true
        })
    })

    function toggleTocExpand(id: string, event?: Event): void {
        event?.stopPropagation()

        const next = new Set(expandedTocIds.value)
        if (next.has(id)) {
            next.delete(id)
        } else {
            next.add(id)
        }

        expandedTocIds.value = next
    }

    function extractToc(container: HTMLElement | null | undefined): void {
        if (!container) {
            tocItems.value = []
            expandedTocIds.value = new Set()
            activeTocId.value = ''
            return
        }

        const previousActiveId = activeTocId.value
        const headings = Array.from(container.querySelectorAll<HTMLHeadingElement>('h1, h2, h3, h4, h5, h6'))
        const items: TocItem[] = []
        const parentStack: TocItem[] = []

        headings.forEach((heading, index) => {
            let id = heading.id
            if (!id) {
                id = `heading-${index}`
                heading.id = id
            }

            const level = Number(heading.tagName.substring(1))
            while (parentStack.length > 0 && parentStack[parentStack.length - 1]!.level >= level) {
                parentStack.pop()
            }

            const parent = parentStack[parentStack.length - 1]
            if (parent) {
                parent.hasChildren = true
            }

            const text = heading.innerText.replace(/#$/, '').trim()
            const item: TocItem = {
                id,
                text,
                level,
                parentId: parent?.id,
                hasChildren: false,
            }

            items.push(item)
            parentStack.push(item)
        })

        tocItems.value = items
        expandedTocIds.value = new Set(items.filter((item) => item.hasChildren).map((item) => item.id))
        activeTocId.value = items.some((item) => item.id === previousActiveId) ? previousActiveId : items[0]?.id ?? ''
    }

    function setupScrollSpy(container: HTMLElement | null | undefined): void {
        scrollSpyCleanup?.()
        scrollSpyCleanup = null

        if (!container) {
            return
        }

        const headings = Array.from(container.querySelectorAll<HTMLHeadingElement>('h1, h2, h3, h4, h5, h6'))
        if (headings.length === 0) {
            return
        }

        const scrollRoot = document.querySelector<HTMLElement>('.main-layout .n-layout-scroll-container')
            ?? document.querySelector<HTMLElement>('.n-layout-scroll-container')
            ?? window
        let animationFrame = 0

        const updateActiveHeading = () => {
            animationFrame = 0
            const rootTop = scrollRoot instanceof Window ? 0 : scrollRoot.getBoundingClientRect().top
            const activationLine = rootTop + 120
            let activeHeading = headings[0]

            for (const heading of headings) {
                if (heading.getBoundingClientRect().top <= activationLine) {
                    activeHeading = heading
                } else {
                    break
                }
            }

            if (activeHeading?.id) {
                activeTocId.value = activeHeading.id
            }
        }

        const requestUpdate = () => {
            if (!animationFrame) {
                animationFrame = window.requestAnimationFrame(updateActiveHeading)
            }
        }

        scrollRoot.addEventListener('scroll', requestUpdate, { passive: true })
        window.addEventListener('resize', requestUpdate, { passive: true })
        updateActiveHeading()

        scrollSpyCleanup = () => {
            if (animationFrame) {
                window.cancelAnimationFrame(animationFrame)
                animationFrame = 0
            }
            scrollRoot.removeEventListener('scroll', requestUpdate)
            window.removeEventListener('resize', requestUpdate)
        }
    }

    function scrollToHeading(container: HTMLElement | null | undefined, id: string): void {
        const element = container?.querySelector<HTMLElement>(`#${CSS.escape(id)}`) ?? document.getElementById(id)
        if (!element) {
            return
        }

        element.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
        })
        activeTocId.value = id
    }

    function cleanupToc(): void {
        scrollSpyCleanup?.()
        scrollSpyCleanup = null
    }

    function scrollActiveTocItemIntoPanel(): void {
        const activeItem = document.querySelector<HTMLElement>('.toc-active')
        const panel = activeItem?.closest<HTMLElement>('.toc-card, .toc-panel')
        if (!activeItem || !panel || panel.scrollHeight <= panel.clientHeight) {
            return
        }

        const activeRect = activeItem.getBoundingClientRect()
        const panelRect = panel.getBoundingClientRect()
        const padding = 18

        if (activeRect.top < panelRect.top + padding) {
            panel.scrollBy({ top: activeRect.top - panelRect.top - padding, behavior: 'smooth' })
            return
        }

        if (activeRect.bottom > panelRect.bottom - padding) {
            panel.scrollBy({ top: activeRect.bottom - panelRect.bottom + padding, behavior: 'smooth' })
        }
    }

    watch(activeTocId, (id) => {
        if (!id) {
            return
        }

        const next = new Set(expandedTocIds.value)
        let changed = false
        let currentParentId = tocItems.value.find((item) => item.id === id)?.parentId

        while (currentParentId) {
            if (!next.has(currentParentId)) {
                next.add(currentParentId)
                changed = true
            }
            currentParentId = tocItems.value.find((item) => item.id === currentParentId)?.parentId
        }

        if (changed) {
            expandedTocIds.value = next
        }

        nextTick(scrollActiveTocItemIntoPanel)
    })

    return {
        activeTocId,
        cleanupToc,
        expandedTocIds,
        extractToc,
        scrollToHeading,
        setupScrollSpy,
        tocItems,
        toggleTocExpand,
        visibleTocItems,
    }
}
