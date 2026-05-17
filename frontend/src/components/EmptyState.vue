<template>
  <div class="empty-state">
    <div class="empty-illustration">
      <svg width="120" height="100" viewBox="0 0 120 100" fill="none" xmlns="http://www.w3.org/2000/svg">
        <!-- 简约插画：文件夹+问号 -->
        <rect x="10" y="30" width="70" height="55" rx="6" fill="#EBF2FF" stroke="#B8D0F5" stroke-width="2"/>
        <path d="M10 42H80" stroke="#B8D0F5" stroke-width="2"/>
        <rect x="20" y="22" width="20" height="12" rx="3" fill="#B8D0F5"/>
        <!-- 问号圆 -->
        <circle cx="85" cy="35" r="22" fill="#F0F5FF" stroke="#B8D0F5" stroke-width="2"/>
        <text x="85" y="42" text-anchor="middle" font-size="22" font-weight="bold" fill="#7C9FCC">?</text>
        <!-- 装饰点 -->
        <circle cx="100" cy="75" r="3" fill="#B8D0F5" opacity="0.6"/>
        <circle cx="108" cy="68" r="2" fill="#B8D0F5" opacity="0.4"/>
        <circle cx="95" cy="82" r="2" fill="#B8D0F5" opacity="0.5"/>
      </svg>
    </div>
    <div class="empty-message">{{ message }}</div>
    <div v-if="subMessage" class="empty-sub">{{ subMessage }}</div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: String,
    default: '暂无数据'
  },
  subMessage: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  user-select: none;
}
.empty-illustration {
  margin-bottom: 20px;
  opacity: 0.9;
}
.empty-message {
  font-size: 15px;
  font-weight: 500;
  color: #3a3a3a;
  margin-bottom: 6px;
}
.empty-sub {
  font-size: 13px;
  color: #999;
}
</style>
