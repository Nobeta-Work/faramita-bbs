package cn.nobeta.bbs.module.blog.mapper;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import cn.nobeta.bbs.module.blog.entity.BlogSearchDocument;

public interface BlogSearchRepository
    extends ElasticsearchRepository<BlogSearchDocument, Long> {

}
