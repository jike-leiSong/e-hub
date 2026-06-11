package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 企业社会责任计算配置
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("企业社会责任计算配置")
@Table(name = "aggregator_ent_social_responsibility")
public class AggregatorEntSocialResponsibility {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("编码")
    @Column(name = "code")
    private String code;
    @ApiModelProperty("名称")
    @Column(name = "name")
    private String name;
    @ApiModelProperty("计算值")
    @Column(name = "value")
    private Double value;
    @ApiModelProperty("保留小数位")
    @Column(name = "point")
    private Integer point;
    @ApiModelProperty("单位")
    @Column(name = "unit")
    private String unit;
}
