package cn.sl.ehub.console.grid.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.service.service.ClearIssueLogService;
import cn.sl.ehub.console.service.ITripartDataSynchronService;
import cn.sl.ehub.common.vo.ResultVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class SynchronizeService {

    @Resource
    private ITripartDataSynchronService tripartDataService;

    @Resource
    private ClearIssueLogService clearIssueLogService;

    public ResultVO<Boolean> clearIssue() {
        String cmdData = clearIssueLogService.getTodayLastedCmdData();
        if (StringUtils.isNotBlank(cmdData)) {
            ResultVO<Boolean> booleanResultVO = tripartDataService.synchronRetry(cmdData);
            return booleanResultVO;
        } else {
            return ResultVO.fail(StatusCode.E_B.getCode(), StatusCode.E_B.getMsg());
        }
    }
}
