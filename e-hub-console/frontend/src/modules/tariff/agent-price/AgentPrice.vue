<template>
  <div class="agent-price-page">
    <section class="agent-price-panel">
      <div class="query-section">
        <div class="mode-tabs" role="tablist">
          <button
            type="button"
            :class="{ active: queryMode === 'month' }"
            @click="switchMode('month')"
          >
            月份查询
          </button>
          <button
            type="button"
            :class="{ active: queryMode === 'date' }"
            @click="switchMode('date')"
          >
            日期查询
          </button>
        </div>

        <div class="filters">
          <label class="field">
            <span>{{ queryMode === 'month' ? '电费月份' : '电费日期' }}</span>
            <el-date-picker
              v-if="queryMode === 'month'"
              v-model="form.yearMonth"
              type="month"
              value-format="yyyy-MM"
              format="yyyy/MM"
              size="small"
              :clearable="false"
              @change="handleDateChange"
            />
            <el-date-picker
              v-else
              v-model="form.selectedDate"
              type="date"
              value-format="yyyy-MM-dd"
              format="yyyy/MM/dd"
              size="small"
              :clearable="false"
              @change="handleDateChange"
            />
          </label>

          <label class="field">
            <span>省份</span>
            <el-select
              v-model="form.provinceCode"
              filterable
              size="small"
              placeholder="请选择"
              @change="handleProvinceChange"
            >
              <el-option
                v-for="item in provinceOptions"
                :key="item.provinceCode"
                :label="item.provinceName"
                :value="item.provinceCode"
              />
            </el-select>
          </label>

          <label class="field">
            <span>二级分类</span>
            <el-select
              v-model="form.secondType"
              filterable
              size="small"
              placeholder="请选择"
              @change="handleSecondTypeChange"
            >
              <el-option
                v-for="item in secondTypeOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </label>

          <label class="field">
            <span>三级分类</span>
            <el-select
              v-model="form.thirdType"
              filterable
              size="small"
              placeholder="请选择"
              @change="handleThirdTypeChange"
            >
              <el-option
                v-for="item in thirdTypeOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </label>

          <label class="field">
            <span>企业用电类别</span>
            <el-select v-model="form.userType" filterable size="small" placeholder="请选择" @change="handleUserTypeChange">
              <el-option v-for="item in userTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>

          <label class="field">
            <span>电压属性</span>
            <el-select v-model="form.sfType" filterable size="small" placeholder="请选择" @change="handleSfTypeChange">
              <el-option v-for="item in sfTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>

          <label class="field">
            <span>企业用电压等级</span>
            <el-select v-model="form.dyLevel" filterable size="small" placeholder="请选择" @change="queryPrices">
              <el-option v-for="item in dyLevelOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </label>
        </div>
      </div>

      <div class="content-grid" v-loading="loading">
        <section class="price-card">
          <div class="card-heading">
            <h3>分时段电度价格</h3>
            <span>单位：元/千瓦时</span>
          </div>
          <div v-if="hasPeriodData" class="period-list">
            <div
              v-for="item in periodRows"
              :key="item.key"
              class="period-row"
              :style="{ '--period-color': item.color }"
            >
              <span class="period-badge">{{ item.shortName }}</span>
              <div class="period-main">
                <strong>{{ item.name }}时电价 {{ fmtPrice(item.data.ddPrice) }}</strong>
                <p v-if="item.timeText">{{ item.timeText }}</p>
              </div>
              <div class="period-breakdown">
                <div>
                  <span>代理购电价</span>
                  <strong>{{ fmtPrice(item.data.dlPrice) }}</strong>
                </div>
                <div>
                  <span>电价附加</span>
                  <strong>{{ fmtPrice(item.data.fjPrice) }}</strong>
                </div>
                <div>
                  <span>系统运行费用</span>
                  <strong>{{ fmtPrice(item.data.spPrice) }}</strong>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">暂无该条件电价数据</div>
        </section>

        <section class="chart-card">
          <div class="card-heading">
            <h3>分时段电度价格分布</h3>
            <span>单位：元/千瓦时</span>
          </div>
          <template v-if="hasPeriodData">
            <div class="bar-chart">
              <div class="bars">
                <div
                  v-for="bar in chartBars"
                  :key="bar.hour"
                  class="bar-column"
                  :title="`${bar.label} ${fmtPrice(bar.price)}`"
                >
                  <i
                    :style="{
                      height: `${bar.height}%`,
                      background: bar.color,
                      opacity: bar.price > 0 ? 1 : 0.18,
                    }"
                  />
                </div>
              </div>
              <div class="axis">
                <span>0:00</span>
                <span>4:00</span>
                <span>8:00</span>
                <span>12:00</span>
                <span>16:00</span>
                <span>20:00</span>
                <span>24:00</span>
              </div>
            </div>
            <div class="legend">
              <span v-for="item in periodRows" :key="item.key">
                <i :style="{ background: item.color }" />
                {{ item.name }}时电价&nbsp;&nbsp;{{ fmtPrice(item.data.ddPrice) }}
              </span>
            </div>
          </template>
          <div v-else class="empty-state chart-empty">暂无分时价格分布</div>
        </section>
      </div>
    </section>
  </div>
