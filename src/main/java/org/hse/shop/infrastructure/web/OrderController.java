package org.hse.shop.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hse.shop.application.Cancellation;
import org.hse.shop.application.OrderCancellationService;
import org.hse.shop.application.OrderHistoryService;
import org.hse.shop.application.OrderLine;
import org.hse.shop.application.OrderPlacementService;
import org.hse.shop.application.PaymentService;
import org.hse.shop.domain.DeliveryMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Заказы: оформление, просмотр, оплата и отмена */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    record NewOrder(@NotNull Long customerId, @NotNull DeliveryMethod delivery,
                    @NotEmpty List<@Valid Line> items) {
    }

    record Line(@NotNull Long productId, @Positive int quantity) {
    }

    record CancellationResponse(OrderResponse order, BigDecimal refund) {
    }

    private final OrderPlacementService placement;
    private final OrderHistoryService history;
    private final PaymentService payments;
    private final OrderCancellationService cancellation;

    public OrderController(OrderPlacementService placement, OrderHistoryService history, PaymentService payments,
                           OrderCancellationService cancellation) {
        this.placement = placement;
        this.history = history;
        this.payments = payments;
        this.cancellation = cancellation;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody NewOrder request) {
        List<OrderLine> lines = request.items().stream()
                .map(line -> new OrderLine(line.productId(), line.quantity()))
                .toList();
        return OrderResponse.from(placement.place(request.customerId(), lines, request.delivery()));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable long id) {
        return OrderResponse.from(history.get(id));
    }

    @PostMapping("/{id}/payment")
    public OrderResponse pay(@PathVariable long id, @Valid @RequestBody PaymentDto payment) {
        return OrderResponse.from(payments.pay(id, payment.toPayment()));
    }

    @PostMapping("/{id}/cancel")
    public CancellationResponse cancel(@PathVariable long id) {
        Cancellation result = cancellation.cancel(id);
        return new CancellationResponse(OrderResponse.from(result.order()), result.refund());
    }
}
