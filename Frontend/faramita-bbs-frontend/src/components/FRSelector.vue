<template>
  <div
    class="fr-selector"
    tabindex="0"
    ref="selectorRef"
    @focusout="closeDropdown"
  >
    <div class="fr-selector-input" @click="toggleDropdown" ref="inputRef">
      <span v-if="selectedLabel">{{ selectedLabel }}</span>
      <span v-else class="fr-selector-placeholder">{{ placeholder }}</span>
      <span class="fr-selector-arrow" :class="{ open: dropdownVisible }">&#9662;</span>
    </div>
    <teleport to="body">
      <transition name="fr-dropdown-fade">
        <ul
          v-if="dropdownVisible"
          class="fr-selector-dropdown"
          :style="dropdownStyle"
        >
          <li
            v-for="item in options"
            :key="item.value"
            :class="{ selected: item.value === modelValue }"
            @mousedown.prevent="select(item.value)"
          >
            {{ item.label }}
          </li>
        </ul>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, defineProps, defineEmits, nextTick } from 'vue'

const props = defineProps<{
  modelValue: string | number
  options: Array<{ label: string, value: string | number }>
  placeholder?: string
}>()
const emits = defineEmits(['update:modelValue'])

const dropdownVisible = ref(false)
const selectorRef = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLElement | null>(null)
const dropdownStyle = ref({})

const selectedLabel = computed(() => {
  const found = props.options.find(opt => opt.value === props.modelValue)
  return found ? found.label : ''
})

function toggleDropdown() {
  dropdownVisible.value = !dropdownVisible.value
  if (dropdownVisible.value) {
    nextTick(() => {
      updateDropdownPosition()
    })
  }
}
function closeDropdown() {
  dropdownVisible.value = false
}
function select(val: string | number) {
  emits('update:modelValue', val)
  closeDropdown()
}

function updateDropdownPosition() {
  const input = inputRef.value
  if (!input) return
  const rect = input.getBoundingClientRect()
  dropdownStyle.value = {
    position: 'absolute',
    left: `${rect.left}px`,
    top: `${rect.bottom + window.scrollY}px`,
    width: `${rect.width}px`,
    zIndex: 9999,
    boxShadow: '0 8px 24px rgba(138,43,226,0.18)',
    borderRadius: '12px',
    background: 'rgba(255,255,255,0.95)',
    border: '1px solid #e3d7fa',
    transition: 'box-shadow 0.3s, background 0.3s'
  }
}

// 点击外部关闭下拉
function handleClickOutside(e: MouseEvent) {
  if (
    !selectorRef.value?.contains(e.target as Node) &&
    !(e.target as HTMLElement).classList.contains('fr-selector-dropdown')
  ) {
    closeDropdown()
  }
}
watch(dropdownVisible, (visible) => {
  if (visible) {
    window.addEventListener('click', handleClickOutside)
  } else {
    window.removeEventListener('click', handleClickOutside)
  }
})
</script>

<style scoped>
.fr-selector {
  position: relative;
  display: inline-block;
  min-width: 120px;
  user-select: none;
}
.fr-selector-input {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  color: #e5ddf0;
  padding: 10px 32px 10px 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  border: 1px solid black;
  box-shadow: 0 2px 8px rgba(138,43,226,0.08);
  transition: box-shadow 0.2s, border 0.2s;
}
.fr-selector-input:hover, .fr-selector-input:focus {
  box-shadow: 0 4px 16px rgba(138,43,226,0.16);
  border-color: #b779f1;
  background: rgba(71, 69, 69, 0.2);
  border: 1px solid rgb(255, 150, 237);
}
.fr-selector-placeholder {
  color: #b7b7b7;
  font-style: italic;
}
.fr-selector-arrow {
  position: absolute;
  right: 12px;
  pointer-events: none;
  color: #b779f1;
  font-size: 16px;
  transition: transform 0.3s;
}
.fr-selector-arrow.open {
  transform: rotate(180deg);
}
.fr-selector-dropdown {
  /* 动态样式覆盖 */
  margin-top: 4px;
  max-height: 220px;
  overflow-y: auto;
  padding: 0;
  list-style: none;
  box-shadow: 0 8px 24px rgba(138,43,226,0.18);
  border-radius: 12px;
  background: rgba(255,255,255,0.95);
  border: 1px solid #e3d7fa;
  animation: fr-dropdown-fade-in 0.3s;
}
.fr-selector-dropdown li {
  padding: 12px 18px;
  color: #4c3a6a;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  border-radius: 8px;
  margin: 2px 6px;
  font-size: 15px;
}
.fr-selector-dropdown li.selected {
  background: linear-gradient(90deg, #e3d7fa 60%, #b779f1 100%);
  color: #8a2be2;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(138,43,226,0.08);
  transition: background 0.2s, color 0.2s;
}
.fr-selector-dropdown li:hover {
  background: linear-gradient(90deg, #f3eaff 60%, #d1b3fa 100%);
  color: #9370db;
}
.fr-selector-dropdown li:active {
  background: linear-gradient(90deg, #d1b3fa 60%, #b779f1 100%);
  color: #4c3a6a;
}
@keyframes fr-dropdown-fade-in {
  from { opacity: 0; transform: translateY(-8px);}
  to { opacity: 1; transform: translateY(0);}
}
.fr-dropdown-fade-enter-active,
.fr-dropdown-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fr-dropdown-fade-enter-from,
.fr-dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
.fr-dropdown-fade-enter-to,
.fr-dropdown-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