</template>

<script>
import moment from "moment";
import { getAgentPriceOptions, queryAgentPrices } from "./api/index.js";

const PERIOD_META = [
  { key: "jian", name: "尖", shortName: "尖", color: "#ef4444" },
  { key: "feng", name: "峰", shortName: "峰", color: "#f59e0b" },
  { key: "ping", name: "平", shortName: "平", color: "#14b8a6" },
  { key: "gu", name: "谷", shortName: "谷", color: "#3b82f6" },
  { key: "shengu", name: "深谷", shortName: "深", color: "#64748b" },
];

function emptyPeriod() {
  return { ddPrice: "0", dlPrice: "0", spPrice: "0", fjPrice: "0", times: [] };
}

export default {
  name: "AgentPrice",
  data() {
    const today = moment().format("YYYY-MM-DD");
    return {
      queryMode: "date",
      loading: false,
      areaOptions: [],
      userTypeOptions: [],
      sfTypeOptions: [],
      dyLevelOptions: [],
      priceData: {},
      form: {
        yearMonth: moment(today).format("YYYY-MM"),
        selectedDate: today,
        provinceCode: "",
        provinceName: "",
        secondType: "",
        thirdType: "",
        userType: "",
        sfType: "",
        dyLevel: "",
      },
    };
  },
  computed: {
    provinceOptions() {
      const seen = new Set();
      return this.areaOptions
        .filter(item => item && item.provinceCode)
        .filter(item => {
          if (seen.has(item.provinceCode)) return false;
          seen.add(item.provinceCode);
          return true;
        })
        .map(item => ({
          provinceCode: item.provinceCode,
          provinceName: item.provinceName || item.provinceCode,
        }));
    },
    secondTypeOptions() {
      if (!this.form.provinceCode) return [];
      const seen = new Set();
      return this.areaOptions
        .filter(item => item.provinceCode === this.form.provinceCode && item.secondType)
        .map(item => item.secondType)
        .filter(item => {
          if (seen.has(item)) return false;
          seen.add(item);
          return true;
        });
    },
    thirdTypeOptions() {
      if (!this.form.provinceCode || !this.form.secondType) return [];
      const seen = new Set();
      return this.areaOptions
        .filter(item => item.provinceCode === this.form.provinceCode
          && item.secondType === this.form.secondType
          && item.thirdType)
        .map(item => item.thirdType)
        .filter(item => {
          if (seen.has(item)) return false;
          seen.add(item);
          return true;
        });
    },
    periodRows() {
      return PERIOD_META.map(meta => {
        const data = this.priceData[meta.key] || emptyPeriod();
        return {
          ...meta,
          data,
          timeText: Array.isArray(data.times) ? data.times.join("  ") : "",
        };
      });
    },
    chartBars() {
      const maxPrice = Math.max(...this.periodRows.map(item => Number(item.data.ddPrice) || 0), 0.0001);
      return Array.from({ length: 24 }).map((_, hour) => {
        const period = this.findPeriodByHour(hour);
        const price = Number(period.data.ddPrice) || 0;
        return {
          hour,
          label: `${hour}:00`,
          price,
          color: period.color,
          height: Math.max(18, Math.round((price / maxPrice) * 90)),
        };
      });
    },
    hasPeriodData() {
      return this.periodRows.some(item => Array.isArray(item.data.times) && item.data.times.length > 0);
    },
  },
  created() {
    this.loadOptions(true);
  },
  methods: {
    switchMode(mode) {
      if (this.queryMode === mode) return;
      this.queryMode = mode;
      this.loadOptions(true);
    },
    handleDateChange() {
      this.loadOptions(true);
    },
    async loadOptions(resetArea) {
      const params = this.buildOptionParams();
      const res = await getAgentPriceOptions(params);
      const body = res.data || {};
      const data = body.code === 200 && body.data ? body.data : {};
      this.applyOptionsData(data);
      this.syncAreaSelection(resetArea);
      if (this.form.provinceCode && this.form.secondType && this.form.thirdType) {
        await this.reloadScopedOptions();
      }
      await this.ensureSelectedOptions();
      this.queryPrices();
    },
    handleProvinceChange(value) {
      this.applyProvince(value);
      this.form.secondType = "";
      this.form.thirdType = "";
      this.form.userType = "";
      this.form.sfType = "";
      this.form.dyLevel = "";
      this.loadOptions(false);
    },
    handleSecondTypeChange() {
      this.form.thirdType = "";
      this.form.userType = "";
      this.form.sfType = "";
      this.form.dyLevel = "";
      this.loadOptions(false);
    },
    handleThirdTypeChange() {
      this.form.userType = "";
      this.form.sfType = "";
      this.form.dyLevel = "";
      this.loadOptions(false);
    },
    handleUserTypeChange() {
      this.form.sfType = "";
      this.form.dyLevel = "";
      this.loadOptions(false);
    },
    handleSfTypeChange() {
      this.form.dyLevel = "";
      this.loadOptions(false);
    },
    applyProvince(provinceCode) {
      const province = this.provinceOptions.find(item => item.provinceCode === provinceCode);
      this.form.provinceCode = province ? province.provinceCode : "";
      this.form.provinceName = province ? province.provinceName : "";
    },
    clearArea() {
      this.form.provinceCode = "";
      this.form.provinceName = "";
      this.form.secondType = "";
      this.form.thirdType = "";
      this.form.userType = "";
      this.form.sfType = "";
      this.form.dyLevel = "";
    },
    async reloadScopedOptions() {
      const res = await getAgentPriceOptions(this.buildOptionParams());
      const body = res.data || {};
      this.applyOptionsData(body.code === 200 && body.data ? body.data : {});
    },
    applyOptionsData(data) {
      this.areaOptions = Array.isArray(data.areas) ? data.areas : [];
      this.userTypeOptions = Array.isArray(data.userTypes) ? data.userTypes : [];
      this.sfTypeOptions = Array.isArray(data.sfTypes) ? data.sfTypes : [];
      this.dyLevelOptions = Array.isArray(data.dyLevels) ? data.dyLevels : [];
    },
    syncAreaSelection(resetArea) {
      if (!this.areaOptions.length) {
        this.clearArea();
        return;
      }

      const provinceCode = resetArea
        ? this.pickOption("", this.provinceOptions.map(item => item.provinceCode))
        : this.pickOption(this.form.provinceCode, this.provinceOptions.map(item => item.provinceCode));
      this.applyProvince(provinceCode);

      const secondType = this.pickOption(
        resetArea ? "" : this.form.secondType,
        this.secondTypeOptions
      );
      this.form.secondType = secondType;

      const thirdType = this.pickOption(
        resetArea ? "" : this.form.thirdType,
        this.thirdTypeOptions
      );
      this.form.thirdType = thirdType;
    },
    async ensureSelectedOptions() {
      const selectedUserType = this.pickOption(this.form.userType, this.userTypeOptions);
      this.form.userType = selectedUserType;
      if (!selectedUserType) {
        this.form.sfType = "";
        this.form.dyLevel = "";
        return;
      }

      const originalSfType = this.form.sfType;
      const originalDyLevel = this.form.dyLevel;
      this.form.sfType = "";
      this.form.dyLevel = "";
      await this.reloadScopedOptions();
      const selectedSfType = this.pickOption(originalSfType, this.sfTypeOptions);
      this.form.sfType = selectedSfType;
      if (!selectedSfType) {
        this.form.dyLevel = "";
        return;
      }

      this.form.dyLevel = "";
      await this.reloadScopedOptions();
      this.form.dyLevel = this.pickOption(originalDyLevel, this.dyLevelOptions);
    },
    pickOption(value, options) {
      if (Array.isArray(options) && options.includes(value)) {
        return value;
      }
      return Array.isArray(options) && options.length ? options[0] : "";
    },
    async queryPrices() {
      if (!this.canQuery()) {
        this.priceData = {};
        return;
      }
      this.loading = true;
      try {
        const res = await queryAgentPrices(this.buildQueryPayload());
        const body = res.data || {};
        this.priceData = body.code === 200 && body.data ? body.data : {};
      } catch (error) {
        this.priceData = {};
        const msg = error?.response?.data?.msg || error.message || "查询失败";
        this.$message.error(msg);
      } finally {
        this.loading = false;
      }
    },
    canQuery() {
      return this.form.provinceCode
        && this.form.secondType
        && this.form.thirdType
        && this.form.userType
        && this.form.sfType
        && this.form.dyLevel
        && (this.queryMode === "date" ? this.form.selectedDate : this.form.yearMonth);
    },
    buildOptionParams() {
      return {
        yearMonth: this.queryMode === "month" ? this.form.yearMonth : undefined,
        selectedDate: this.queryMode === "date" ? this.form.selectedDate : undefined,
        provinceCode: this.form.provinceCode || undefined,
        secondType: this.form.secondType || undefined,
        thirdType: this.form.thirdType || undefined,
        userType: this.form.userType || undefined,
        sfType: this.form.sfType || undefined,
      };
    },
    buildQueryPayload() {
      return {
        provinceCode: this.form.provinceCode,
        provinceName: this.form.provinceName,
        secondType: this.form.secondType,
        thirdType: this.form.thirdType,
        yearMonth: this.queryMode === "month" ? this.form.yearMonth : undefined,
        selectedDate: this.queryMode === "date" ? this.form.selectedDate : undefined,
        userType: this.form.userType,
        sfType: this.form.sfType,
        dyLevel: this.form.dyLevel,
      };
    },
    findPeriodByHour(hour) {
      const minute = hour * 60;
      for (const item of this.periodRows) {
        const ranges = Array.isArray(item.data.times) ? item.data.times : [];
        if (ranges.some(range => this.minuteInRange(minute, range))) {
          return item;
        }
      }
      return this.periodRows.find(item => Number(item.data.ddPrice) > 0) || this.periodRows[0];
    },
    minuteInRange(minute, range) {
      const parts = String(range || "").split(",");
      if (parts.length !== 2) return false;
      const start = this.timeToMinute(parts[0]);
      const end = this.timeToMinute(parts[1]);
      if (start == null || end == null) return false;
      return minute >= start && minute < end;
    },
    timeToMinute(value) {
      const parts = String(value || "").split(":");
      if (parts.length !== 2) return null;
      return Number(parts[0]) * 60 + Number(parts[1]);
    },
    fmtPrice(value) {
      const num = Number(value);
      if (!Number.isFinite(num)) return "0.0000";
      return num.toFixed(4);
    },
  },
};
</script>

