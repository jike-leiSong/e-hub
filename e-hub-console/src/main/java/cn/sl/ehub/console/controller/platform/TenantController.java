package cn.sl.ehub.console.controller.platform;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.model.req.TenantPageReq;
import cn.sl.ehub.console.model.req.TenantProductSaveReq;
import cn.sl.ehub.console.model.req.TenantStatusUpdateReq;
import cn.sl.ehub.console.model.req.TenantUpsertReq;
import cn.sl.ehub.console.model.resp.TenantDetailResp;
import cn.sl.ehub.console.model.resp.TenantPageItemResp;
import cn.sl.ehub.console.model.resp.TenantProductResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.ITenantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tenant")
@Api(tags = "租户中心")
public class TenantController {

    private final ITenantService tenantService;

    @GetMapping("/page")
    @ApiOperation("租户分页查询")
    public ResultVO<PageResultVO<TenantPageItemResp>> page(TenantPageReq req) {
        return ResultVO.success(tenantService.page(req));
    }

    @GetMapping("/{tenantId}")
    @ApiOperation("租户详情")
    public ResultVO<TenantDetailResp> detail(@PathVariable("tenantId") String tenantId) {
        return ResultVO.success(tenantService.detail(tenantId));
    }

    @PostMapping
    @ApiOperation("新增租户")
    public ResultVO<TenantDetailResp> create(@RequestBody TenantUpsertReq req) {
        return ResultVO.success(tenantService.create(req));
    }

    @PutMapping("/{tenantId}")
    @ApiOperation("更新租户")
    public ResultVO<TenantDetailResp> update(@PathVariable("tenantId") String tenantId,
                                             @RequestBody TenantUpsertReq req) {
        return ResultVO.success(tenantService.update(tenantId, req));
    }

    @PutMapping("/{tenantId}/status")
    @ApiOperation("租户状态变更")
    public ResultVO<Boolean> updateStatus(@PathVariable("tenantId") String tenantId,
                                          @RequestBody TenantStatusUpdateReq req) {
        return ResultVO.success(tenantService.updateStatus(tenantId, req));
    }

    @GetMapping("/{tenantId}/products")
    @ApiOperation("租户产品列表")
    public ResultVO<List<TenantProductResp>> products(@PathVariable("tenantId") String tenantId) {
        return ResultVO.success(tenantService.products(tenantId));
    }

    @PutMapping("/{tenantId}/products")
    @ApiOperation("保存租户产品")
    public ResultVO<Boolean> saveProducts(@PathVariable("tenantId") String tenantId,
                                          @RequestBody TenantProductSaveReq req) {
        return ResultVO.success(tenantService.saveProducts(tenantId, req));
    }
}
