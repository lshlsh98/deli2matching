package com.example.deli2matching.controller;

import com.example.deli2matching.dto.delivery.DeliveryListReqDTO;
import com.example.deli2matching.entity.delivery.DeliveryList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @GetMapping()
    public ResponseEntity<?> getList(@RequestBody DeliveryListReqDTO req) {
        List<DeliveryList> list =

    }//
}
