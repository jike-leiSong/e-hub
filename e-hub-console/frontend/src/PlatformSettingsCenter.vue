<template>
  <div class="platform-settings-center">
    <section class="center-hero">
      <div>
        <p class="hero-kicker">PLATFORM SETTINGS</p>
        <h2>把平台参数、字典、集成配置和审计集中治理</h2>
        <p class="hero-copy">
          系统设置不再承载用户和权限配置，只负责平台级配置项、第三方接入、通知通道和审计留痕。
        </p>
      </div>
      <div class="hero-tags">
        <span>平台参数</span>
        <span>接口配置</span>
        <span>操作审计</span>
      </div>
    </section>

    <section class="setting-grid">
      <article v-for="item in settingModules" :key="item.title" class="setting-card">
        <p>{{ item.title }}</p>
        <strong>{{ item.stage }}</strong>
        <span>{{ item.desc }}</span>
      </article>
    </section>

    <section class="roadmap">
      <div class="roadmap-head">
        <p>收敛原则</p>
        <span>系统设置只保留平台级配置，不再混入用户、角色、产品订阅这类业务治理项。</span>
      </div>
      <div class="roadmap-grid">
        <article v-for="item in roadmapItems" :key="item.title" class="roadmap-card">
          <p>{{ item.title }}</p>
          <span>{{ item.desc }}</span>
        </article>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: "PlatformSettingsCenter",
  data() {
    return {
      settingModules: [
        {
          title: "平台参数",
          stage: "待建设",
          desc: "平台开关、会话超时、默认策略和展示参数配置",
        },
        {
          title: "字典中心",
          stage: "待建设",
          desc: "业务字典、区域字典、物联字典和前端枚举统一维护",
        },
        {
          title: "集成配置",
          stage: "待建设",
          desc: "外部接口地址、密钥、凭证、回调和限流策略配置",
        },
        {
          title: "审计中心",
          stage: "待建设",
          desc: "操作日志、权限变更、产品开通和接口调用审计",
        },
      ],
      roadmapItems: [
        {
          title: "账号权限迁出",
          desc: "账号、角色、授权配置全部归入身份与权限中心",
        },
        {
          title: "产品订阅迁出",
          desc: "客户开通、续期、额度和凭证配置归入租户中心",
        },
        {
          title: "参数标准化",
          desc: "统一参数命名、变更记录和多环境发布机制",
        },
        {
          title: "审计闭环",
          desc: "平台设置、权限和产品订阅的操作链路统一留痕",
        },
      ],
    };
  },
};
</script>

<style lang="less" scoped>
.platform-settings-center {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.center-hero {
  min-height: 168px;
  padding: 24px;
  border-radius: 10px;
  background:
    linear-gradient(135deg, rgba(59, 83, 20, 0.95), rgba(15, 118, 110, 0.88)),
    #244230;
  color: #ffffff;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.hero-kicker,
.hero-copy,
.center-hero h2,
.hero-tags span,
.setting-card p,
.setting-card strong,
.setting-card span,
.roadmap-head p,
.roadmap-head span,
.roadmap-card p,
.roadmap-card span {
  margin: 0;
}

.hero-kicker {
  color: #dff6b3;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.center-hero h2 {
  margin-top: 10px;
  max-width: 680px;
  font-size: 30px;
  line-height: 1.25;
}

.hero-copy {
  margin-top: 12px;
  max-width: 760px;
  color: #e1edd8;
  font-size: 14px;
  line-height: 1.7;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.hero-tags span {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 12px;
}

.setting-grid,
.roadmap-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.setting-card,
.roadmap {
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.setting-card {
  min-height: 122px;
  padding: 18px;
}

.setting-card p {
  color: #607d8f;
  font-size: 13px;
}

.setting-card strong {
  display: block;
  margin-top: 12px;
  color: #0e2638;
  font-size: 24px;
  font-weight: 700;
}

.setting-card span {
  display: block;
  margin-top: 10px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.6;
}

.roadmap {
  padding: 18px;
}

.roadmap-head span {
  display: block;
  margin-top: 8px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.7;
}

.roadmap-grid {
  margin-top: 18px;
}

.roadmap-card {
  min-height: 112px;
  padding: 18px;
  border-radius: 8px;
  background: #fbfcf5;
  border: 1px solid #e3e6d0;
}

.roadmap-card p {
  color: #0e2638;
  font-size: 17px;
  font-weight: 700;
}

.roadmap-card span {
  display: block;
  margin-top: 12px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1280px) {
  .setting-grid,
  .roadmap-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .center-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-tags {
    justify-content: flex-start;
  }

  .setting-grid,
  .roadmap-grid {
    grid-template-columns: 1fr;
  }
}
</style>
