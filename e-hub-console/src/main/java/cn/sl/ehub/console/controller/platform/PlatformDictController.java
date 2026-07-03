package cn.sl.ehub.console.controller.platform;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.resp.DictItemResp;
import cn.sl.ehub.console.model.resp.DictTypeResp;
import cn.sl.ehub.console.service.IPlatformDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/dict")
@Api(tags = "平台字典")
public class PlatformDictController {

    private final IPlatformDictService platformDictService;

    @GetMapping("/types")
    @ApiOperation("字典类型列表")
    public ResultVO<List<DictTypeResp>> types() {
        return ResultVO.success(platformDictService.types());
    }

    @GetMapping("/items")
    @ApiOperation("字典项列表")
    public ResultVO<List<DictItemResp>> items(@RequestParam("dictType") String dictType) {
        return ResultVO.success(platformDictService.items(dictType));
    }
}
