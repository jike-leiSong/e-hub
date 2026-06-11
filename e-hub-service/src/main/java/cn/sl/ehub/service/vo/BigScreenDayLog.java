package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 今日日志内容
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("今日日志内容")
@Table(name = "big_screen_day_log")
public class BigScreenDayLog {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;
    @ApiModelProperty("时间")
    @Column(name = "time")
    private String time;
    @ApiModelProperty("内容")
    @Column(name = "content")
    private String content;
    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private Date createTime;
    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private Date updateTime;
}
