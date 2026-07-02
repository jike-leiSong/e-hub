package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@ApiModel("系统字典")
@Table(name = "sys_dict")
public class SysDict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("字典类型编码")
    @Column(name = "dict_type_code")
    private String dictTypeCode;

    @ApiModelProperty("字典类型名称")
    @Column(name = "dict_type_name")
    private String dictTypeName;

    @ApiModelProperty("字典编码")
    @Column(name = "dict_code")
    private String dictCode;

    @ApiModelProperty("字典值")
    @Column(name = "dict_value")
    private String dictValue;

    @ApiModelProperty("排序")
    @Column(name = "sort")
    private Integer sort;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @ApiModelProperty("删除标识：0正常，1删除")
    @Column(name = "deleted")
    private Integer deleted;
}
