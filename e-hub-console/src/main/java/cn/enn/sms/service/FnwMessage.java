package cn.enn.sms.service;

import cn.enn.sms.resp.AliveClient;
import cn.sl.ehub.common.vo.ResultVO;

import java.util.List;

public interface FnwMessage {
    Object sendMessage(Object req);
    ResultVO<List<AliveClient>> getAliveClients(String entId);
}
