package cn.nobeta.bbs.module.blog.service;

import org.springframework.stereotype.Service;

import cn.nobeta.bbs.module.blog.entity.BlogSearchDocument;
import cn.nobeta.bbs.module.blog.mapper.BlogMapper;
import cn.nobeta.bbs.module.blog.mapper.BlogSearchRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogSearchIndexer {

    private final BlogMapper blogMapper;
    private final BlogSearchRepository searchRepository;

    
    public void syncBlog(Long blogId) {
        BlogSearchDocument document = 
            blogMapper.selectSearchDocumentById(blogId);

        if (document == null) {
            // 博客未发布 || 博客删除 -> 删除 ES 文档
            searchRepository.deleteById(blogId);
            return;
        }

        // 博客存在 && 公开 -> 新增/覆盖 ES 文档
        searchRepository.save(document);
    }
}
