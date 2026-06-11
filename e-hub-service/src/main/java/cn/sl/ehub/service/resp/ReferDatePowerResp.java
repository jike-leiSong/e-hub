package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

 /**
  * 参考日功率返回实体
  *
  * @author sl
  * @classes cn.sl.ehub.upstream.resp.ReferDatePowerResp
  * @date 2026-05-28
  */
@Data
@ApiModel("企业用户申报计划返回实体")
public class ReferDatePowerResp {

   private List<ReferDatePowerDataResp> list;
}
