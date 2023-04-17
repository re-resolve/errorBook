package com.example.errorBook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.errorBook.common.dto.IdListDto;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Collection;
import com.example.errorBook.entity.Subject;
import com.example.errorBook.entity.User;
import com.example.errorBook.service.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController("/collection")
public class CollectionController {

    @Autowired
    CollectionService collectionService;
    
    /**
     * 新增一个收藏题目
     *
     * @param collection
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/insert")
    public Res insert(@RequestBody Collection collection){
        Collection newCollection = collectionService.getOne(new LambdaQueryWrapper<Collection>().eq(Collection::getQuestionId, collection.getQuestionId()).eq(Collection::getUserId, collection.getUserId()));
        if(newCollection != null){
            Res.fail("该题目已存在");
        }
        boolean sucToSave = collectionService.save(collection);

        if(sucToSave)return Res.succ("收藏成功");
        else return Res.fail("收藏失败");
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
        if(collectionIds.getIds().length == 0){
            return Res.succ(collectionService.list());
        }
        java.util.Collection<Collection> collections = collectionService.listByIds(Arrays.asList(collectionIds.getIds()));
        return Res.succ(collections);
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
        boolean sucToDel = collectionService.removeById(id);
        if(sucToDel)return Res.succ("成功删除题目");
        else return Res.fail("删除失败");
    }
    
}