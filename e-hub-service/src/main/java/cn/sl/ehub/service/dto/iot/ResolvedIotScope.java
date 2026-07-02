package cn.sl.ehub.service.dto.iot;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResolvedIotScope {

    private String aggregatorId;

    private String entId;

    private String energyStationCode;

    private boolean deviceRestricted;

    private boolean emptyScope;

    private List<Long> deviceIds = new ArrayList<>();

    private List<String> deviceCodes = new ArrayList<>();
}
