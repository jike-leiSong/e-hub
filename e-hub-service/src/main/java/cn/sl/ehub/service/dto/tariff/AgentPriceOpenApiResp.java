package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("开放接口代理电价响应")
public class AgentPriceOpenApiResp {

    @ApiModelProperty("内部版本")
    private String version;

    @ApiModelProperty("电费年月 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("二级区域")
    private String secondType;

    @ApiModelProperty("三级区域")
    private String thirdType;

    @ApiModelProperty("企业用电属性")
    private String userType;

    @ApiModelProperty("电压等级")
    private String dyLevel;

    @ApiModelProperty("收费类型")
    private String sfType;

    @ApiModelProperty("价格单位")
    private String unit = "元/kWh";

    @ApiModelProperty("容量电价")
    private BigDecimal capacityElectricityPrice;

    @ApiModelProperty("需量电价")
    private BigDecimal demandElectricityPrice;

    @ApiModelProperty("分时段聚合价格")
    private Map<String, AgentPricePeriodResp> periodSummary = new LinkedHashMap<>();

    @ApiModelProperty("96 点明细")
    private List<AgentPricePointResp> points96 = new ArrayList<>();

    @ApiModelProperty("数据来源")
    private AgentPriceSourceResp source;
}
