package cn.sl.ehub.console.grid.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.jws.WebService;
import java.io.IOException;

/**
 * @Description: webService接口实现
 * @Author sl
 * @Date 2026-05-28
 */
@Slf4j
@Service
@WebService(serviceName = "IssueWebService",
        targetNamespace = "http://ws.la.enn.cn/",
        endpointInterface = "cn.sl.ehub.console.grid.service.IssueWebService")
public class IssueWebServiceImpl implements IssueWebService {

    @Override
    public String controlIssue(String controlIssueRequest) {
        log.info("controlIssue called with request: {}", controlIssueRequest);
        return "success";
    }

    @Override
    public String singleRetryIssue(String retryIssueRequest) throws IOException {
        log.info("singleRetryIssue called with request: {}", retryIssueRequest);
        return "success";
    }

    @Override
    public String totalRetryIssue(String retryIssueRequest) {
        log.info("totalRetryIssue called with request: {}", retryIssueRequest);
        return "success";
    }

    @Override
    public String clearIssue(String clearIssueRequest) {
        log.info("clearIssue called with request: {}", clearIssueRequest);
        return "success";
    }
}
