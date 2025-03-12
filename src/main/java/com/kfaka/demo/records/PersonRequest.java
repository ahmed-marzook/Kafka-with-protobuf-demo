package com.kfaka.demo.records;

import com.kfaka.demo.controller.MessageController;

import java.util.List;

public record PersonRequest(
        String name,
        int id,
        String email,
        List<PhoneRequest> phones
) {}
