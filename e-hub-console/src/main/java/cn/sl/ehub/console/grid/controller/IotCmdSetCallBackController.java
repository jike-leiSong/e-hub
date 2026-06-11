package cn.sl.ehub.console.grid.controller;

import cn.enn.iot.vo.IotCallBackQueryVo;
import cn.sl.ehub.console.grid.service.IotCmdSetCallBackService;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ProjectName: industry-provider-trusteeship
 * @Package: com.enn.industry.trusteeship.controller
 * @ClassName: OperationalOverviewController
 * @Description: <iot指令下发回调>
 * @CreateDate: 2020/6/16 2:35 下午
 * @UpdateUser: xx
 * @UpdateDate: 2020/6/16 2:35 下午
 * @UpdateRemark: xx
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/iot")
@Api(tags = "iot指令回调")
public class IotCmdSetCallBackController {

    @Autowired
    private IotCmdSetCallBackService iotCmdSetCallBackService;

    @ApiOperation(value = "iot指令回调")
    @RequestMapping(value = "/callBack", method = RequestMethod.POST)
    public ResultVO<Boolean> iotCallBack(@RequestBody IotCallBackQueryVo callBackData) {

        ResultVO<Boolean> resp = new ResultVO();
        try {
            Boolean result = iotCmdSetCallBackService.iotCallBack(callBackData);
            resp = ResultVO.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("iot 回调失败 {}", e.getMessage());
            resp = ResultVO.fail(StatusCode.IOT_ERROR.getCode(), StatusCode.IOT_ERROR.getMsg(), callBackData.getUid());
        }
        return resp;
    }
}
