package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.resp.DictItemResp;
import cn.sl.ehub.console.model.resp.DictTypeResp;

import java.util.List;

public interface IPlatformDictService {

    List<DictTypeResp> types();

    List<DictItemResp> items(String dictType);
}
