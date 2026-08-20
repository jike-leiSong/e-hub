<template>
  <div class="tariff-import-page">
    <section class="import-panel scope-panel">
      <div class="panel-head">
        <div>
          <h2>发布范围</h2>
          <p>{{ previewVersionText }}</p>
        </div>
        <div class="panel-actions">
          <el-button size="small" icon="el-icon-document-copy" @click="openCopyDialog">复制历史</el-button>
          <el-button size="small" type="primary" icon="el-icon-view" :loading="previewLoading" @click="handlePreview">
            预览
          </el-button>
          <el-button size="small" type="success" icon="el-icon-upload2" :loading="publishLoading" @click="handlePublish">
            发布
          </el-button>
          <el-button size="small" type="danger" icon="el-icon-delete" :loading="deleteLoading" @click="handleDelete">
            删除
          </el-button>
        </div>
      </div>

      <div class="scope-form">
        <div class="scope-row scope-row-main">
          <label class="field effective-field">
            <span>生效类型</span>
            <el-radio-group v-model="form.effectiveType" size="small" @change="handleEffectiveScopeChange">
              <el-radio-button label="MONTH">月度默认</el-radio-button>
              <el-radio-button label="DAY">单日特殊</el-radio-button>
              <el-radio-button label="RANGE">节假日区间</el-radio-button>
            </el-radio-group>
          </label>

          <label v-if="form.effectiveType === 'MONTH'" class="field time-field">
            <span>生效时间</span>
            <el-date-picker
              v-model="form.yearMonth"
              type="month"
              value-format="yyyy-MM"
              format="yyyy/MM"
              size="small"
              :clearable="false"
              @change="handleEffectiveScopeChange"
            />
          </label>

          <label v-if="form.effectiveType === 'DAY'" class="field time-field">
            <span>生效时间</span>
            <el-date-picker
              v-model="form.selectedDate"
              type="date"
              value-format="yyyy-MM-dd"
              format="yyyy/MM/dd"
              size="small"
              :clearable="false"
              @change="handleEffectiveScopeChange"
            />
          </label>

          <div v-if="form.effectiveType === 'RANGE'" class="field time-field range-time-field">
            <span>生效时间</span>
            <div class="date-pair">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                value-format="yyyy-MM-dd"
                format="yyyy/MM/dd"
                size="small"
                :clearable="false"
                placeholder="开始日期"
                @change="handleStartDateChange"
              />
              <el-date-picker
                v-model="form.endDate"
                type="date"
                value-format="yyyy-MM-dd"
                format="yyyy/MM/dd"
                size="small"
                :clearable="false"
                placeholder="结束日期"
                @change="handleEndDateChange"
              />
            </div>
          </div>

          <label class="field area-field">
            <span>省份/区域</span>
            <el-cascader
              v-model="areaPath"
              :options="areaCascaderOptions"
              :props="areaCascaderProps"
              filterable
              size="small"
              placeholder="请选择省份/区域"
              @change="handleAreaChange"
            />
          </label>
        </div>
      </div>
    </section>

    <div class="import-layout">
      <section class="import-panel periods-panel">
        <div class="panel-head compact">
          <div>
            <h2>时段规则</h2>
            <p>{{ slotPeriodSummaryRows.length }} 组，{{ assignedSlotCount }}/96 点</p>
          </div>
          <div class="panel-actions">
            <el-button size="small" icon="el-icon-refresh" @click="resetDefaultPeriods">默认模板</el-button>
            <el-button size="small" icon="el-icon-delete" @click="clearPeriods">清空</el-button>
          </div>
        </div>

        <div class="period-toolbar">
          <button
            v-for="item in periodOptions"
            :key="item.type"
            type="button"
            class="period-tool"
            :class="{ active: activePeriodType === item.type }"
            :style="{ '--period-color': item.color }"
            @click="activePeriodType = item.type"
          >
            <i />
            {{ item.type }}
          </button>
        </div>

        <div class="range-fillbar">
          <label class="range-fill-field">
            <span>开始</span>
            <el-select v-model="rangeFill.start" size="small" filterable>
              <el-option v-for="item in timeOptions" :key="`start-${item.value}`" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <label class="range-fill-field">
            <span>结束</span>
            <el-select v-model="rangeFill.end" size="small" filterable>
              <el-option v-for="item in timeOptions" :key="`end-${item.value}`" :label="item.label" :value="item.value" />
            </el-select>
          </label>
          <el-button size="small" type="primary" :disabled="rangeFillInvalid" @click="fillSelectedRange">
            填充范围
          </el-button>
          <el-button size="small" @click="fillMissingSlots">补齐未覆盖</el-button>
        </div>

        <div class="slot-grid" @mouseleave="stopPaint" @mouseup="stopPaint">
          <button
            v-for="slot in timeSlotViews"
            :key="slot.index"
            type="button"
            class="slot-cell"
            :class="{ empty: !slot.periodType }"
            :style="{ background: slot.color }"
            :title="`${slot.time} ${slot.periodType || '未设置'}`"
            @mousedown.prevent="paintSlot(slot.index)"
            @mouseenter="paintSlotWhileDrag(slot.index)"
          >
            <span v-if="slot.showHour">{{ slot.hourLabel }}</span>
          </button>
        </div>
        <div class="timeline-axis">
          <span>00:00</span>
          <span>04:00</span>
          <span>08:00</span>
          <span>12:00</span>
          <span>16:00</span>
          <span>20:00</span>
          <span>24:00</span>
        </div>

        <div class="validation-line" :class="{ invalid: !periodValidation.valid }">
          {{ periodValidation.message }}
        </div>

        <el-table :data="slotPeriodSummaryRows" size="small" class="editable-table period-summary-table">
          <el-table-column prop="periodType" label="时段" width="90">
            <template slot-scope="{ row }">
              <span class="period-badge" :style="{ background: row.color }">{{ row.periodType }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="rangesText" label="时间范围" />
          <el-table-column prop="pointCount" label="点数" width="72" />
        </el-table>
      </section>

      <section class="import-panel price-panel">
        <div class="panel-head compact">
          <div>
            <h2>价格行</h2>
            <p>{{ form.priceRows.length }} 行</p>
          </div>
          <div class="panel-actions">
            <el-button size="small" icon="el-icon-plus" @click="addPriceRow">添加价格行</el-button>
          </div>
        </div>

        <div class="price-row-list">
          <div v-for="(row, index) in form.priceRows" :key="row._key" class="price-row-card">
            <div class="price-row-head">
              <div class="price-row-title">
                <strong>价格行 {{ index + 1 }}</strong>
                <span>{{ row.userType || "用电类别" }} / {{ row.dyLevel || "电压等级" }} / {{ row.sfType || "收费类型" }}</span>
              </div>
              <div class="price-row-actions">
                <el-button type="text" icon="el-icon-document-copy" @click="duplicatePriceRow(row)">复制</el-button>
                <el-button type="text" icon="el-icon-delete" class="danger-text" @click="removePriceRow(index)">删除</el-button>
              </div>
            </div>

            <div class="price-base-grid">
              <label class="field">
                <span>用电类别</span>
                <el-select v-model="row.userType" filterable size="small" placeholder="请选择" @change="clearPreview">
                  <el-option v-for="item in userTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </label>
              <label class="field">
                <span>电压等级</span>
                <el-select v-model="row.dyLevel" filterable size="small" placeholder="请选择" @change="clearPreview">
                  <el-option v-for="item in dyLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </label>
              <label class="field">
                <span>收费类型</span>
                <el-select v-model="row.sfType" filterable size="small" placeholder="请选择" @change="clearPreview">
                  <el-option v-for="item in sfTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </label>
            </div>

            <div class="price-value-grid">
              <label v-for="field in pricePeriodFields" :key="field.key" class="field price-number-field">
                <span>{{ field.label }}</span>
                <el-input
                  v-model.trim="row[field.key]"
                  type="number"
                  size="small"
                  :placeholder="field.placeholder"
                  @input="clearPreview"
                />
              </label>
              <label v-for="field in priceExtraFields" :key="field.key" class="field price-number-field">
                <span>{{ field.label }}</span>
                <el-input
                  v-model.trim="row[field.key]"
                  type="number"
                  size="small"
                  :placeholder="field.placeholder"
                  @input="clearPreview"
                />
              </label>
            </div>
          </div>
        </div>
      </section>
    </div>

    <section class="import-panel preview-panel" v-loading="previewLoading || publishLoading">
      <div class="panel-head compact">
        <div>
          <h2>发布预览</h2>
          <p>{{ previewSummaryText }}</p>
        </div>
      </div>

      <div v-if="previewData" class="preview-content">
        <div class="stats-row">
          <div>
            <span>版本</span>
            <strong>{{ previewData.versions.join(" / ") }}</strong>
          </div>
          <div>
            <span>时段点</span>
            <strong>{{ previewData.fpgjPointCount }}</strong>
          </div>
          <div>
            <span>价格行</span>
            <strong>{{ previewData.priceRowCount }}</strong>
          </div>
          <div>
            <span>价格点</span>
            <strong>{{ previewData.pricePointCount }}</strong>
          </div>
        </div>

        <div class="timeline-strip">
          <span
            v-for="point in timelinePoints"
            :key="point.bizTime"
            :style="{ background: point.color }"
            :title="`${point.bizTime} ${point.periodType}`"
          />
        </div>
        <div class="timeline-axis">
          <span>00:00</span>
          <span>04:00</span>
          <span>08:00</span>
          <span>12:00</span>
          <span>16:00</span>
          <span>20:00</span>
          <span>24:00</span>
        </div>

        <div class="preview-grid">
          <el-table :data="periodSummaryRows" size="small" height="260">
            <el-table-column prop="periodType" label="时段" width="90">
              <template slot-scope="{ row }">
                <span class="period-badge" :style="{ background: row.color }">{{ row.periodType }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="rangesText" label="时间范围" />
            <el-table-column prop="pointCount" label="点数" width="80" />
          </el-table>

          <el-table :data="previewData.priceRows" size="small" height="260">
            <el-table-column prop="userType" label="用电类别" min-width="120" />
            <el-table-column prop="dyLevel" label="电压等级" min-width="120" />
            <el-table-column prop="sfType" label="收费类型" width="96" />
            <el-table-column label="峰平谷价格" min-width="220">
              <template slot-scope="{ row }">
                <span class="price-tags">
                  <em v-if="row.jianPrice != null">尖 {{ formatPrice(row.jianPrice) }}</em>
                  <em v-if="row.fengPrice != null">峰 {{ formatPrice(row.fengPrice) }}</em>
                  <em v-if="row.pingPrice != null">平 {{ formatPrice(row.pingPrice) }}</em>
                  <em v-if="row.guPrice != null">谷 {{ formatPrice(row.guPrice) }}</em>
                  <em v-if="row.shenguPrice != null">深 {{ formatPrice(row.shenguPrice) }}</em>
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div v-else class="empty-state">暂无预览数据</div>
    </section>

    <el-dialog title="复制历史模板" :visible.sync="copyDialogVisible" width="560px">
      <div class="copy-form">
        <label class="field">
          <span>来源版本</span>
          <el-input v-model="copyForm.sourceVersion" size="small" placeholder="2607 或 2026-07" />
        </label>
        <label class="field">
          <span>来源省份</span>
          <el-select
            v-model="copyForm.sourceProvinceCode"
            filterable
            allow-create
            default-first-option
            size="small"
            @change="handleCopyProvinceChange"
          >
            <el-option v-for="item in provinceOptions" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </label>
        <label class="field">
          <span>省份名称</span>
          <el-input v-model="copyForm.sourceProvinceName" size="small" />
        </label>
        <label class="field">
          <span>二级分类</span>
          <el-input v-model="copyForm.sourceSecondType" size="small" />
        </label>
        <label class="field">
          <span>三级分类</span>
          <el-input v-model="copyForm.sourceThirdType" size="small" />
        </label>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="copyDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="copyLoading" @click="handleCopy">复制</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import moment from "moment";
import {
  copyTariffRuleImport,
  deleteTariffRuleImport,
  getTariffAreaOptions,
  getTariffDictByType,
  previewTariffRuleImport,
  publishTariffRuleImport,
} from "./api/index.js";

const PERIOD_OPTIONS = [
  { type: "尖", shortName: "尖", color: "#dc2626" },
  { type: "峰", shortName: "峰", color: "#f59e0b" },
  { type: "平", shortName: "平", color: "#0f9f8f" },
  { type: "谷", shortName: "谷", color: "#2563eb" },
  { type: "深谷", shortName: "深", color: "#64748b" },
];

const DEFAULT_USER_TYPE_OPTIONS = ["工商业", "一般工商业", "大工业"];
const DEFAULT_DY_LEVEL_OPTIONS = ["不满1千伏", "1-10千伏", "35千伏", "110千伏", "220千伏及以上"];
const DEFAULT_SF_TYPE_OPTIONS = ["单一制", "两部制"];
const PRICE_PERIOD_FIELDS = [
  { key: "jianPrice", label: "尖", placeholder: "元/kWh" },
  { key: "fengPrice", label: "峰", placeholder: "元/kWh" },
  { key: "pingPrice", label: "平", placeholder: "元/kWh" },
  { key: "guPrice", label: "谷", placeholder: "元/kWh" },
  { key: "shenguPrice", label: "深谷", placeholder: "元/kWh" },
];
const PRICE_EXTRA_FIELDS = [
  { key: "demandElectricityPrice", label: "需量", placeholder: "元/kW·月" },
  { key: "capacityElectricityPrice", label: "容量", placeholder: "元/kVA·月" },
];

const PROVINCE_OPTIONS = [
  { code: "110000000000", name: "北京市" },
  { code: "120000000000", name: "天津市" },
  { code: "130000000000", name: "河北省" },
  { code: "140000000000", name: "山西省" },
  { code: "150000000000", name: "内蒙古自治区" },
  { code: "210000000000", name: "辽宁省" },
  { code: "220000000000", name: "吉林省" },
  { code: "230000000000", name: "黑龙江省" },
  { code: "310000000000", name: "上海市" },
  { code: "320000000000", name: "江苏省" },
  { code: "330000000000", name: "浙江省" },
  { code: "340000000000", name: "安徽省" },
  { code: "350000000000", name: "福建省" },
  { code: "360000000000", name: "江西省" },
  { code: "370000000000", name: "山东省" },
  { code: "410000000000", name: "河南省" },
  { code: "420000000000", name: "湖北省" },
  { code: "430000000000", name: "湖南省" },
  { code: "440000000000", name: "广东省" },
  { code: "450000000000", name: "广西壮族自治区" },
  { code: "460000000000", name: "海南省" },
  { code: "500000000000", name: "重庆市" },
  { code: "510000000000", name: "四川省" },
  { code: "520000000000", name: "贵州省" },
  { code: "530000000000", name: "云南省" },
  { code: "610000000000", name: "陕西省" },
  { code: "620000000000", name: "甘肃省" },
  { code: "630000000000", name: "青海省" },
  { code: "640000000000", name: "宁夏回族自治区" },
  { code: "650000000000", name: "新疆维吾尔自治区" },
];

const AREA_PRESET_OPTIONS = [
  { provinceCode: "150000000000", secondType: "东部地区", thirdType: "不限" },
  { provinceCode: "150000000000", secondType: "西部地区", thirdType: "不限" },
  { provinceCode: "440000000000", secondType: "珠三角五市", thirdType: "不限" },
  { provinceCode: "440000000000", secondType: "深圳市", thirdType: "不限" },
  { provinceCode: "440000000000", secondType: "惠州市", thirdType: "不限" },
  { provinceCode: "440000000000", secondType: "江门市", thirdType: "不限" },
  { provinceCode: "440000000000", secondType: "东西两翼地区", thirdType: "不限" },
  { provinceCode: "440000000000", secondType: "粤北山区", thirdType: "不限" },
];

let rowSeed = 0;

function rowKey(prefix) {
  rowSeed += 1;
  return `${prefix}-${Date.now()}-${rowSeed}`;
}

function defaultPeriods() {
  return [
    { _key: rowKey("period"), periodType: "谷", rangeText: "00:00-08:00" },
    { _key: rowKey("period"), periodType: "峰", rangeText: "08:00-11:00, 18:00-21:00" },
    { _key: rowKey("period"), periodType: "平", rangeText: "11:00-18:00, 21:00-24:00" },
  ];
}

function defaultPriceRow() {
  return {
    _key: rowKey("price"),
    userType: "工商业",
    dyLevel: "1-10千伏",
    sfType: "两部制",
    priceType: "电度",
    jianPrice: null,
    fengPrice: null,
    pingPrice: null,
    guPrice: null,
    shenguPrice: null,
    demandElectricityPrice: null,
    capacityElectricityPrice: null,
  };
}

function defaultForm() {
  const today = moment().format("YYYY-MM-DD");
  return {
    effectiveType: "MONTH",
    yearMonth: moment(today).format("YYYY-MM"),
    selectedDate: today,
    startDate: today,
    endDate: today,
    provinceCode: "330000000000",
    provinceName: "浙江省",
    secondType: "不限",
    thirdType: "不限",
    periods: defaultPeriods(),
    priceRows: [defaultPriceRow()],
  };
}

function defaultCopyForm(form) {
  return {
    sourceVersion: "",
    sourceProvinceCode: form.provinceCode,
    sourceProvinceName: form.provinceName,
    sourceSecondType: form.secondType,
    sourceThirdType: form.thirdType,
  };
}

function dictItems(values) {
  return values.map(value => ({ label: value, value }));
}

function defaultAreaPath(form) {
  return [
    form.provinceCode,
    form.secondType || "不限",
    form.thirdType || "不限",
  ];
}

function timeToIndex(value, allowEnd) {
  const text = String(value || "").trim();
  if (allowEnd && text === "24:00") {
    return 96;
  }
  if (!/^\d{2}:\d{2}$/.test(text)) {
    return null;
  }
  const [hour, minute] = text.split(":").map(Number);
  if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || minute % 15 !== 0) {
    return null;
  }
  return hour * 4 + minute / 15;
}

function splitRanges(period) {
  const result = [];
  if (period && Array.isArray(period.ranges)) {
    result.push(...period.ranges);
  }
  if (period && period.rangeText) {
    result.push(...String(period.rangeText).split(/[,，;；\n\r]+/));
  }
  return result.map(item => String(item || "").trim()).filter(Boolean);
}

function parseRangeToIndexes(range) {
  const normalized = String(range || "")
    .replace(/\s/g, "")
    .replace(/[~—－]/g, "-")
    .replace(/至|到/g, "-");
  const parts = normalized.split("-");
  if (parts.length !== 2) {
    return null;
  }
  const start = timeToIndex(parts[0], false);
  const end = timeToIndex(parts[1], true);
  if (start == null || end == null || start >= end) {
    return null;
  }
  return [start, end];
}

function buildSlotsFromPeriods(periods) {
  const slots = Array(96).fill("");
  if (!Array.isArray(periods)) {
    return slots;
  }
  periods.forEach(period => {
    if (!period || !period.periodType) {
      return;
    }
    splitRanges(period).forEach(range => {
      const indexes = parseRangeToIndexes(range);
      if (!indexes) {
        return;
      }
      for (let i = indexes[0]; i < indexes[1]; i += 1) {
        slots[i] = period.periodType;
      }
    });
  });
  return slots;
}

export default {
  name: "TariffRuleImport",
  data() {
    const form = defaultForm();
    return {
      form,
      timeSlots: buildSlotsFromPeriods(form.periods),
      activePeriodType: "平",
      rangeFill: {
        start: 32,
        end: 44,
      },
      painting: false,
      periodOptions: PERIOD_OPTIONS,
      pricePeriodFields: PRICE_PERIOD_FIELDS,
      priceExtraFields: PRICE_EXTRA_FIELDS,
      provinceOptions: PROVINCE_OPTIONS,
      areaOptions: [],
      areaPath: defaultAreaPath(form),
      areaCascaderProps: {
        emitPath: true,
        expandTrigger: "hover",
      },
      userTypeOptions: dictItems(DEFAULT_USER_TYPE_OPTIONS),
      dyLevelOptions: dictItems(DEFAULT_DY_LEVEL_OPTIONS),
      sfTypeOptions: dictItems(DEFAULT_SF_TYPE_OPTIONS),
      previewData: null,
      previewLoading: false,
      publishLoading: false,
      deleteLoading: false,
      copyLoading: false,
      copyDialogVisible: false,
      copyForm: defaultCopyForm(form),
    };
  },
  created() {
    this.loadDictOptions();
    this.loadAreaOptions();
  },
  mounted() {
    window.addEventListener("mouseup", this.stopPaint);
  },
  beforeDestroy() {
    window.removeEventListener("mouseup", this.stopPaint);
  },
  computed: {
    scopeAreaText() {
      const parts = [this.form.provinceName || this.form.provinceCode];
      if (this.form.secondType && this.form.secondType !== "不限") {
        parts.push(this.form.secondType);
      }
      if (this.form.thirdType && this.form.thirdType !== "不限") {
        parts.push(this.form.thirdType);
      }
      return parts.filter(Boolean).join(" / ");
    },
    previewVersionText() {
      if (this.previewData && Array.isArray(this.previewData.versions)) {
        return `${this.scopeAreaText} / ${this.previewData.versions.join(" / ")}`;
      }
      if (this.form.effectiveType === "MONTH") {
        return `${this.scopeAreaText} / ${this.form.yearMonth || ""}`;
      }
      if (this.form.effectiveType === "DAY") {
        return `${this.scopeAreaText} / ${this.form.selectedDate || ""}`;
      }
      return `${this.scopeAreaText} / ${this.form.startDate || ""} - ${this.form.endDate || ""}`;
    },
    previewSummaryText() {
      if (!this.previewData) {
        return "待生成";
      }
      return `${this.previewData.priceRowCount} 行价格，${this.previewData.pricePointCount} 个价格点`;
    },
    areaCascaderOptions() {
      const provinceMap = new Map();
      const ensureProvince = (code, name) => {
        const key = code || name;
        if (!key) {
          return null;
        }
        if (!provinceMap.has(key)) {
          provinceMap.set(key, {
            value: key,
            label: name || key,
            children: [],
          });
        }
        const province = provinceMap.get(key);
        if (name && province.label === key) {
          province.label = name;
        }
        return province;
      };
      const ensureSecond = (province, secondType) => {
        const value = secondType || "不限";
        let second = province.children.find(item => item.value === value);
        if (!second) {
          second = {
            value,
            label: value,
            children: [],
          };
          province.children.push(second);
        }
        return second;
      };
      const ensureThird = (provinceCode, provinceName, secondType, thirdType) => {
        const province = ensureProvince(provinceCode, provinceName);
        if (!province) {
          return;
        }
        const second = ensureSecond(province, secondType);
        const value = thirdType || "不限";
        if (!second.children.some(item => item.value === value)) {
          second.children.push({
            value,
            label: value,
          });
        }
      };

      this.provinceOptions.forEach(item => {
        ensureProvince(item.code, item.name);
      });
      AREA_PRESET_OPTIONS.forEach(item => {
        const province = this.provinceOptions.find(option => option.code === item.provinceCode);
        ensureThird(
          item.provinceCode,
          province ? province.name : item.provinceCode,
          item.secondType || "不限",
          item.thirdType || "不限"
        );
      });
      this.areaOptions.forEach(item => {
        ensureThird(
          item.provinceCode,
          item.provinceName || item.provinceCode,
          item.secondType || "不限",
          item.thirdType || "不限"
        );
      });
      ensureThird(
        this.form.provinceCode,
        this.form.provinceName || this.form.provinceCode,
        this.form.secondType || "不限",
        this.form.thirdType || "不限"
      );
      provinceMap.forEach(province => {
        if (!province.children.length) {
          const second = ensureSecond(province, "不限");
          second.children.push({ value: "不限", label: "不限" });
        }
      });
      return Array.from(provinceMap.values());
    },
    timelinePoints() {
      const points = this.previewData && Array.isArray(this.previewData.fpgjPoints)
        ? this.previewData.fpgjPoints
        : [];
      return points.map(point => ({
        ...point,
        color: this.periodColor(point.periodType),
      }));
    },
    timeSlotViews() {
      return this.timeSlots.map((periodType, index) => {
        const hour = Math.floor(index / 4);
        return {
          index,
          periodType,
          color: periodType ? this.periodColor(periodType) : "#d9e2ec",
          time: this.indexToTime(index),
          showHour: index % 4 === 0 && index % 16 === 0,
          hourLabel: `${String(hour).padStart(2, "0")}:00`,
        };
      });
    },
    timeOptions() {
      return Array.from({ length: 97 }, (_, index) => ({
        value: index,
        label: this.indexToTime(index),
      }));
    },
    rangeFillInvalid() {
      return Number(this.rangeFill.start) >= Number(this.rangeFill.end);
    },
    assignedSlotCount() {
      return this.timeSlots.filter(Boolean).length;
    },
    periodValidation() {
      const missing = this.timeSlots
        .map((periodType, index) => (periodType ? null : this.indexToTime(index)))
        .filter(Boolean);
      if (missing.length) {
        return {
          valid: false,
          message: `时段未覆盖完整，还缺 ${missing.length} 个 15 分钟点，首个缺口 ${missing[0]}`,
        };
      }
      const used = new Set(this.timeSlots);
      return {
        valid: true,
        message: `时段已覆盖完整，共 ${used.size} 类时段、96 个 15 分钟点`,
      };
    },
    slotPeriodSummaryRows() {
      return this.periodRowsFromSlots(this.timeSlots);
    },
    periodSummaryRows() {
      const points = this.previewData && Array.isArray(this.previewData.fpgjPoints)
        ? this.previewData.fpgjPoints
        : [];
      const grouped = {};
      PERIOD_OPTIONS.forEach(item => {
        grouped[item.type] = {
          periodType: item.type,
          color: item.color,
          ranges: [],
          pointCount: 0,
        };
      });
      let index = 0;
      while (index < points.length) {
        const point = points[index];
        const type = point.periodType;
        const start = index;
        while (index < points.length && points[index].periodType === type) {
          index += 1;
        }
        if (grouped[type]) {
          grouped[type].ranges.push(`${this.indexToTime(start)}-${this.indexToTime(index)}`);
          grouped[type].pointCount += index - start;
        }
      }
      return PERIOD_OPTIONS
        .map(item => grouped[item.type])
        .filter(item => item.pointCount > 0)
        .map(item => ({
          ...item,
          rangesText: item.ranges.join(", "),
        }));
    },
  },
  methods: {
    async loadDictOptions() {
      try {
        const res = await getTariffDictByType(["useElectricType", "dyLevel", "sfType"]);
        const body = res.data || {};
        const data = body.code === 200 && body.data ? body.data : {};
        this.userTypeOptions = this.normalizeDictItems(data.useElectricType || data.userType, DEFAULT_USER_TYPE_OPTIONS);
        this.dyLevelOptions = this.normalizeDictItems(data.dyLevel, DEFAULT_DY_LEVEL_OPTIONS);
        this.sfTypeOptions = this.normalizeDictItems(data.sfType || data.otherType, DEFAULT_SF_TYPE_OPTIONS);
      } catch (error) {
        this.userTypeOptions = dictItems(DEFAULT_USER_TYPE_OPTIONS);
        this.dyLevelOptions = dictItems(DEFAULT_DY_LEVEL_OPTIONS);
        this.sfTypeOptions = dictItems(DEFAULT_SF_TYPE_OPTIONS);
      }
      this.syncPriceRowSelections();
    },
    async loadAreaOptions() {
      try {
        const res = await getTariffAreaOptions(this.areaOptionParams());
        const body = res.data || {};
        const data = body.code === 200 && body.data ? body.data : {};
        this.areaOptions = Array.isArray(data.areas) ? data.areas : [];
      } catch (error) {
        this.areaOptions = [];
      }
      this.syncAreaPath();
    },
    areaOptionParams() {
      if (this.form.effectiveType === "MONTH") {
        return { yearMonth: this.form.yearMonth };
      }
      if (this.form.effectiveType === "DAY") {
        return { selectedDate: this.form.selectedDate };
      }
      return { selectedDate: this.form.startDate || this.form.endDate };
    },
    normalizeDictItems(rows, fallbackValues) {
      const source = Array.isArray(rows) && rows.length ? rows : dictItems(fallbackValues);
      const seen = new Set();
      const result = [];
      source.forEach(item => {
        const value = typeof item === "string" ? item : item && (item.value || item.label);
        const label = typeof item === "string" ? item : item && (item.label || item.value);
        if (!value || seen.has(value)) {
          return;
        }
        seen.add(value);
        result.push({ label, value });
      });
      return result.length ? result : dictItems(fallbackValues);
    },
    syncPriceRowSelections() {
      this.form.priceRows.forEach(row => {
        row.userType = this.ensureDictValue(row.userType, this.userTypeOptions);
        row.dyLevel = this.ensureDictValue(row.dyLevel, this.dyLevelOptions);
        row.sfType = this.ensureDictValue(row.sfType, this.sfTypeOptions);
      });
    },
    ensureDictValue(value, options) {
      if (options.some(item => item.value === value)) {
        return value;
      }
      if (value) {
        options.push({ label: value, value });
        return value;
      }
      return options.length ? options[0].value : value;
    },
    handleAreaChange(path) {
      if (!Array.isArray(path) || path.length < 3) {
        return;
      }
      const [provinceCode, secondType, thirdType] = path;
      const province = this.areaCascaderOptions.find(item => item.value === provinceCode);
      this.form.provinceCode = provinceCode;
      this.form.provinceName = province ? province.label : provinceCode;
      this.form.secondType = secondType || "不限";
      this.form.thirdType = thirdType || "不限";
      this.syncAreaPath();
      this.clearPreview();
    },
    syncAreaPath() {
      this.areaPath = defaultAreaPath(this.form);
    },
    handleProvinceChange(code) {
      const province = this.provinceOptions.find(item => item.code === code);
      if (province) {
        this.form.provinceName = province.name;
      } else {
        this.form.provinceName = code;
      }
      this.syncAreaPath();
      this.clearPreview();
    },
    handleCopyProvinceChange(code) {
      const province = this.provinceOptions.find(item => item.code === code);
      if (province) {
        this.copyForm.sourceProvinceName = province.name;
      }
    },
    handleEffectiveScopeChange() {
      this.clearPreview();
      this.loadAreaOptions();
    },
    handleStartDateChange(value) {
      if (value && this.form.endDate && moment(value).isAfter(moment(this.form.endDate))) {
        this.form.endDate = value;
      }
      this.handleEffectiveScopeChange();
    },
    handleEndDateChange(value) {
      if (value && this.form.startDate && moment(value).isBefore(moment(this.form.startDate))) {
        this.form.startDate = value;
      }
      this.handleEffectiveScopeChange();
    },
    clearPreview() {
      this.previewData = null;
    },
    resetDefaultPeriods() {
      this.form.periods = defaultPeriods();
      this.timeSlots = buildSlotsFromPeriods(this.form.periods);
      this.clearPreview();
    },
    clearPeriods() {
      this.timeSlots = Array(96).fill("");
      this.clearPreview();
    },
    fillSelectedRange() {
      const start = Number(this.rangeFill.start);
      const end = Number(this.rangeFill.end);
      if (!Number.isInteger(start) || !Number.isInteger(end) || start < 0 || end > 96 || start >= end) {
        this.$message.warning("时间范围必须是 15 分钟刻度，且开始早于结束");
        return;
      }
      for (let index = start; index < end; index += 1) {
        this.$set(this.timeSlots, index, this.activePeriodType);
      }
      this.clearPreview();
    },
    fillMissingSlots() {
      let changed = false;
      this.timeSlots.forEach((periodType, index) => {
        if (!periodType) {
          this.$set(this.timeSlots, index, this.activePeriodType);
          changed = true;
        }
      });
      if (changed) {
        this.clearPreview();
      }
    },
    paintSlot(index) {
      this.painting = true;
      this.setSlotPeriod(index);
    },
    paintSlotWhileDrag(index) {
      if (!this.painting) {
        return;
      }
      this.setSlotPeriod(index);
    },
    stopPaint() {
      this.painting = false;
    },
    setSlotPeriod(index) {
      if (index < 0 || index >= this.timeSlots.length) {
        return;
      }
      this.$set(this.timeSlots, index, this.activePeriodType);
      this.clearPreview();
    },
    addPriceRow() {
      this.form.priceRows.push(defaultPriceRow());
      this.clearPreview();
    },
    duplicatePriceRow(row) {
      this.form.priceRows.push({
        ...JSON.parse(JSON.stringify(row)),
        _key: rowKey("price"),
      });
      this.clearPreview();
    },
    removePriceRow(index) {
      this.form.priceRows.splice(index, 1);
      if (!this.form.priceRows.length) {
        this.addPriceRow();
      }
      this.clearPreview();
    },
    async handlePreview() {
      this.previewLoading = true;
      try {
        const body = await this.postPreview();
        this.previewData = body.data;
        this.$message.success("预览已生成");
      } catch (error) {
        this.previewData = null;
        this.$message.error(this.errorMessage(error, "预览失败"));
      } finally {
        this.previewLoading = false;
      }
    },
    async handlePublish() {
      try {
        await this.$confirm("发布后会替换同版本、同省份、同地区下本次录入的价格对象，未录入的电压等级和用电类型会保留。", "发布确认", {
          type: "warning",
          confirmButtonText: "发布",
          cancelButtonText: "取消",
        });
      } catch (error) {
        return;
      }

      this.publishLoading = true;
      try {
        if (!this.previewData) {
          const previewBody = await this.postPreview();
          this.previewData = previewBody.data;
        }
        const res = await publishTariffRuleImport(this.buildPayload());
        const body = res.data || {};
        if (body.code !== 200) {
          throw new Error(body.msg || "发布失败");
        }
        const versions = body.data && Array.isArray(body.data.versions) ? body.data.versions.join(" / ") : "";
        this.$message.success(versions ? `发布成功：${versions}` : "发布成功");
      } catch (error) {
        this.$message.error(this.errorMessage(error, "发布失败"));
      } finally {
        this.publishLoading = false;
      }
    },
    async handleDelete() {
      const scopeText = this.deleteScopeText();
      try {
        await this.$confirm(
          `将物理删除 ${scopeText} 的正式电价数据。月度删除会同时删除该月所有日版本；单日或区间删除后日期查询可能回退到月度版本。删除后不可恢复。`,
          "物理删除确认",
          {
            type: "warning",
            confirmButtonText: "删除",
            cancelButtonText: "取消",
            confirmButtonClass: "el-button--danger",
          }
        );
      } catch (error) {
        return;
      }

      this.deleteLoading = true;
      try {
        const res = await deleteTariffRuleImport(this.buildDeletePayload());
        const body = res.data || {};
        if (body.code !== 200) {
          throw new Error(body.msg || "删除失败");
        }
        const data = body.data || {};
        const versions = this.formatVersionList(data.versions);
        this.previewData = null;
        this.$message.success(
          `删除成功${versions ? `：${versions}` : ""}，价格 ${data.priceRowCount || 0} 行，峰谷时段 ${data.fpgjRowCount || 0} 行`
        );
      } catch (error) {
        this.$message.error(this.errorMessage(error, "删除失败"));
      } finally {
        this.deleteLoading = false;
      }
    },
    async postPreview() {
      if (!this.periodValidation.valid) {
        throw new Error(this.periodValidation.message);
      }
      const res = await previewTariffRuleImport(this.buildPayload());
      const body = res.data || {};
      if (body.code !== 200) {
        throw new Error(body.msg || "预览失败");
      }
      return body;
    },
    openCopyDialog() {
      this.copyForm = defaultCopyForm(this.form);
      this.copyDialogVisible = true;
    },
    async handleCopy() {
      this.copyLoading = true;
      try {
        const res = await copyTariffRuleImport(this.buildCopyPayload());
        const body = res.data || {};
        if (body.code !== 200) {
          throw new Error(body.msg || "复制失败");
        }
        this.applyCopiedConfig(body.data || {});
        this.copyDialogVisible = false;
        this.$message.success("模板已复制");
      } catch (error) {
        this.$message.error(this.errorMessage(error, "复制失败"));
      } finally {
        this.copyLoading = false;
      }
    },
    buildPayload() {
      const periods = this.buildPeriodsFromSlots();
      this.form.periods = periods.map(item => ({
        _key: rowKey("period"),
        periodType: item.periodType,
        rangeText: item.rangeText,
        ranges: item.ranges,
      }));
      return {
        effectiveType: this.form.effectiveType,
        yearMonth: this.form.yearMonth,
        selectedDate: this.form.selectedDate,
        startDate: this.form.startDate,
        endDate: this.form.endDate,
        provinceCode: this.form.provinceCode,
        provinceName: this.form.provinceName,
        secondType: this.form.secondType || "不限",
        thirdType: this.form.thirdType || "不限",
        periods: periods.map(item => ({
          periodType: item.periodType,
          rangeText: item.rangeText,
          ranges: item.ranges,
        })),
        priceRows: this.form.priceRows.map(item => ({
          userType: item.userType,
          dyLevel: item.dyLevel,
          sfType: item.sfType,
          priceType: item.priceType || "电度",
          jianPrice: this.cleanNumber(item.jianPrice),
          fengPrice: this.cleanNumber(item.fengPrice),
          pingPrice: this.cleanNumber(item.pingPrice),
          guPrice: this.cleanNumber(item.guPrice),
          shenguPrice: this.cleanNumber(item.shenguPrice),
          demandElectricityPrice: this.cleanNumber(item.demandElectricityPrice),
          capacityElectricityPrice: this.cleanNumber(item.capacityElectricityPrice),
        })),
      };
    },
    buildDeletePayload() {
      return {
        effectiveType: this.form.effectiveType,
        yearMonth: this.form.yearMonth,
        selectedDate: this.form.selectedDate,
        startDate: this.form.startDate,
        endDate: this.form.endDate,
        provinceCode: this.form.provinceCode,
        secondType: this.form.secondType || "不限",
        thirdType: this.form.thirdType || "不限",
      };
    },
    buildCopyPayload() {
      return {
        sourceVersion: this.copyForm.sourceVersion,
        sourceProvinceCode: this.copyForm.sourceProvinceCode,
        sourceProvinceName: this.copyForm.sourceProvinceName,
        sourceSecondType: this.copyForm.sourceSecondType || "不限",
        sourceThirdType: this.copyForm.sourceThirdType || "不限",
        targetEffectiveType: this.form.effectiveType,
        targetYearMonth: this.form.yearMonth,
        targetSelectedDate: this.form.selectedDate,
        targetStartDate: this.form.startDate,
        targetEndDate: this.form.endDate,
        targetProvinceCode: this.form.provinceCode,
        targetProvinceName: this.form.provinceName,
        targetSecondType: this.form.secondType || "不限",
        targetThirdType: this.form.thirdType || "不限",
      };
    },
    deleteScopeText() {
      const dateText = this.form.effectiveType === "MONTH"
        ? this.form.yearMonth
        : this.form.effectiveType === "DAY"
          ? this.form.selectedDate
          : `${this.form.startDate} 至 ${this.form.endDate}`;
      return `${this.form.provinceName || this.form.provinceCode} / ${this.form.secondType || "不限"} / ${this.form.thirdType || "不限"} / ${dateText}`;
    },
    applyCopiedConfig(data) {
      this.form.effectiveType = data.effectiveType || this.form.effectiveType;
      this.form.yearMonth = data.yearMonth || this.form.yearMonth;
      this.form.selectedDate = data.selectedDate || this.form.selectedDate;
      this.form.startDate = data.startDate || this.form.startDate;
      this.form.endDate = data.endDate || this.form.endDate;
      this.form.provinceCode = data.provinceCode || this.form.provinceCode;
      this.form.provinceName = data.provinceName || this.form.provinceName;
      this.form.secondType = data.secondType || this.form.secondType || "不限";
      this.form.thirdType = data.thirdType || this.form.thirdType || "不限";
      this.form.periods = Array.isArray(data.periods) && data.periods.length
        ? data.periods.map(item => ({
          _key: rowKey("period"),
          periodType: item.periodType,
          ranges: Array.isArray(item.ranges) ? item.ranges : [],
          rangeText: item.rangeText || (Array.isArray(item.ranges) ? item.ranges.join(", ") : ""),
        }))
        : this.form.periods;
      this.timeSlots = buildSlotsFromPeriods(this.form.periods);
      this.form.priceRows = Array.isArray(data.priceRows) && data.priceRows.length
        ? data.priceRows.map(item => ({
          _key: rowKey("price"),
          userType: item.userType || "",
          dyLevel: item.dyLevel || "",
          sfType: item.sfType || "",
          priceType: item.priceType || "电度",
          jianPrice: this.cleanNumber(item.jianPrice),
          fengPrice: this.cleanNumber(item.fengPrice),
          pingPrice: this.cleanNumber(item.pingPrice),
          guPrice: this.cleanNumber(item.guPrice),
          shenguPrice: this.cleanNumber(item.shenguPrice),
          demandElectricityPrice: this.cleanNumber(item.demandElectricityPrice),
          capacityElectricityPrice: this.cleanNumber(item.capacityElectricityPrice),
        }))
        : this.form.priceRows;
      this.syncPriceRowSelections();
      this.syncAreaPath();
      this.clearPreview();
    },
    buildPeriodsFromSlots() {
      return this.periodRowsFromSlots(this.timeSlots).map(item => ({
        periodType: item.periodType,
        ranges: item.ranges,
        rangeText: item.rangesText,
      }));
    },
    periodRowsFromSlots(slots) {
      const grouped = {};
      PERIOD_OPTIONS.forEach(item => {
        grouped[item.type] = {
          periodType: item.type,
          color: item.color,
          ranges: [],
          pointCount: 0,
        };
      });
      let index = 0;
      while (index < slots.length) {
        const periodType = slots[index];
        const start = index;
        while (index < slots.length && slots[index] === periodType) {
          index += 1;
        }
        if (periodType && grouped[periodType]) {
          grouped[periodType].ranges.push(`${this.indexToTime(start)}-${this.indexToTime(index)}`);
          grouped[periodType].pointCount += index - start;
        }
      }
      return PERIOD_OPTIONS
        .map(item => grouped[item.type])
        .filter(item => item.pointCount > 0)
        .map(item => ({
          ...item,
          rangesText: item.ranges.join(", "),
        }));
    },
    cleanNumber(value) {
      if (value === "" || value === null || value === undefined) {
        return null;
      }
      const num = Number(value);
      return Number.isFinite(num) ? num : null;
    },
    periodColor(type) {
      const item = this.periodOptions.find(meta => meta.type === type);
      return item ? item.color : "#94a3b8";
    },
    indexToTime(index) {
      const safe = Math.max(0, Math.min(96, index));
      const hour = Math.floor(safe / 4);
      const minute = (safe % 4) * 15;
      return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
    },
    formatPrice(value) {
      const num = Number(value);
      if (!Number.isFinite(num)) {
        return "";
      }
      return num.toFixed(6).replace(/0+$/, "").replace(/\.$/, "");
    },
    formatVersionList(versions) {
      if (!Array.isArray(versions) || !versions.length) {
        return "";
      }
      if (versions.length <= 5) {
        return versions.join(" / ");
      }
      return `${versions[0]} 等 ${versions.length} 个版本`;
    },
    errorMessage(error, fallback) {
      return error?.response?.data?.msg || error?.message || fallback;
    },
  },
};
</script>

<style scoped lang="less">
.tariff-import-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
  min-height: 100%;
  color: #1f2933;
}

