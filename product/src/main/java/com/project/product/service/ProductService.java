package com.project.product.service;

import com.project.product.db.ProductRepository;
import com.project.product.dto.ProductRequestDTO;
import com.project.product.dto.ProductResponseDTO;
import com.project.product.exception.BadRequestException;
import com.project.product.exception.ResourceNotFoundException;
import com.project.product.model.Product;
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

    public ProductResponseDTO addProduct(ProductRequestDTO dto) {

        if (productRepository.existsByProductName(dto.getProductName())) {
            throw new BadRequestException(
                    "Product with name '" + dto.getProductName() + "' already exists");
        }

        Product product = mapToEntity(dto);
        product.setStatus("ACTIVE");
        return mapToDTO(productRepository.save(product));
    }

    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));

        product.setProductName(dto.getProductName());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());

        return mapToDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));


        product.setStatus("INACTIVE");
        productRepository.save(product);


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