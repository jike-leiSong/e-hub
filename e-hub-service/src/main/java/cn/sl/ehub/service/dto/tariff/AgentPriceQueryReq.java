package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("电网代理价格查询请求")
public class AgentPriceQueryReq {

    @ApiModelProperty("省份/区域编码")
    private String provinceCode;

    @ApiModelProperty("省份名称")
    private String provinceName;

    @ApiModelProperty("二级分类")
    private String secondType;

    @ApiModelProperty("三级分类")
    private String thirdType;

    @ApiModelProperty("电费年月 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("选择日期 yyyy-MM-dd")
    private String selectedDate;

    @ApiModelProperty("企业用电性质")
    private String userType;

    @ApiModelProperty("企业用电电压等级")
    private String dyLevel;

    @ApiModelProperty("收费类型/其他属性")
    private String sfType;

    @ApiModelProperty("价格类型")
    private String priceType;
}
