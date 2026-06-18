import Vue from "vue";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import moment from "moment";
import App from "./App.vue";

Vue.config.productionTip = false;
Vue.use(ElementUI);

window.moment = moment;
Vue.prototype.$moment = moment;

const params = new URLSearchParams(window.location.search);
["entId", "cid", "ticket", "systemCode", "openId"].forEach(key => {
  const value = params.get(key);
  if (value) {
    sessionStorage.setItem(key, value);
  }
});

new Vue({
  render: h => h(App),
}).$mount("#app");
