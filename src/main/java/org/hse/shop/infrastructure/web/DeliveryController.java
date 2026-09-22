package org.hse.shop.infrastructure.web;

import org.hse.shop.application.DeliveryCostService;
import org.hse.shop.domain.DeliveryMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/** Предварительный расчёт доставки, чтобы показать его покупателю до оформления заказа */
@RestController
public class DeliveryController {

    record DeliveryCost(DeliveryMethod method, BigDecimal itemsTotal, BigDecimal cost) {
    }

    private final DeliveryCostService deliveryCost;

    public DeliveryController(DeliveryCostService deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    @GetMapping("/api/delivery/cost")
    public DeliveryCost cost(@RequestParam DeliveryMethod method, @RequestParam BigDecimal itemsTotal) {
        return new DeliveryCost(method, itemsTotal, deliveryCost.costFor(method, itemsTotal));
    }
}
