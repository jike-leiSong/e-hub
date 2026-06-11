package cn.sl.ehub.console.service;

import cn.sl.ehub.console.req.PeakPlanDeclareImportReq;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 调峰计划申报Service接口
 *
 * @author sl
 * @date 2026-05-28
 */
public interface IPeakPlanDeclareService {

    /**
     * 导入调峰计划申报数据
     *
     * @param req 导入请求
     * @return 导入结果
     */
    String importPeakPlanDeclare(PeakPlanDeclareImportReq req);
}