.import-panel {
  border: 1px solid #dde6ed;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;

  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #102a43;
    line-height: 1.3;
  }

  p {
    margin: 4px 0 0;
    color: #627d98;
    font-size: 13px;
  }
}

.panel-head.compact {
  margin-bottom: 12px;

  h2 {
    font-size: 16px;
  }
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.scope-panel {
  padding-bottom: 18px;
}

.scope-form {
  display: grid;
  gap: 14px;
}

.scope-row {
  display: grid;
  align-items: end;
  gap: 14px;
}

.scope-row-main {
  grid-template-columns: minmax(300px, 0.8fr) minmax(220px, 0.55fr) minmax(360px, 1.15fr);
}

.effective-field {
  /deep/ .el-radio-group {
    display: inline-flex;
    width: max-content;
    max-width: 100%;
  }
}

.time-field,
.area-field {
  min-width: 0;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;

  > span {
    color: #52606d;
    font-size: 12px;
    line-height: 16px;
  }

  /deep/ .el-select,
  /deep/ .el-cascader,
  /deep/ .el-date-editor,
  /deep/ .el-input {
    width: 100%;
  }
}

.date-pair {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.import-layout {
  display: grid;
  grid-template-columns: minmax(420px, 0.9fr) minmax(0, 1.6fr);
  gap: 14px;
  align-items: start;
}

.editable-table {
  border: 1px solid #edf2f7;
  border-radius: 6px;
  overflow: hidden;

  /deep/ .el-table__header th {
    background: #f7fafc;
    color: #52606d;
    font-weight: 600;
  }
}

.period-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.period-tool {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding: 0 10px;
  border: 1px solid #d9e2ec;
  border-radius: 6px;
  background: #ffffff;
  color: #334e68;
  font-size: 13px;
  cursor: pointer;

  i {
    width: 10px;
    height: 10px;
    border-radius: 2px;
    background: var(--period-color);
  }
}

.period-tool.active {
  border-color: var(--period-color);
  box-shadow: 0 0 0 2px rgba(7, 128, 237, 0.1);
  color: #102a43;
  font-weight: 700;
}

.range-fillbar {
  display: grid;
  grid-template-columns: repeat(2, minmax(112px, 1fr)) auto auto;
  gap: 8px;
  align-items: end;
  margin-bottom: 12px;
}

.range-fill-field {
  display: flex;
  flex-direction: column;
  gap: 5px;

  > span {
    color: #52606d;
    font-size: 12px;
  }
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(96, minmax(5px, 1fr));
  gap: 1px;
  height: 52px;
  border: 1px solid #d9e2ec;
  border-radius: 6px;
  overflow: hidden;
  background: #d9e2ec;
  user-select: none;
}

.slot-cell {
  position: relative;
  min-width: 0;
  height: 100%;
  padding: 0;
  border: 0;
  color: #ffffff;
  cursor: crosshair;

  span {
    position: absolute;
    top: 4px;
    left: 2px;
    writing-mode: vertical-rl;
    font-size: 10px;
    line-height: 1;
    color: rgba(255, 255, 255, 0.9);
    pointer-events: none;
  }
}

.slot-cell.empty {
  color: #52606d;

  span {
    color: #52606d;
  }
}

.validation-line {
  min-height: 22px;
  margin: 8px 0 10px;
  color: #13795b;
  font-size: 13px;
  line-height: 22px;
}

.validation-line.invalid {
  color: #b42318;
}

.period-summary-table {
  /deep/ .el-table__body-wrapper {
    max-height: 260px;
    overflow-y: auto;
  }
}

.price-row-list {
  display: grid;
  gap: 12px;
}

.price-row-card {
  border: 1px solid #edf2f7;
  border-radius: 6px;
  background: #fbfdff;
  padding: 12px;
}

.price-row-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.price-row-title {
  display: grid;
  gap: 3px;
  min-width: 0;

  strong {
    color: #102a43;
    font-size: 14px;
    line-height: 20px;
  }

  span {
    color: #627d98;
    font-size: 12px;
    line-height: 18px;
    overflow-wrap: anywhere;
  }
}

.price-row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.price-base-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.price-value-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(96px, 1fr));
  gap: 10px;
}

