package cn.sl.ehub.console.grid.controller;

import cn.sl.ehub.console.grid.service.IssueWebService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@RestController
@RequestMapping("/issue")
@Api(tags = "下发")
public class IssueController {

    @Resource
    private IssueWebService issueWebService;

    @ApiOperation(value = "出清下发")
    @PostMapping("/clearIssue")
    public String clearIssue(@RequestParam(value = "clearIssueRequest") String clearIssueRequest) {

        return issueWebService.clearIssue(clearIssueRequest);

    }
}
