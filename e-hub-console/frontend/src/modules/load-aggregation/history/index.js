import Vue from "vue";

const components = [];
const contexts = require.context("./src", false, /\.vue$/);
contexts.keys().forEach(component => {
  const componentEntity = contexts(component).default;
  components.push(componentEntity);
});

const dashboardPlugin = {
  install(Vue) {
    components.forEach(com => {
      Vue.component(com.name, com);
    });
  },
};
Vue.use(dashboardPlugin);
