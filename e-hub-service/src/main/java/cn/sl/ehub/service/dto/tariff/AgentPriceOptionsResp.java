package cn.sl.ehub.service.dto.tariff;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentPriceOptionsResp {

    private List<AgentPriceAreaOption> areas = new ArrayList<>();

    private List<String> userTypes = new ArrayList<>();

    private List<String> sfTypes = new ArrayList<>();

    private List<String> dyLevels = new ArrayList<>();
}
