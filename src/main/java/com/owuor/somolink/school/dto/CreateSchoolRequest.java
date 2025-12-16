package com.owuor.somolink.school.dto;

import lombok.Data;

@Data
public class CreateSchoolRequest {
    private String name;
    private String code;
    private String location;
}
