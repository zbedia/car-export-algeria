package com.carexport.shipping;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ShippingEstimateResponse {
    private final OriginPort originPort;
    private final DestinationPort destinationPort;
    private final BigDecimal baseFreightCost;
    private final BigDecimal handlingFee;
    private final BigDecimal totalCost;
    private final String currency;
}