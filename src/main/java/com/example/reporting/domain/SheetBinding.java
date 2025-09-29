package com.example.reporting.domain;

import java.util.List;

public record SheetBinding(
        String name,
        List<Binding> bindings
) {}
