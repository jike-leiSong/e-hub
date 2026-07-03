package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel("字典项")
@Table(name = "console_dict_item")
public class ConsoleDictItem {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "dict_type")
    @ApiModelProperty("字典类型")
    private String dictType;

    @Column(name = "item_code")
    @ApiModelProperty("编码")
    private String itemCode;

    @Column(name = "item_name")
    @ApiModelProperty("名称")
    private String itemName;

    @Column(name = "item_value")
    @ApiModelProperty("值")
    private String itemValue;

    @Column(name = "sort_no")
    @ApiModelProperty("排序")
    private Integer sortNo;

    @Column(name = "status")
    @ApiModelProperty("状态")
    private Integer status;

    @Column(name = "ext_json")
    @ApiModelProperty("扩展JSON")
    private String extJson;

    @Column(name = "create_time")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Column(name = "update_time")
    @ApiModelProperty("更新时间")
    private String updateTime;
}
