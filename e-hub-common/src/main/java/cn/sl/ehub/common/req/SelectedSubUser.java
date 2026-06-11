package cn.sl.ehub.common.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * 邀约子用户
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel(value = "邀约子用户")
public class SelectedSubUser {

    private String assetCode;
    private List<Double> baseLine;
}