<style scoped lang="less">
.agent-price-page {
  width: 100%;
  min-height: 100%;
  color: #0e2638;
}

.agent-price-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 768px;
}

.query-section {
  padding: 16px;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
}

.mode-tabs {
  display: inline-flex;
  gap: 8px;
  margin-bottom: 16px;

  button {
    height: 30px;
    padding: 0 14px;
    border: 1px solid #dde6ed;
    border-radius: 6px;
    background: #ffffff;
    color: #456577;
    font-size: 13px;
    cursor: pointer;
  }

  button.active {
    border-color: #0780ed;
    background: #0780ed;
    color: #ffffff;
    box-shadow: none;
  }
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.field {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;

  span {
    color: #607d8f;
    font-size: 13px;
    font-weight: 600;
  }

  ::v-deep .el-select,
  ::v-deep .el-date-editor {
    width: 100%;
  }
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(420px, 1fr);
  gap: 16px;
}

.price-card,
.chart-card {
  min-width: 0;
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
}

.card-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;

  h3 {
    margin: 0;
    padding-left: 10px;
    border-left: 3px solid #0780ed;
    color: #0e2638;
    font-size: 16px;
    font-weight: 700;
  }

  span {
    color: #607d8f;
    font-size: 13px;
  }
}

.period-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.period-row {
  min-height: 78px;
  display: grid;
  grid-template-columns: 54px minmax(130px, 1fr) minmax(230px, 1.25fr);
  align-items: center;
  gap: 12px;
  border: 1px solid #ecf1f6;
  border-left: 4px solid var(--period-color);
  border-radius: 8px;
  padding: 12px 14px;
  background: #fbfdff;
}

