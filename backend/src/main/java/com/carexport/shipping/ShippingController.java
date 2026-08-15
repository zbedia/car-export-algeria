package com.carexport.shipping;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingCostService shippingCostService;

    public ShippingController(ShippingCostService shippingCostService) {
        this.shippingCostService = shippingCostService;
    }

    @GetMapping("/estimate")
    public ResponseEntity<ShippingEstimateResponse> estimate(
        @RequestParam OriginPort originPort,
        @RequestParam DestinationPort destinationPort
    ) {
        return ResponseEntity.ok(shippingCostService.estimate(originPort, destinationPort));
    }
}