.price-number-field {
  /deep/ .el-input__inner {
    padding-left: 8px;
    padding-right: 8px;
    text-align: left;
  }

  /deep/ input[type="number"]::-webkit-outer-spin-button,
  /deep/ input[type="number"]::-webkit-inner-spin-button {
    margin: 0;
    -webkit-appearance: none;
  }

  /deep/ input[type="number"] {
    -moz-appearance: textfield;
  }
}

.danger-text {
  color: #c2410c;
}

.preview-panel {
  min-height: 320px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 10px;
  margin-bottom: 14px;

  div {
    border: 1px solid #edf2f7;
    border-radius: 6px;
    padding: 10px 12px;
    background: #fbfdff;
  }

  span {
    display: block;
    color: #627d98;
    font-size: 12px;
    margin-bottom: 4px;
  }

  strong {
    display: block;
    min-height: 20px;
    color: #102a43;
    font-size: 14px;
    overflow-wrap: anywhere;
  }
}

.timeline-strip {
  display: grid;
  grid-template-columns: repeat(96, minmax(3px, 1fr));
  gap: 1px;
  height: 28px;
  border-radius: 6px;
  overflow: hidden;
  background: #d9e2ec;

  span {
    min-width: 0;
  }
}

.timeline-axis {
  display: flex;
  justify-content: space-between;
  margin: 6px 0 14px;
  color: #829ab1;
  font-size: 11px;
}

.preview-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.8fr) minmax(420px, 1.2fr);
  gap: 14px;
}

.period-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 22px;
  border-radius: 4px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
}

.price-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;

  em {
    font-style: normal;
    border: 1px solid #dde6ed;
    border-radius: 4px;
    padding: 2px 6px;
    background: #ffffff;
    color: #334e68;
    line-height: 18px;
  }
}

.empty-state {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  color: #829ab1;
  background: #fbfdff;
}

.copy-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 1180px) {
  .scope-row-main,
  .import-layout,
  .preview-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .panel-head,
  .stats-row,
  .copy-form {
    grid-template-columns: 1fr;
  }

  .panel-head {
    display: grid;
  }

  .stats-row {
    display: grid;
  }

  .date-pair,
  .range-fillbar,
  .price-base-grid {
    grid-template-columns: 1fr;
  }

  .price-row-head {
    display: grid;
  }

  .price-row-actions {
    justify-content: flex-start;
  }

  .slot-grid {
    grid-template-columns: repeat(48, minmax(6px, 1fr));
    height: 84px;
  }
}
</style>
