package cn.sl.ehub.console.grid.controller;

import cn.sl.ehub.console.grid.service.IssueWebService;
import cn.sl.ehub.service.dto.ControlIssueDTO;
import cn.sl.ehub.common.vo.ResultVO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/iot")
@Api(tags = "iot下发")
public class IotCmdSetController {

    @Resource
    private IssueWebService issueWebService;

    @ApiOperation(value = "下发设置")
    @RequestMapping(value = "/cmdSet", method = RequestMethod.POST)
    public ResultVO<String> cmdSet(@RequestBody ControlIssueDTO issue) {
        return ResultVO.success(issueWebService.controlIssue(JSONObject.toJSONString(issue)));
    }


}
