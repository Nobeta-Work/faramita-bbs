package cn.nobeta.bbs.module.blog.service;

import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.module.blog.dto.BlogEditDTO;
import cn.nobeta.bbs.module.blog.dto.BlogPageQuery;
import cn.nobeta.bbs.module.blog.dto.BlogSaveDTO;
import cn.nobeta.bbs.module.blog.vo.BlogPrivateDetailVO;
import cn.nobeta.bbs.module.blog.vo.BlogPublicBriefVO;
import cn.nobeta.bbs.module.blog.vo.BlogPublicDetailVO;

public interface BlogService {

    PageResult<BlogPublicBriefVO> queryPublicBlogPage(BlogPageQuery blogPageQuery);

    Long addBlogByUserId(Long userId, BlogSaveDTO blogSaveDTO);

    BlogPublicDetailVO getPublicBlogById(Long id);

    BlogPrivateDetailVO getPrivateBlogById(Long id);

    void editBlogById(Long blogId, BlogEditDTO blogEditDTO);

    void deleteBlogById(Long id);

    

    

}
