package online.faramita.bbs.entity;

import lombok.Data;

@Data
public class BlogCategory {
    private Long id;
    private String name;
    private Long bigCategoryId;
    private Long userId;
}
