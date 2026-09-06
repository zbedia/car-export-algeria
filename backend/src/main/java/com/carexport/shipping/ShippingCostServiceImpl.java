package com.carexport.shipping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Base freight rates below are indicative placeholders — replace them
 * with actual carrier rates (e.g. from a freight forwarder or shipping
 * line) before relying on this for real cost estimates.
 */
@Service
public class ShippingCostServiceImpl implements ShippingCostService {

    private record Route(OriginPort origin, DestinationPort destination) {}

    private static final Map<Route, BigDecimal> BASE_FREIGHT_RATES_EUR = Map.ofEntries(
        Map.entry(new Route(OriginPort.MARSEILLE, DestinationPort.ALGER), new BigDecimal("850")),
        Map.entry(new Route(OriginPort.MARSEILLE, DestinationPort.ORAN), new BigDecimal("900")),
        Map.entry(new Route(OriginPort.MARSEILLE, DestinationPort.BEJAIA), new BigDecimal("950")),
        Map.entry(new Route(OriginPort.ALICANTE, DestinationPort.ALGER), new BigDecimal("700")),
        Map.entry(new Route(OriginPort.ALICANTE, DestinationPort.ORAN), new BigDecimal("650")),
        Map.entry(new Route(OriginPort.ALICANTE, DestinationPort.BEJAIA), new BigDecimal("800")),
        Map.entry(new Route(OriginPort.SETE, DestinationPort.ALGER), new BigDecimal("870")),
        Map.entry(new Route(OriginPort.SETE, DestinationPort.ORAN), new BigDecimal("910")),
        Map.entry(new Route(OriginPort.SETE, DestinationPort.BEJAIA), new BigDecimal("960"))
    );

    private final BigDecimal handlingFeeEur;

    public ShippingCostServiceImpl(@Value("${shipping.handling-fee-eur}") BigDecimal handlingFeeEur) {
        this.handlingFeeEur = handlingFeeEur;
    }

    @Override
    public ShippingEstimateResponse estimate(OriginPort origin, DestinationPort destination) {
        BigDecimal baseFreight = BASE_FREIGHT_RATES_EUR.get(new Route(origin, destination));
        if (baseFreight == null) {
            throw new RouteNotSupportedException(origin, destination);
        }

        BigDecimal total = baseFreight.add(handlingFeeEur);
        return new ShippingEstimateResponse(origin, destination, baseFreight, handlingFeeEur, total, "EUR");
    }
}