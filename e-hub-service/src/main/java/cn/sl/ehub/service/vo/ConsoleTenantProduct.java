package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("租户产品订阅")
@Table(name = "console_tenant_product")
public class ConsoleTenantProduct {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "tenant_id")
    @ApiModelProperty("租户ID")
    private String tenantId;

    @Column(name = "product_code")
    @ApiModelProperty("产品编码")
    private String productCode;

    @Column(name = "enabled")
    @ApiModelProperty("是否启用")
    private Integer enabled;

    @Column(name = "valid_from")
    @ApiModelProperty("开始日期")
    private String validFrom;

    @Column(name = "valid_to")
    @ApiModelProperty("结束日期")
    private String validTo;

    @Column(name = "config_json")
    @ApiModelProperty("配置JSON")
    private String configJson;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Column(name = "update_time")
    @ApiModelProperty("更新时间")
    private String updateTime;
}
