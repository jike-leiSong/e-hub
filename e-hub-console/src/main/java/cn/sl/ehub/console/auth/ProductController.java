package cn.sl.ehub.console.auth;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.model.ProductCustomerResp;
import cn.sl.ehub.console.auth.model.ProductOptionResp;
import cn.sl.ehub.console.auth.model.ProductProvisionReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "Console产品开通")
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ConsoleProductService productService;

    public ProductController(ConsoleProductService productService) {
        this.productService = productService;
    }

    @ApiOperation("产品选项")
    @GetMapping("/options")
    public ResultVO<List<ProductOptionResp>> options() {
        return ResultVO.success(productService.productOptions());
    }

    @ApiOperation("客户产品开通列表")
    @GetMapping("/customers")
    public ResultVO<List<ProductCustomerResp>> customers(@RequestParam(value = "keyword", required = false) String keyword) {
        return ResultVO.success(productService.listCustomers(keyword));
    }

    @ApiOperation("保存客户产品开通")
    @PutMapping("/customers/{userId}/products")
    public ResultVO<Boolean> saveProducts(@PathVariable("userId") String userId,
                                          @RequestBody ProductProvisionReq req) {
        productService.saveCustomerProducts(userId, req == null ? null : req.getProductCodes());
        return ResultVO.success(true);
    }
}
