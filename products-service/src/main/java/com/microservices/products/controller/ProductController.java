package com.microservices.products.controller;

import com.microservices.products.dto.JsonApiError;
import com.microservices.products.dto.JsonApiResponse;
import com.microservices.products.dto.ProductDto;
import com.microservices.products.dto.ProductCreateDto;
import com.microservices.products.model.Product;
import com.microservices.products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management API following JSON API specification")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<JsonApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductCreateDto request) {

        Product product = new Product(
            request.getAttributes().getName(),
            request.getAttributes().getPrice(),
            request.getAttributes().getDescription()
        );

        Product createdProduct = productService.createProduct(product);
        ProductDto responseDto = new ProductDto(
            createdProduct.getId().toString(),
            createdProduct.getName(),
            createdProduct.getPrice(),
            createdProduct.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(JsonApiResponse.success(responseDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<JsonApiResponse<ProductDto>> getProduct(@PathVariable Long id) {
        logger.info("Fetching product with ID: {}", id);
        
        Optional<Product> product = productService.getProductById(id);
        
        if (product.isPresent()) {
            Product p = product.get();
            ProductDto productDto = new ProductDto(
                p.getId().toString(),
                p.getName(),
                p.getPrice(),
                p.getDescription()
            );
            return ResponseEntity.ok(JsonApiResponse.success(productDto));
        } else {
            List<JsonApiError> errors = List.of(
                new JsonApiError("404", "Not Found", "Product with ID " + id + " not found")
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(JsonApiResponse.error(errors));
        }
    }

    @GetMapping
    @Operation(summary = "Get all products")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<JsonApiResponse<ProductDto>> getAllProducts() {
        logger.info("Fetching all products");
        
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = products.stream()
            .map(p -> new ProductDto(
                p.getId().toString(),
                p.getName(),
                p.getPrice(),
                p.getDescription()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(JsonApiResponse.successList(productDtos));
    }
}