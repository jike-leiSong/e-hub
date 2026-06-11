package cn.sl.ehub.common.dto;

import lombok.Data;

import java.util.HashMap;

/**
 * @Description: 控制下发转化类
 * @Author sl
 * @Date 2026-05-28
 */
@Data
public class ControlIssueDTO {

    private String remoteId;

    private String remoteName;

    private HashMap<String,String> cmdData;

}
