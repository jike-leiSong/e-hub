package cn.sl.ehub.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Description: 出清下发转化类
 * @Author sl
 * @Date 2026-05-28
 */
@Data
public class ClearIssueDTO {

    private ArrayList<HashMap<String,String>> cmdData;

}
