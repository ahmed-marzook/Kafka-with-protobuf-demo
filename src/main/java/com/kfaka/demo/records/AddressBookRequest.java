package com.kfaka.demo.records;

import java.util.List;

public record AddressBookRequest(List<PersonRequest> people) {}
