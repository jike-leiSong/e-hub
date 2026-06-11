package cn.sl.ehub.console.controller;

import cn.sl.ehub.common.utils.DingUtil;
import cn.sl.ehub.common.vo.ResultVO;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 健康检查
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RestController
@RequestMapping("/health")
@Api(tags = "健康检查")
public class HealthController {

    @ApiOperation(value = "健康检查")
    @RequestMapping(value = "/getSuccess", method = RequestMethod.GET)
    public ResultVO<String> getSuccess() {
        return ResultVO.success("success");
    }

    @Value("${apollo.check}")
    private String apolloCheck;

    @ApiOperation(value = "apollo测试接口")
    @GetMapping("/apollo")
    public ResultVO<String> getApolloCheck() {
        return ResultVO.success(apolloCheck);
    }

    @ApiOperation(value = "钉钉测试接口")
    @GetMapping("/dingSendTest")
    public ResultVO<Boolean> dingSendTest() {
        try {
            DingUtil.sendMsg("SECdea6e749470094096ca4ce97dda1e48678678e353ff4716d6440635f3da26322",
                    "https://oapi.dingtalk.com/robot/send?access_token=4761c8f56b05df3aeb404acfc085a923045bdfb31f7776f1c57d033b478ab2e4",
                    "1", true, Lists.newArrayList());
            DingUtil.sendMsg("SECdea6e749470094096ca4ce97dda1e48678678e353ff4716d6440635f3da26322",
                    "https://oapi.dingtalk.com/robot/send?access_token=4761c8f56b05df3aeb404acfc085a923045bdfb31f7776f1c57d033b478ab2e4",
                    "11", false, Arrays.asList("18910140332"));
            DingUtil.sendMsgAll("SECdea6e749470094096ca4ce97dda1e48678678e353ff4716d6440635f3da26322",
                    "https://oapi.dingtalk.com/robot/send?access_token=4761c8f56b05df3aeb404acfc085a923045bdfb31f7776f1c57d033b478ab2e4",
                    "111");
            DingUtil.sendMsgByMobile("SECdea6e749470094096ca4ce97dda1e48678678e353ff4716d6440635f3da26322",
                    "https://oapi.dingtalk.com/robot/send?access_token=4761c8f56b05df3aeb404acfc085a923045bdfb31f7776f1c57d033b478ab2e4",
                    "1111", Arrays.asList("18910140332"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultVO.success(true);
    }
}
