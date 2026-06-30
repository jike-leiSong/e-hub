package cn.sl.ehub.service.dto.iot;

import lombok.Data;

@Data
public class IotPointDefinitionSaveReq {

    private Long id;

    private String value;

    private String description;

    private String tags;

    private Integer sort;

    private String remark;
}
