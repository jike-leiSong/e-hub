package cn.sl.ehub.service.dto.tariff;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TariffAgentPriceDataInsertRow {

    private String agentPriceId;

    private String bizTime;

    private BigDecimal price;
}
