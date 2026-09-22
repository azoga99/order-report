package org.hse.shop.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hse.shop.application.ProductCatalogService;
import org.hse.shop.application.StockService;
import org.hse.shop.domain.Product;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Каталог товаров и поступления на склад */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    record NewProduct(@NotBlank String name, @NotNull @Positive BigDecimal price) {
    }

    record NewPrice(@NotNull @Positive BigDecimal price) {
    }

    record Supply(@Positive int quantity) {
    }

    private final ProductCatalogService catalog;
    private final StockService stock;

    public ProductController(ProductCatalogService catalog, StockService stock) {
        this.catalog = catalog;
        this.stock = stock;
    }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(defaultValue = "false") boolean inStock) {
        List<Product> products = inStock ? catalog.inStock() : catalog.list();
        return products.stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable long id) {
        return ProductResponse.from(catalog.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse add(@Valid @RequestBody NewProduct request) {
        return ProductResponse.from(catalog.add(request.name(), request.price()));
    }

    @PutMapping("/{id}/price")
    public ProductResponse changePrice(@PathVariable long id, @Valid @RequestBody NewPrice request) {
        return ProductResponse.from(catalog.changePrice(id, request.price()));
    }

    @PostMapping("/{id}/supply")
    public ProductResponse supply(@PathVariable long id, @Valid @RequestBody Supply request) {
        return ProductResponse.from(stock.receive(id, request.quantity()));
    }
}
