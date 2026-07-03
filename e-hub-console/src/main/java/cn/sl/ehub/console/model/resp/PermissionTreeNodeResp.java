package cn.sl.ehub.console.model.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("权限树节点响应")
public class PermissionTreeNodeResp {

    @ApiModelProperty("权限编码")
    private String permissionCode;

    @ApiModelProperty("权限名称")
    private String permissionName;

    @ApiModelProperty("权限类型")
    private String permissionType;

    @ApiModelProperty("模块编码")
    private String moduleCode;

    @ApiModelProperty("路径")
    private String path;

    @ApiModelProperty("是否选中")
    private Boolean checked;

    @ApiModelProperty("子节点")
    private List<PermissionTreeNodeResp> children;
}
