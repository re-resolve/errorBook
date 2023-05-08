package com.errorbook.errorbookv1.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class IdListDto implements Serializable {
    private static final long serialVersionUID = 1L;
    //用户id
    private Long userId;
    //多个id
    private Long[] ids;
}
