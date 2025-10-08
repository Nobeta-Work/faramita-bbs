declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 添加图片文件的类型声明
declare module '*.png' {
  const value: string
  export default value
}

declare module '*.jpg' {
  const value: string
  export default value
}

declare module '*.jpeg' {
  const value: string
  export default value
}

declare module '*.gif' {
  const value: string
  export default value
}

declare module '*.svg' {
  const value: string
  export default value
}

// 音频文件说明
declare module '*.mp3' {
    const value: string;
    export default value;
}

declare module '*.m4a' {
    const value: string;
    export default value;
}

declare module '*.wav' {
    const value: string;
    export default value;
}

declare module '*.ogg' {
    const value: string;
    export default value;
}