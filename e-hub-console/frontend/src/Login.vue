<template>
  <div class="login-page">
    <section class="login-shell">
      <div class="login-main">
        <div class="login-brand">
          <div class="login-mark">e</div>
          <div>
            <p class="login-product">e-hub</p>
            <p class="login-title">能源电力聚合运营平台</p>
          </div>
        </div>

        <div class="login-heading">
          <p>统一身份登录</p>
          <h1>运营平台</h1>
        </div>

        <el-form class="login-form" :model="form" @submit.native.prevent>
          <el-form-item>
            <el-input
              v-model.trim="form.account"
              placeholder="请输入账号"
              autocomplete="username"
              prefix-icon="el-icon-user"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              placeholder="请输入密码"
              autocomplete="current-password"
              prefix-icon="el-icon-lock"
              show-password
              @keyup.enter.native="submit"
            />
          </el-form-item>
          <div class="login-options">
            <el-checkbox v-model="form.remember">记住账号</el-checkbox>
            <span>console</span>
          </div>
          <el-button class="login-button" type="primary" :loading="loading" @click="submit">
            登录
          </el-button>
        </el-form>
      </div>

      <aside class="login-aside">
        <div class="aside-top">
          <span>ENERGY OPERATIONS</span>
          <strong>负荷聚合 · 电价服务</strong>
        </div>
        <div class="power-board">
          <div class="power-row">
            <span></span>
            <i></i>
          </div>
          <div class="power-row short">
            <span></span>
            <i></i>
          </div>
          <div class="power-grid">
            <div>
              <p>聚合负荷</p>
              <strong>24.8 MW</strong>
            </div>
            <div>
              <p>响应完成率</p>
              <strong>96.2%</strong>
            </div>
            <div>
              <p>收益结算</p>
              <strong>186k</strong>
            </div>
          </div>
        </div>
      </aside>
    </section>
  </div>
</template>

<script>
import service from "@/services/http";

export default {
  name: "Login",
  data() {
    return {
      loading: false,
      form: {
        account: localStorage.getItem("ehub-account") || "",
        password: "",
        remember: Boolean(localStorage.getItem("ehub-account")),
      },
    };
  },
  methods: {
    submit() {
      if (!this.form.account) {
        this.$message.warning("请输入账号");
        return;
      }
      if (!this.form.password) {
        this.$message.warning("请输入密码");
        return;
      }
      this.loading = true;
      service({
        method: "post",
        url: "/auth/login",
        data: {
          username: this.form.account,
          password: this.form.password,
        },
        headers: {
          "Content-Type": "application/json;charset=UTF-8",
        },
      })
        .then(response => {
          const body = response.data || {};
          if (body.code !== 200) {
            this.$message.error(body.msg || "登录失败");
            return;
          }
          const authUser = body.data || {};
          if (this.form.remember) {
            localStorage.setItem("ehub-account", this.form.account);
          } else {
            localStorage.removeItem("ehub-account");
          }
          sessionStorage.setItem("token", authUser.token || "");
          sessionStorage.setItem("ehub-token", authUser.token || "");
          sessionStorage.setItem("ticket", authUser.token || "");
          sessionStorage.setItem("aggregatorId", authUser.aggregatorId || "");
          sessionStorage.setItem("entId", authUser.entId || "");
          this.$emit("login", { ...this.form, authUser });
        })
        .catch(error => {
          const message =
            error && error.response && error.response.data
              ? error.response.data.msg
              : "登录失败";
          this.$message.error(message || "登录失败");
        })
        .finally(() => {
          this.loading = false;
        });
    },
  },
};
</script>

<style lang="less" scoped>
.login-page {
  min-height: 100vh;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(90deg, rgba(20, 94, 125, 0.06) 1px, transparent 1px),
    linear-gradient(0deg, rgba(20, 94, 125, 0.05) 1px, transparent 1px),
    #eef4f7;
  background-size: 32px 32px;
}

.login-shell {
  width: min(1080px, 100%);
  min-height: 620px;
  display: grid;
  grid-template-columns: minmax(360px, 460px) 1fr;
  border: 1px solid #d8e4ea;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 22px 60px rgba(15, 44, 63, 0.12);
  overflow: hidden;
}

.login-main {
  padding: 64px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 54px;
}

.login-mark {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: #17b8b3;
  color: #0d2536;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 800;
}

.login-product,
.login-title,
.login-heading p,
.login-heading h1,
.aside-top span,
.aside-top strong,
.power-grid p,
.power-grid strong {
  margin: 0;
}

.login-product {
  color: #0d2536;
  font-size: 30px;
  line-height: 1;
  font-weight: 800;
}

.login-title {
  margin-top: 6px;
  color: #667d8b;
  font-size: 14px;
}

.login-heading {
  margin-bottom: 28px;
}

.login-heading p {
  color: #1687a7;
  font-size: 13px;
  font-weight: 700;
}

.login-heading h1 {
  margin-top: 8px;
  color: #0d2536;
  font-size: 28px;
  font-weight: 700;
}

.login-form {
  width: 100%;
}

.login-form ::v-deep .el-form-item {
  margin-bottom: 18px;
}

.login-form ::v-deep .el-input__inner {
  height: 46px;
  border-radius: 6px;
  border-color: #cfdce5;
  color: #1f2933;
  font-size: 15px;
}

.login-form ::v-deep .el-input__inner:focus {
  border-color: #128bdc;
}

.login-options {
  height: 28px;
  margin: 0 0 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #6b7f8c;
  font-size: 13px;
}

.login-button {
  width: 100%;
  height: 46px;
  border-radius: 6px;
  background: #128bdc;
  border-color: #128bdc;
  font-size: 16px;
  font-weight: 700;
}

.login-aside {
  min-width: 0;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background:
    linear-gradient(135deg, rgba(19, 184, 179, 0.18), rgba(18, 139, 220, 0.08)),
    #0d2536;
  color: #ffffff;
}

.aside-top {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.aside-top span {
  color: #8ecfdf;
  font-size: 12px;
  font-weight: 700;
}

.aside-top strong {
  max-width: 420px;
  color: #ffffff;
  font-size: 34px;
  line-height: 1.25;
}

.power-board {
  padding: 24px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.power-row {
  height: 56px;
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 14px;
  align-items: center;
}

.power-row span,
.power-row i {
  display: block;
  height: 10px;
  border-radius: 999px;
}

.power-row span {
  background: rgba(142, 207, 223, 0.38);
}

.power-row i {
  background: linear-gradient(90deg, #17b8b3, #4fa3ff);
}

.power-row.short {
  grid-template-columns: 128px 1fr;
  opacity: 0.8;
}

.power-grid {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.power-grid > div {
  min-height: 84px;
  padding: 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.power-grid p {
  color: #a7c6d4;
  font-size: 13px;
}

.power-grid strong {
  display: block;
  margin-top: 10px;
  color: #ffffff;
  font-size: 22px;
}

@media (max-width: 900px) {
  .login-page {
    padding: 0;
  }

  .login-shell {
    min-height: 100vh;
    grid-template-columns: 1fr;
    border: 0;
    border-radius: 0;
  }

  .login-main {
    padding: 48px 28px;
  }

  .login-aside {
    display: none;
  }
}
</style>
