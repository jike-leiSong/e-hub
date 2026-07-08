package cn.sl.ehub.console.controller.tariff;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigQueryReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigResp;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigSaveReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentQueryReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentResp;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentSaveReq;
import cn.sl.ehub.service.service.TariffSourceService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tariff/sources")
@RequiredArgsConstructor
@Api(tags = "电价数据来源")
public class TariffSourceController {

    private final TariffSourceService tariffSourceService;

    @GetMapping("/configs")
    @ApiOperation("查询电价数据来源配置")
    public ResultVO<List<TariffSourceConfigResp>> sourceConfigs(TariffSourceConfigQueryReq req) {
        return ResultVO.success(tariffSourceService.listSourceConfigs(req));
    }

    @GetMapping("/configs/{id}")
    @ApiOperation("查询电价数据来源配置详情")
    public ResultVO<TariffSourceConfigResp> sourceConfig(@PathVariable("id") Long id) {
        return ResultVO.success(tariffSourceService.getSourceConfig(id));
    }

    @PostMapping("/configs")
    @ApiOperation("新增电价数据来源配置")
    public ResultVO<TariffSourceConfigResp> createSourceConfig(@RequestBody TariffSourceConfigSaveReq req) {
        return ResultVO.success(tariffSourceService.createSourceConfig(req));
    }

    @PutMapping("/configs/{id}")
    @ApiOperation("更新电价数据来源配置")
    public ResultVO<TariffSourceConfigResp> updateSourceConfig(@PathVariable("id") Long id,
                                                               @RequestBody TariffSourceConfigSaveReq req) {
        return ResultVO.success(tariffSourceService.updateSourceConfig(id, req));
    }

    @DeleteMapping("/configs/{id}")
    @ApiOperation("停用电价数据来源配置")
    public ResultVO<Boolean> disableSourceConfig(@PathVariable("id") Long id) {
        tariffSourceService.setSourceConfigEnabled(id, 0);
        return ResultVO.success(true);
    }

    @PutMapping("/configs/{id}/enabled")
    @ApiOperation("启用或停用电价数据来源配置")
    public ResultVO<Boolean> setSourceConfigEnabled(@PathVariable("id") Long id,
                                                    @RequestParam("enabled") Integer enabled) {
        tariffSourceService.setSourceConfigEnabled(id, enabled);
        return ResultVO.success(true);
    }

    @GetMapping("/documents")
    @ApiOperation("分页查询电价来源文档")
    public ResultVO<PageResultVO<TariffSourceDocumentResp>> sourceDocuments(
            TariffSourceDocumentQueryReq req,
            @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<TariffSourceDocumentResp> list = tariffSourceService.listSourceDocuments(req);
        return ResultVO.success(toPage(list, pageIndex, pageSize));
    }

    @GetMapping("/documents/{id}")
    @ApiOperation("查询电价来源文档详情")
    public ResultVO<TariffSourceDocumentResp> sourceDocument(@PathVariable("id") Long id) {
        return ResultVO.success(tariffSourceService.getSourceDocument(id));
    }

    @PostMapping("/documents")
    @ApiOperation("新增电价来源文档")
    public ResultVO<TariffSourceDocumentResp> createSourceDocument(@RequestBody TariffSourceDocumentSaveReq req) {
        return ResultVO.success(tariffSourceService.createSourceDocument(req));
    }

    @PutMapping("/documents/{id}")
    @ApiOperation("更新电价来源文档")
    public ResultVO<TariffSourceDocumentResp> updateSourceDocument(@PathVariable("id") Long id,
                                                                   @RequestBody TariffSourceDocumentSaveReq req) {
        return ResultVO.success(tariffSourceService.updateSourceDocument(id, req));
    }

    @PutMapping("/documents/{id}/status")
    @ApiOperation("更新电价来源文档状态")
    public ResultVO<TariffSourceDocumentResp> updateSourceDocumentStatus(@PathVariable("id") Long id,
                                                                         @RequestParam("status") String status) {
        return ResultVO.success(tariffSourceService.updateSourceDocumentStatus(id, status));
    }

    @DeleteMapping("/documents/{id}")
    @ApiOperation("归档电价来源文档")
    public ResultVO<TariffSourceDocumentResp> archiveSourceDocument(@PathVariable("id") Long id) {
        return ResultVO.success(tariffSourceService.updateSourceDocumentStatus(id, TariffSourceService.STATUS_ARCHIVED));
    }

    private <T> PageResultVO<T> toPage(List<T> list, Integer pageIndex, Integer pageSize) {
        PageInfo<T> pageInfo = new PageInfo<T>(list);
        PageResultVO<T> page = new PageResultVO<T>();
        page.setList(list);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }
}
