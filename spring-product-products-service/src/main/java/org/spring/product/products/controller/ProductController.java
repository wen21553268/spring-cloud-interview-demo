package org.spring.product.products.controller;

import lombok.RequiredArgsConstructor;
import org.spring.product.products.model.dto.ProductDTO;
import org.spring.product.products.model.dto.ProductRequest;
import org.spring.product.products.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/get")
    @PreAuthorize("hasAuthority('USER')")
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasAuthority('EDITOR')")
    public ProductDTO createProduct(@RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @PostMapping(value = "/update/{id}")
    @PreAuthorize("hasAuthority('EDITOR')")
    public ProductDTO updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @GetMapping(value = "/delete/{id}")
    @PreAuthorize("hasAuthority('EDITOR')")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}