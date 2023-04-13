package com.example.errorBook.controller;

import com.example.errorBook.common.dto.IdListDto;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("/collection")
public class CollectionController {
    
    /**
     * 新增一个收藏题目
     *
     * @param collection
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/insert")
    public Res insert(@RequestBody Collection collection){
        return null;
    }
    
    /**
     * 根据多个用户id查询题目
     * id为0则查询全部
     * @param collectionIds
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @GetMapping("/listCollection")
    public Res listCollection(@RequestBody IdListDto collectionIds){
        return null;
    }
    
    /**
     * 删除一个收藏题目
     * @param id
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @DeleteMapping("/deleteById")
    public Res deleteById(Long id){
        return null;
    }
    
}