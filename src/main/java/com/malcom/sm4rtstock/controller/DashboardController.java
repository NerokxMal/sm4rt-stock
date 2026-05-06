package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.model.DashboardStats;
import com.malcom.sm4rtstock.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> stats() {
        return ResponseEntity.ok(dashboardService.obtenerStats());
    }
}