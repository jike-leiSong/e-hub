package cn.sl.ehub.service.dto.tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
@ApiModel("好买电菜单请求")
public class HaomaidianMenuReq {

    @NotBlank(message = "菜单类型不能为空")
    @ApiModelProperty("菜单类型，4表示默认菜单")
    private String menuType;

    @ApiModelProperty("电费年月 yyyy-MM")
    private String yearMonth;

    @ApiModelProperty("省份编码")
    private String provinceCode;

    @ApiModelProperty("二级分类")
    private String secondType;

    @ApiModelProperty("三级分类")
    private String thirdType;

    @ApiModelProperty("企业用电属性")
    private String userType;

    @ApiModelProperty("企业用电电压等级")
    private String dyLevel;

    @ApiModelProperty("是否限制返回省份")
    private String isLimitProvince = "0";

    @ApiModelProperty("收费类型")
    private String otherType;
}
