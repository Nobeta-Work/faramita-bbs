package cn.nobeta.bbs.common.result;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;

    List<T> records;

    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return PageResult.<T>builder()
                .total(0L)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(0)
                .records(Collections.emptyList())
                .build();
    }
}
