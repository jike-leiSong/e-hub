<template>
  <div class="identity-access-center">
    <section class="center-hero">
      <div>
        <p class="hero-kicker">IDENTITY & ACCESS</p>
        <h2>统一管理账号、角色、菜单、接口和数据权限</h2>
        <p class="hero-copy">
          当前项目已具备登录账号、产品开通推导菜单和接口访问控制骨架，后续在该中心统一沉淀用户、角色和数据权限配置。
        </p>
      </div>
      <div class="hero-tags">
        <span>已落地：登录账号</span>
        <span>已落地：产品权限推导</span>
        <span>规划中：角色权限</span>
      </div>
    </section>

    <section class="lane-grid">
      <article v-for="item in implementedLanes" :key="item.title" class="lane-card implemented">
        <p class="lane-title">{{ item.title }}</p>
        <strong>{{ item.status }}</strong>
        <span>{{ item.desc }}</span>
      </article>
    </section>

    <section class="board">
      <div class="board-head">
        <p>能力分层</p>
        <span>把原“用户管理 + 权限管理”收敛为同一治理中心，避免账号、角色、权限、数据范围四处分散。</span>
      </div>
      <div class="board-grid">
        <article v-for="item in plannedLanes" :key="item.title" class="board-card">
          <p>{{ item.title }}</p>
          <span>{{ item.desc }}</span>
        </article>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: "IdentityAccessCenter",
  data() {
    return {
      implementedLanes: [
        {
          title: "账号基础",
          status: "已落地",
          desc: "基于 console_user 管理登录账号、用户类型、聚合商和企业归属",
        },
        {
          title: "菜单控制",
          status: "已落地",
          desc: "基于平台类型和产品开通状态生成 owner/customer 菜单结构",
        },
        {
          title: "接口控制",
          status: "已落地",
          desc: "按负荷聚合、电价服务和 owner API 前缀做请求访问控制",
        },
        {
          title: "角色治理",
          status: "待升级",
          desc: "当前仍是代码推导，后续需要角色、权限点和数据范围配置化",
        },
      ],
      plannedLanes: [
        {
          title: "用户账号",
          desc: "账号创建、启停、组织归属、租户绑定和登录状态管理",
        },
        {
          title: "角色权限",
          desc: "角色定义、菜单权限、按钮权限、接口权限和角色继承",
        },
        {
          title: "数据权限",
          desc: "租户、聚合商、企业、资源和设备级的数据范围授权",
        },
        {
          title: "权限审计",
          desc: "权限变更留痕、授权审批和高风险操作审计",
        },
      ],
    };
  },
};
</script>

<style lang="less" scoped>
.identity-access-center {
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
    linear-gradient(135deg, rgba(11, 36, 71, 0.96), rgba(17, 94, 89, 0.92)),
    #0e2638;
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
.lane-title,
.lane-card strong,
.lane-card span,
.board-head p,
.board-head span,
.board-card p,
.board-card span {
  margin: 0;
}

.hero-kicker {
  color: #9bf6ea;
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
  color: #d7e6ef;
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
  border: 1px solid rgba(255, 255, 255, 0.16);
  font-size: 12px;
}

.lane-grid,
.board-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.lane-card,
.board {
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.lane-card {
  min-height: 122px;
  padding: 18px;
}

.lane-title {
  color: #607d8f;
  font-size: 13px;
}

.lane-card strong {
  display: block;
  margin-top: 12px;
  color: #0e2638;
  font-size: 24px;
  font-weight: 700;
}

.lane-card span {
  display: block;
  margin-top: 10px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.6;
}

.lane-card.implemented {
  background: linear-gradient(180deg, #ffffff, #f7fbff);
}

.board {
  padding: 18px;
}

.board-head span {
  display: block;
  margin-top: 8px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.7;
}

.board-grid {
  margin-top: 18px;
}

.board-card {
  min-height: 118px;
  padding: 18px;
  border-radius: 8px;
  background: #f7fbff;
  border: 1px solid #dbe8f2;
}

.board-card p {
  color: #0e2638;
  font-size: 17px;
  font-weight: 700;
}

.board-card span {
  display: block;
  margin-top: 12px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1280px) {
  .lane-grid,
  .board-grid {
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

  .lane-grid,
  .board-grid {
    grid-template-columns: 1fr;
  }
}
</style>
