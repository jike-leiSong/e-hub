package cn.sl.ehub.console.model.req;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 调峰计划申报Excel导入请求
 *
 * @author sl
 * @date 2026-05-28
 */
@Data
@ApiModel("调峰计划申报Excel导入请求")
public class PeakPlanDeclareImportReq {

    private Date startDate;
    private Date endDate;

    @NotNull(message = "Excel文件不能为空")
    @ApiModelProperty(value = "Excel文件", required = true)
    private MultipartFile file;

    @NotNull(message = "资源类型不能为空")
    @ApiModelProperty(value = "资源类型code（电采暖:15, 工业负荷:44）", required = true, example = "15")
    private Integer resourceType;

    @ApiModelProperty(value = "聚合商ID")
    private String aggregatorId;
}
