package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("Console客户产品开通")
@Table(name = "console_customer_product")
public class ConsoleCustomerProduct {

    @Id
    @GeneratedValue(generator = "JDBC")
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;

    @ApiModelProperty("登录用户ID")
    @Column(name = "user_id")
    private String userId;

    @ApiModelProperty("客户ID，优先企业ID，其次聚合商ID")
    @Column(name = "customer_id")
    private String customerId;

    @ApiModelProperty("产品编码")
    @Column(name = "product_code")
    private String productCode;

    @ApiModelProperty("是否启用 1启用 0停用")
    @Column(name = "enabled")
    private Integer enabled;

    @ApiModelProperty("生效开始日期 yyyy-MM-dd")
    @Column(name = "valid_from")
    private String validFrom;

    @ApiModelProperty("生效结束日期 yyyy-MM-dd")
    @Column(name = "valid_to")
    private String validTo;

    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private String createTime;

    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private String updateTime;
}
