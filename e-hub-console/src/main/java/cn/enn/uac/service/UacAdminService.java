package cn.enn.uac.service;

import cn.sl.ehub.common.vo.ResultVO;

public interface UacAdminService {
    Object getEntInfo(String entId);
    Object getDevopsEntInfo(String entId);
    ResultVO<Boolean> checkTicket(String ticket);
}
