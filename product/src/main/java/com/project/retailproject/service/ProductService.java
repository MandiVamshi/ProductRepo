package com.project.retailproject.service;

import com.project.retailproject.clients.AuditLogClient;
import com.project.retailproject.db.ProductRepository;
import com.project.retailproject.dto.AuditLogRequestDTO;
import com.project.retailproject.dto.ProductRequestDTO;
import com.project.retailproject.dto.ProductResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogClient auditLogClient;  // ✅ inject AuditLogClient

    public ProductResponseDTO addProduct(ProductRequestDTO dto) {
        if (productRepository.existsByProductName(dto.getProductName())) {
            sendAuditLog(
                    "Product.CREATE_FAILED | Error: Duplicate name: " + dto.getProductName(),
                    null, null
            );
            throw new BadRequestException(
                    "Product with name '" + dto.getProductName() + "' already exists");
        }

        Product product = mapToEntity(dto);
        product.setStatus("ACTIVE");
        ProductResponseDTO result = mapToDTO(productRepository.save(product));

        sendAuditLog(
                "Product.CREATE_SUCCESS | ProductID: " + result.getProductId()
                        + " | Name: " + result.getProductName()
                        + " | Category: " + result.getCategory()
                        + " | Price: " + result.getPrice()
                        + " | Status: ACTIVE",
                null, null
        );

        return result;
    }

    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));

        String before = "Name: " + product.getProductName()
                + " | Category: " + product.getCategory()
                + " | Price: " + product.getPrice()
                + " | Status: " + product.getStatus();

        product.setProductName(dto.getProductName());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());

        ProductResponseDTO result = mapToDTO(productRepository.save(product));

        sendAuditLog(
                "Product.UPDATE_SUCCESS | ProductID: " + id
                        + " | Before: " + before
                        + " | After: Name: " + dto.getProductName()
                        + " | Category: " + dto.getCategory()
                        + " | Price: " + dto.getPrice()
                        + " | Status: " + dto.getStatus(),
                null, null
        );

        return result;
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));

        product.setStatus("INACTIVE");
        productRepository.save(product);

        sendAuditLog(
                "Product.DELETE_SUCCESS | ProductID: " + id
                        + " | Name: " + product.getProductName()
                        + " | Status: INACTIVE",
                null, null
        );
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getProductById(Long id) {
        return mapToDTO(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id)));
    }

    public List<ProductResponseDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductResponseDTO> getAllProductsWithPagination(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToDTO);
    }

    // ✅ Same pattern as UserService.sendAuditLog
    private void sendAuditLog(String action, Long userId, String userName) {
        try {
            AuditLogRequestDTO dto = new AuditLogRequestDTO();
            dto.setAction(action);
            dto.setUserId(userId);
            dto.setUserName(userName);
            auditLogClient.createAuditLog(dto);
        } catch (Exception e) {
            System.err.println("AuditLog service unavailable: " + e.getMessage());
        }
    }

    private Product mapToEntity(ProductRequestDTO dto) {
        Product p = new Product();
        p.setProductName(dto.getProductName());
        p.setCategory(dto.getCategory());
        p.setPrice(dto.getPrice());
        p.setStatus(dto.getStatus());
        return p;
    }

    private ProductResponseDTO mapToDTO(Product p) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setCategory(p.getCategory());
        dto.setPrice(p.getPrice());
        dto.setStatus(p.getStatus());
        return dto;
    }
}