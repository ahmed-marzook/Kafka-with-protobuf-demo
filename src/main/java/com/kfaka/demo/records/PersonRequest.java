package com.kfaka.demo.records;

import java.util.List;

public record PersonRequest(
        String name,
        int id,
        String email,
        List<PhoneRequest> phones
) {}
