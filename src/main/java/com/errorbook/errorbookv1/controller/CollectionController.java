package com.errorbook.errorbookv1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.errorbook.errorbookv1.common.dto.IdListDto;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Collection;
import com.errorbook.errorbookv1.service.CollectionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/collection")
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
    public Res insert(@RequestBody Collection collection) {
        Collection newCollection = collectionService.getOne(new LambdaQueryWrapper<Collection>().eq(Collection::getQuestionId, collection.getQuestionId()).eq(Collection::getUserId, collection.getUserId()));
        if (newCollection != null) {
            return Res.fail("该题目已存在");
        }
        boolean sucToSave = collectionService.save(collection);
        
        if (sucToSave) return Res.succ("收藏成功");
        else return Res.fail("收藏失败");
    }
    
    /**
     * 根据多个用户id查询各自收藏的题目
     * id为0则查询全部
     *
     * @param collectionIds
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PostMapping("/listCollection")
    public Res listCollection(@RequestBody IdListDto collectionIds) {
        if (collectionIds.getIds().length == 0) {
            return Res.succ(collectionService.list());
        }
        java.util.Collection<Collection> collections = collectionService.list(new LambdaQueryWrapper<Collection>().in(Collection::getUserId, Arrays.asList(collectionIds.getIds())));
        return Res.succ(collections);
    }
    
    /**
     * 删除一个收藏题目
     * @param userId
     * @param questionId 为0的话，删除该用户所有收藏的题目
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @DeleteMapping("/deleteById")
    public Res deleteById(@RequestParam Long userId,@RequestParam Long questionId) {
        boolean sucToDel = collectionService.remove(new LambdaQueryWrapper<Collection>().eq(Collection::getUserId,userId).eq(questionId!=0,Collection::getQuestionId,questionId));
        if (sucToDel) return Res.succ("成功取消收藏该题目");
        else return Res.fail("取消收藏失败");
    }
    
}