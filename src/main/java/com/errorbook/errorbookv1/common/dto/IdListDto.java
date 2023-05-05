package com.errorbook.errorbookv1.common.dto;

import lombok.Data;

@Data
public class IdListDto {
    private static final long serialVersionUID = 1L;
    //用户id
    private Long userId;
    //多个id
    private Long[] ids;
}
