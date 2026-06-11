package cn.sl.ehub.console.service;

/**
 * 数据处理Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IDataV2Service {

    /**
     * 数据处理
     *
     * @param issue
     */
    void dealData(String issue);

    /**
     * 数据处理
     *
     * @param issue
     */
    void dealDataDetail(String issue);

    /**
     * 处理出清
     *
     * @param startDate
     * @param endDate
     */
    void dealClear(String startDate, String endDate);

    double gridClear(String startDate, String endDate);
}
