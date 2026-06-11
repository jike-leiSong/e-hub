package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户情况返回实体
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("用户情况返回实体")
public class EntUserOverviewResp implements Comparable<EntUserOverviewResp> {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("企业编码")
    private String stationId;
    @ApiModelProperty("企业名称")
    private String entName;
    @ApiModelProperty("申报状态 0=未申报 1=邀约 2=已申报")
    private String applyStatus;
    @ApiModelProperty("邀约人(明日 未申报 状态下有值)")
    private String inviteBy;
    @ApiModelProperty("邀约时间(明日 未申报 状态下有值)")
    private String inviteTime;
    @ApiModelProperty("申报时间")
    private String applyTime;
    @ApiModelProperty("总收益")
    private Double totalProfit;

    @Override
    public int compareTo(EntUserOverviewResp resp) {
        if (null != resp) {
            if (null == this.applyStatus) {
                this.applyStatus = "";
            }
            if (null == resp.getApplyStatus()) {
                resp.setApplyStatus("");
            }
            if (null == this.applyTime) {
                this.applyTime = "";
            }
            if (null == resp.getApplyTime()) {
                resp.setApplyTime("");
            }
            int applyStatusSort = resp.getApplyStatus().compareTo(this.applyStatus);
            if (0 != applyStatusSort) {
                return applyStatusSort;
            }
            return this.applyTime.compareTo(resp.getApplyTime());
        }
        return 0;
    }
}
