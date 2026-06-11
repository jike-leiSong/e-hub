package cn.sl.ehub.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("数据返回实体")
public class DataResp {

    @ApiModelProperty("时间")
    private String time;
    @ApiModelProperty("值")
    private Double value;

    public DataResp() {
    }

    public DataResp(String time, Double value) {
        this.time = time;
        this.value = value;
    }
}
