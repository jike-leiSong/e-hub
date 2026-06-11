package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.CommonSqlMapper;
import cn.sl.ehub.console.service.ICommonSqlService;
import org.springframework.stereotype.Service;

/**
 * 公共SQLServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Service
public class CommonSqlServiceImpl implements ICommonSqlService {

    private final CommonSqlMapper sqlMapper;

    public CommonSqlServiceImpl(CommonSqlMapper sqlMapper) {
        this.sqlMapper = sqlMapper;
    }

    @Override
    public Boolean updateDataBase(String sql) {
        sqlMapper.updateDataBase(sql);
        return true;
    }
}
