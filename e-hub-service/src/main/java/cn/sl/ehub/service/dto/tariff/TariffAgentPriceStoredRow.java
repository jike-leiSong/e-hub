package cn.sl.ehub.service.dto.tariff;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TariffAgentPriceStoredRow {

    private String id;

    private String userType;

    private String dyLevel;

    private String otherType;

    private String priceType;

    private BigDecimal capacityElectricityPrice;

    private BigDecimal demandElectricityPrice;
}
