package com.kfaka.demo.records;

import com.kfaka.demo.controller.MessageController;

import java.util.List;

public record AddressBookRequest(List<PersonRequest> people) {}
