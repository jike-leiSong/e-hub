package cn.sl.ehub.console.grid.service;


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.IOException;

/**
 * @Description: webService接口
 * @Author sl
 * @Date 2026-05-28
 */
@WebService(targetNamespace = "http://ws.la.enn.cn/", name = "IssueWebService")
public interface IssueWebService {


    @WebMethod
    String controlIssue(

            @WebParam(name = "controlIssueRequest")
                    String controlIssueRequest
    );

    @WebMethod
    String singleRetryIssue(

            @WebParam(name = "retryIssueRequest")
                    String retryIssueRequest
    ) throws IOException;

    @WebMethod
    String totalRetryIssue(

            @WebParam(name = "retryIssueRequest")
                    String retryIssueRequest
    );

    @WebMethod
    String clearIssue(

            @WebParam(name = "clearIssueRequest")
                    String clearIssueRequest
    );
}
