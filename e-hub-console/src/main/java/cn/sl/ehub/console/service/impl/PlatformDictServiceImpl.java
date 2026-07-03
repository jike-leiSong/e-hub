package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.model.resp.DictItemResp;
import cn.sl.ehub.console.model.resp.DictTypeResp;
import cn.sl.ehub.console.service.IPlatformDictService;
import cn.sl.ehub.service.mapper.ConsoleDictItemMapper;
import cn.sl.ehub.service.mapper.ConsoleDictTypeMapper;
import cn.sl.ehub.service.vo.ConsoleDictItem;
import cn.sl.ehub.service.vo.ConsoleDictType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformDictServiceImpl implements IPlatformDictService {

    private final ConsoleDictTypeMapper consoleDictTypeMapper;
    private final ConsoleDictItemMapper consoleDictItemMapper;

    @Override
    public List<DictTypeResp> types() {
        List<ConsoleDictType> list = consoleDictTypeMapper.listAll(1);
        List<DictTypeResp> respList = new ArrayList<>();
        for (ConsoleDictType item : list) {
            DictTypeResp resp = new DictTypeResp();
            resp.setDictType(item.getDictType());
            resp.setDictName(item.getDictName());
            resp.setStatus(item.getStatus());
            resp.setRemark(item.getRemark());
            respList.add(resp);
        }
        return respList;
    }

    @Override
    public List<DictItemResp> items(String dictType) {
        List<ConsoleDictItem> list = consoleDictItemMapper.listByDictType(StringUtils.trimToEmpty(dictType), 1);
        List<DictItemResp> respList = new ArrayList<>();
        for (ConsoleDictItem item : list) {
            DictItemResp resp = new DictItemResp();
            resp.setDictType(item.getDictType());
            resp.setItemCode(item.getItemCode());
            resp.setItemName(item.getItemName());
            resp.setItemValue(item.getItemValue());
            resp.setSortNo(item.getSortNo());
            resp.setStatus(item.getStatus());
            resp.setExtJson(item.getExtJson());
            respList.add(resp);
        }
        return respList;
    }
}
