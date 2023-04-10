package com.example.errorBook.common.dto;

import lombok.Data;

@Data
public class IdListDto {
    private static final long serialVersionUID = 1L;
    //多个id
    private Long[] ids;
}
