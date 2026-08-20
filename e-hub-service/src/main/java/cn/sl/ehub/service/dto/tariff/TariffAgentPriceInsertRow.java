package cn.sl.ehub.service.dto.tariff;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TariffAgentPriceInsertRow {

    private String id;

    private String version;

    private String provinceCode;

    private String provinceName;

    private String secondType;

    private String thirdType;

    private String dyLevel;

    private String userType;

    private String otherType;

    private String priceType;

    private String createBy;

    private BigDecimal capacityElectricityPrice;

    private BigDecimal demandElectricityPrice;
}
