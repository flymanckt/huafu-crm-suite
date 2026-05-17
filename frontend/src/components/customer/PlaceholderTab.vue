<template>
  <div class="placeholder-tab">
    <div class="placeholder-illustration">
      <svg width="140" height="110" viewBox="0 0 140 110" fill="none" xmlns="http://www.w3.org/2000/svg">
        <!-- 背景圆 -->
        <circle cx="70" cy="55" r="48" fill="#F0F5FF"/>
        <!-- 主体：齿轮/构建图标 -->
        <circle cx="70" cy="50" r="22" fill="none" stroke="#B8D0F5" stroke-width="3"/>
        <circle cx="70" cy="50" r="10" fill="#B8D0F5" opacity="0.6"/>
        <!-- 齿轮齿 -->
        <rect x="66" y="24" width="8" height="8" rx="2" fill="#B8D0F5"/>
        <rect x="66" y="68" width="8" height="8" rx="2" fill="#B8D0F5"/>
        <rect x="44" y="46" width="8" height="8" rx="2" fill="#B8D0F5"/>
        <rect x="88" y="46" width="8" height="8" rx="2" fill="#B8D0F5"/>
        <!-- 装饰线：代表进行中 -->
        <line x1="36" y1="86" x2="60" y2="86" stroke="#B8D0F5" stroke-width="3" stroke-linecap="round"/>
        <line x1="66" y1="86" x2="90" y2="86" stroke="#D0E4FF" stroke-width="3" stroke-linecap="round"/>
        <line x1="96" y1="86" x2="104" y2="86" stroke="#E8F2FF" stroke-width="3" stroke-linecap="round"/>
        <!-- 小装饰圆 -->
        <circle cx="30" cy="36" r="3" fill="#D0E4FF"/>
        <circle cx="110" cy="30" r="2" fill="#E8F2FF"/>
        <circle cx="118" cy="72" r="2" fill="#D0E4FF"/>
      </svg>
    </div>
    <div class="placeholder-title">功能开发中</div>
    <div class="placeholder-sub">敬请期待</div>
    <div v-if="description" class="placeholder-desc">{{ description }}</div>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.placeholder-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  user-select: none;
}
.placeholder-illustration {
  margin-bottom: 20px;
  opacity: 0.85;
}
.placeholder-title {
  font-size: 16px;
  font-weight: 600;
  color: #3a3a3a;
  margin-bottom: 4px;
}
.placeholder-sub {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}
.placeholder-desc {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
