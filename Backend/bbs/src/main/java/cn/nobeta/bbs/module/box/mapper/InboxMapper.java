package cn.nobeta.bbs.module.box.mapper;

import org.apache.ibatis.annotations.Mapper;

import cn.nobeta.bbs.module.box.entity.InboxEvent;

@Mapper
public interface InboxMapper {

    int insertIfLatest(InboxEvent event);
}
