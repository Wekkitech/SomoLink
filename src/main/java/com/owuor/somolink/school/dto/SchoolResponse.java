package com.owuor.somolink.school.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class SchoolResponse {
    private Long id;
    private Long bridgeId;
    private String name;
    private String code;
    private String location;
    private boolean active;
}
