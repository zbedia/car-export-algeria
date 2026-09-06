package com.carexport.shipping;

import java.math.BigDecimal;

/**
 * Estimates RoRo (Roll-on/Roll-off) freight costs for the supported
 * Europe-to-Algeria routes.
 */
public interface ShippingCostService {

    ShippingEstimateResponse estimate(OriginPort origin, DestinationPort destination);
}