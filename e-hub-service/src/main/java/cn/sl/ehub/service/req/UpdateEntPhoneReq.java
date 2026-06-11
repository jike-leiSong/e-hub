package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 更新用户联系人信息实体
 *
 * @Author sl
 * @phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("更新用户联系人信息实体")
public class UpdateEntPhoneReq {

    private String smsName;
    private String smsPhone;
}
