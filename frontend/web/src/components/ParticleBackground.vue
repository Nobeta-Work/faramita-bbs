<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let animationFrameId: number | null = null

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number
}

const particles: Particle[] = []
const particleCount = 100
const connectionDistance = 150
const mouse = { x: -1000, y: -1000 }

// Get colors from CSS variables
const getColors = () => {
  const style = getComputedStyle(document.documentElement)
  return {
    dot: style.getPropertyValue('--accent-color').trim() || '#006D77',
    line: style.getPropertyValue('--accent-color').trim() || '#006D77',
  }
}

let colors = getColors()

const createParticles = (width: number, height: number) => {
  particles.length = 0
  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      size: Math.random() * 2 + 1,
    })
  }
}

const updateParticles = (width: number, height: number) => {
  particles.forEach(p => {
    p.x += p.vx
    p.y += p.vy

    if (p.x < 0 || p.x > width) p.vx *= -1
    if (p.y < 0 || p.y > height) p.vy *= -1

    // Mouse interaction: attract particles slightly
    const dx = mouse.x - p.x
    const dy = mouse.y - p.y
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (dist < 200) {
      // Gentle attraction towards mouse
      p.x += dx * 0.005
      p.y += dy * 0.005
    }
  })
}

const draw = () => {
  if (!ctx || !canvasRef.value) return
  const { width, height } = canvasRef.value

  ctx.clearRect(0, 0, width, height)

  // Draw lines
  for (let i = 0; i < particles.length; i++) {
    const p1 = particles[i]
    if (!p1) continue

    for (let j = i + 1; j < particles.length; j++) {
      const p2 = particles[j]
      if (!p2) continue
      
      const dx = p1.x - p2.x
      const dy = p1.y - p2.y
      const dist = Math.sqrt(dx * dx + dy * dy)

      if (dist < connectionDistance) {
        ctx.strokeStyle = colors.line
        // More subtle lines
        ctx.globalAlpha = (1 - dist / connectionDistance) * 0.2
        ctx.lineWidth = 0.8
        ctx.beginPath()
        ctx.moveTo(p1.x, p1.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.stroke()
      }
    }

    // Connect to mouse with stronger alpha
    const dx = p1.x - mouse.x
    const dy = p1.y - mouse.y
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (dist < connectionDistance * 1.5) {
      ctx.strokeStyle = colors.line
      ctx.globalAlpha = (1 - dist / (connectionDistance * 1.5)) * 0.4
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(p1.x, p1.y)
      ctx.lineTo(mouse.x, mouse.y)
      ctx.stroke()
    }
  }

  // Draw particles
  particles.forEach(p => {
    ctx!.globalAlpha = 0.6
    ctx!.fillStyle = colors.dot
    ctx!.beginPath()
    ctx!.arc(p.x, p.y, p.size, 0, Math.PI * 2)
    ctx!.fill()
  })

  updateParticles(width, height)
  animationFrameId = requestAnimationFrame(draw)
}

const handleResize = () => {
  if (!canvasRef.value) return
  canvasRef.value.width = window.innerWidth
  canvasRef.value.height = window.innerHeight
  createParticles(canvasRef.value.width, canvasRef.value.height)
}

const handleMouseMove = (e: MouseEvent) => {
  mouse.x = e.clientX
  mouse.y = e.clientY
}

const handleMouseLeave = () => {
  mouse.x = -1000
  mouse.y = -1000
}

onMounted(() => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    handleResize()
    window.addEventListener('resize', handleResize)
    window.addEventListener('mousemove', handleMouseMove)
    window.addEventListener('mouseleave', handleMouseLeave)
    draw()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseleave', handleMouseLeave)
  if (animationFrameId) cancelAnimationFrame(animationFrameId)
})

// Update colors when theme changes (detected by looking at class on html)
const observer = new MutationObserver(() => {
  colors = getColors()
})

onMounted(() => {
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
})

onUnmounted(() => {
  observer.disconnect()
})
</script>

<template>
  <canvas ref="canvasRef" class="particle-canvas"></canvas>
</template>

<style scoped>
.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1; /* Match the z-index of .static-dot-grid */
  pointer-events: none;
  background-color: transparent;
}
</style>
