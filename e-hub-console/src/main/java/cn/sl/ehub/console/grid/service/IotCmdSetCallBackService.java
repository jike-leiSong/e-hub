package cn.sl.ehub.console.grid.service;

import cn.enn.iot.contants.IotMsgContants;
import cn.enn.iot.contants.IotStatuCode;
import cn.enn.iot.contants.IotStatusCode;
import cn.enn.iot.vo.IotCallBackQueryVo;
import cn.sl.ehub.service.mapper.IotCmdSetLogMapper;
import cn.sl.ehub.service.vo.IotCmdSetLog;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class IotCmdSetCallBackService {

    @Resource
    private IotCmdSetLogMapper iotCmdSetLogMapper;

    public Boolean iotCallBack(IotCallBackQueryVo callBackData) {
        Example example = new Example(IotCmdSetLog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("uId", callBackData.getUid());
        IotCmdSetLog setLog = new IotCmdSetLog();
        if (callBackData.getCode().equals(String.valueOf(IotStatuCode.SUCCESS))) {
            // iot code
            setLog.setCallBackCode(Integer.valueOf(callBackData.getCode()));
            // 回调时间
            setLog.setCallBackTime(new Date());
            setLog.setCallBackResult(IotMsgContants.SUCCESS_MSG);
        } else {
            // iot code
            setLog.setCallBackCode(Integer.valueOf(callBackData.getCode()));
            setLog.setCallBackResult(callBackData.getMsg());
        }
        setLog.setUpdateTime(new Date());
        iotCmdSetLogMapper.updateByExampleSelective(setLog, example);
        return true;
    }
}
