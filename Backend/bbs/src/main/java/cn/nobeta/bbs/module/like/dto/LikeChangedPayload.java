package cn.nobeta.bbs.module.like.dto;

import lombok.Data;

@Data
public class LikeChangedPayload {
    private Long userId;
    private Boolean liked;
}
