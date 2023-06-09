package com.errorbook.errorbookv1.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 自定义元数据对象处理器
 */
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        
        if (metaObject.hasSetter("createTime")) {
            log.info("公共字段自动填充[insert]...");
            log.info(metaObject.toString());
            metaObject.setValue("createTime", System.currentTimeMillis());
        }
        if (metaObject.hasSetter("updateTime")) {
            log.info("公共字段自动填充[insert]...");
            log.info(metaObject.toString());
            metaObject.setValue("updateTime", System.currentTimeMillis());
        }
    }
    
    /**
     * 更新操作，自动填充
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        
        if (metaObject.hasSetter("updateTime")) {
            log.info("公共字段自动填充[update]...");
            log.info(metaObject.toString());
    
            Long id = Thread.currentThread().getId();
            log.info("线程id为：{}", id);
            metaObject.setValue("updateTime", System.currentTimeMillis());
        }
        
    }
    
}
