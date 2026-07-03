package cn.sl.ehub.console.controller.platform;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.req.ConfigItemPageReq;
import cn.sl.ehub.console.model.req.ConfigItemUpsertReq;
import cn.sl.ehub.console.model.resp.ConfigItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IPlatformConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/config")
@Api(tags = "平台配置")
public class PlatformConfigController {

    private final IPlatformConfigService platformConfigService;

    @GetMapping("/items")
    @ApiOperation("配置项分页")
    public ResultVO<PageResultVO<ConfigItemResp>> items(ConfigItemPageReq req) {
        return ResultVO.success(platformConfigService.items(req));
    }

    @PostMapping("/items")
    @ApiOperation("新增配置项")
    public ResultVO<ConfigItemResp> create(@RequestBody ConfigItemUpsertReq req) {
        return ResultVO.success(platformConfigService.create(req));
    }

    @PutMapping("/items/{id}")
    @ApiOperation("更新配置项")
    public ResultVO<ConfigItemResp> update(@PathVariable("id") Long id,
                                           @RequestBody ConfigItemUpsertReq req) {
        return ResultVO.success(platformConfigService.update(id, req));
    }
}
