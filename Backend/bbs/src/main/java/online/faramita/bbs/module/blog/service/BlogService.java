package online.faramita.bbs.module.blog.service;

import online.faramita.bbs.common.result.PageResult;
import online.faramita.bbs.module.blog.dto.BlogEditDTO;
import online.faramita.bbs.module.blog.dto.BlogPageQuery;
import online.faramita.bbs.module.blog.dto.BlogSaveDTO;
import online.faramita.bbs.module.blog.vo.BlogPrivateDetailVO;
import online.faramita.bbs.module.blog.vo.BlogPublicBriefVO;
import online.faramita.bbs.module.blog.vo.BlogPublicDetailVO;

public interface BlogService {

    PageResult<BlogPublicBriefVO> queryPublicBlogPage(BlogPageQuery blogPageQuery);

    Long addBlogByUserId(Long userId, BlogSaveDTO blogSaveDTO);

    BlogPublicDetailVO getPublicBlogById(Long id);

    BlogPrivateDetailVO getPrivateBlogById(Long id);

    void editBlogById(Long blogId, BlogEditDTO blogEditDTO);

    void deleteBlogById(Long id);

    

    

}
