<template>
  <div class="coming-soon">
    <div class="soon-head">
      <p>{{ title }}</p>
      <span>{{ description }}</span>
    </div>
    <div class="soon-grid">
      <div v-for="item in items" :key="item" class="soon-card">
        <p>{{ item }}</p>
        <span>待接入</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ComingSoon",
  props: {
    title: {
      type: String,
      required: true,
    },
    description: {
      type: String,
      required: true,
    },
    items: {
      type: Array,
      default: () => [],
    },
  },
};
</script>

<style lang="less" scoped>
.coming-soon {
  min-height: 100%;
  padding: 24px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.soon-head p,
.soon-head span,
.soon-card p,
.soon-card span {
  margin: 0;
}

.soon-head p {
  color: #0e2638;
  font-size: 24px;
  font-weight: 700;
}

.soon-head span {
  display: block;
  margin-top: 10px;
  max-width: 680px;
  color: #607d8f;
  font-size: 14px;
  line-height: 1.7;
}

.soon-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.soon-card {
  min-height: 110px;
  padding: 18px;
  border-radius: 8px;
  background: #f7fbff;
  border: 1px solid #dce9f5;
}

.soon-card p {
  color: #0e2638;
  font-size: 17px;
  font-weight: 700;
}

.soon-card span {
  display: inline-flex;
  margin-top: 18px;
  height: 26px;
  align-items: center;
  padding: 0 10px;
  border-radius: 6px;
  background: #e9f7f6;
  color: #057c78;
  font-size: 12px;
}
</style>