.period-badge {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--period-color);
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
}

.period-main {
  strong {
    display: block;
    color: #0e2638;
    font-size: 15px;
    line-height: 1.4;
  }

  p {
    margin: 4px 0 0;
    color: #607d8f;
    font-size: 12px;
    line-height: 1.4;
  }
}

.period-breakdown {
  display: grid;
  grid-template-columns: repeat(3, minmax(70px, 1fr));
  gap: 10px;

  div {
    min-width: 0;
  }

  span {
    display: block;
    color: #607d8f;
    font-size: 12px;
    line-height: 1.4;
  }

  strong {
    display: block;
    margin-top: 2px;
    color: #0e2638;
    font-size: 13px;
    line-height: 1.4;
  }
}

.chart-card {
  display: flex;
  flex-direction: column;
}

.bar-chart {
  flex: 1;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 18px 10px 4px;
}

.bars {
  height: 230px;
  display: grid;
  grid-template-columns: repeat(24, 1fr);
  align-items: end;
  gap: 5px;
}

.bar-column {
  height: 100%;
  display: flex;
  align-items: flex-end;

  i {
    width: 100%;
    min-height: 16px;
    border-radius: 8px 8px 0 0;
    display: block;
    transition: height 0.2s ease;
  }
}

.axis {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-top: 10px;
  color: #8aa0b8;
  font-size: 12px;

  span {
    text-align: center;
  }
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 18px;
  margin-top: 18px;
  color: #60738c;
  font-size: 13px;

  span {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  i {
    width: 9px;
    height: 9px;
    border-radius: 50%;
  }
}

.empty-state {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #cfdce5;
  border-radius: 8px;
  background: #fbfdff;
  color: #8aa0b8;
  font-size: 14px;
}

.chart-empty {
  flex: 1;
}

@media (max-width: 1280px) {
  .filters {
    grid-template-columns: repeat(3, minmax(160px, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .query-section,
  .content-grid {
    padding-left: 12px;
    padding-right: 12px;
  }

  .filters,
  .period-row {
    grid-template-columns: 1fr;
  }

  .period-breakdown {
    grid-template-columns: 1fr;
  }
}
</style>
