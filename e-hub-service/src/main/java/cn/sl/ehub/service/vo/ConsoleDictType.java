package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("字典类型")
@Table(name = "console_dict_type")
public class ConsoleDictType {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "dict_type")
    @ApiModelProperty("字典类型")
    private String dictType;

    @Column(name = "dict_name")
    @ApiModelProperty("字典名称")
    private String dictName;

    @Column(name = "status")
    @ApiModelProperty("状态")
    private Integer status;

    @Column(name = "remark")
    @ApiModelProperty("备注")
    private String remark;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Column(name = "update_time")
    @ApiModelProperty("更新时间")
    private String updateTime;
}
