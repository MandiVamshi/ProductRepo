package com.project.retailproject.clients;

import com.project.retailproject.dto.AuditLogRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "AuditLogApplication", url = "${audit.service.url}")
public interface AuditLogClient {

    @PostMapping("/api/audit-logs")
    ResponseEntity<Void> createAuditLog(@RequestBody AuditLogRequestDTO dto);
}