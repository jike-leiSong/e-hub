package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.auth.AuthContext;
import cn.sl.ehub.console.auth.AuthUser;
import cn.sl.ehub.console.model.req.OperationLogPageReq;
import cn.sl.ehub.console.model.resp.OperationLogPageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IPlatformAuditLogService;
import cn.sl.ehub.service.mapper.ConsoleOperationLogMapper;
import cn.sl.ehub.service.vo.ConsoleOperationLog;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformAuditLogServiceImpl implements IPlatformAuditLogService {

    private final ConsoleOperationLogMapper consoleOperationLogMapper;

    @Override
    public PageResultVO<OperationLogPageItemResp> logs(OperationLogPageReq req) {
        Integer pageIndex = req.getPageIndex() == null || req.getPageIndex() < 1 ? 1 : req.getPageIndex();
        Integer pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 20 : req.getPageSize();
        PageHelper.startPage(pageIndex, pageSize);
        List<ConsoleOperationLog> list = consoleOperationLogMapper.page(
                StringUtils.trimToNull(req.getBizType()),
                StringUtils.trimToNull(req.getOperatorUserId()),
                StringUtils.trimToNull(req.getStartTime()),
                StringUtils.trimToNull(req.getEndTime())
        );
        List<OperationLogPageItemResp> respList = new ArrayList<>();
        for (ConsoleOperationLog item : list) {
            respList.add(toPageItem(item));
        }
        PageInfo<ConsoleOperationLog> pageInfo = new PageInfo<>(list);
        PageResultVO<OperationLogPageItemResp> page = new PageResultVO<>();
        page.setList(respList);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public void record(String bizType, String bizId, String action, Object beforeValue, Object afterValue, String result, String errorMsg) {
        try {
            AuthUser user = AuthContext.get();
            ConsoleOperationLog logEntity = new ConsoleOperationLog();
            logEntity.setBizType(StringUtils.defaultIfBlank(bizType, "UNKNOWN"));
            logEntity.setBizId(StringUtils.trimToNull(bizId));
            logEntity.setAction(StringUtils.defaultIfBlank(action, "UNKNOWN"));
            logEntity.setOperatorUserId(user == null ? null : user.getUserId());
            logEntity.setOperatorName(user == null ? null : user.getDisplayName());
            logEntity.setRequestPath(currentRequestPath());
            logEntity.setBeforeJson(toJson(beforeValue));
            logEntity.setAfterJson(toJson(afterValue));
            logEntity.setResult(StringUtils.defaultIfBlank(result, "SUCCESS"));
            logEntity.setErrorMsg(StringUtils.trimToNull(errorMsg));
            logEntity.setCreateTime(DateUtils.getTime());
            consoleOperationLogMapper.insertSelective(logEntity);
        } catch (Exception e) {
            log.warn("记录平台操作日志失败 bizType={}, action={}", bizType, action, e);
        }
    }

    private String toJson(Object value) {
        return value == null ? null : JSON.toJSONString(value);
    }

    private String currentRequestPath() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request == null ? null : request.getRequestURI();
    }

    private OperationLogPageItemResp toPageItem(ConsoleOperationLog item) {
        OperationLogPageItemResp resp = new OperationLogPageItemResp();
        resp.setId(item.getId());
        resp.setBizType(item.getBizType());
        resp.setBizId(item.getBizId());
        resp.setAction(item.getAction());
        resp.setOperatorUserId(item.getOperatorUserId());
        resp.setOperatorName(item.getOperatorName());
        resp.setRequestPath(item.getRequestPath());
        resp.setResult(item.getResult());
        resp.setErrorMsg(item.getErrorMsg());
        resp.setCreateTime(item.getCreateTime());
        return resp;
    }
}
