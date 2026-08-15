package com.carexport.shipping;

import java.math.BigDecimal;

public class ShippingEstimateResponse {
    private OriginPort originPort;
    private DestinationPort destinationPort;
    private BigDecimal baseFreightCost;
    private BigDecimal handlingFee;
    private BigDecimal totalCost;
    private String currency;

    public ShippingEstimateResponse(OriginPort originPort, DestinationPort destinationPort,
                                     BigDecimal baseFreightCost, BigDecimal handlingFee,
                                     BigDecimal totalCost, String currency) {
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.baseFreightCost = baseFreightCost;
        this.handlingFee = handlingFee;
        this.totalCost = totalCost;
        this.currency = currency;
    }

    public OriginPort getOriginPort() { return originPort; }
    public DestinationPort getDestinationPort() { return destinationPort; }
    public BigDecimal getBaseFreightCost() { return baseFreightCost; }
    public BigDecimal getHandlingFee() { return handlingFee; }
    public BigDecimal getTotalCost() { return totalCost; }
    public String getCurrency() { return currency; }
}
