package com.example.errorBook.controller;

import com.example.errorBook.common.dto.IdListDto;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Chapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("/chapter")
public class ChapterController {
    
    /**
     * 新增一个章
     * @param chapter
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody Chapter chapter){
        return null;
    }
    
    /**
     * 根据多个学科id查询章
     * id为0则查询全部
     * @param subjectIds
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @GetMapping("/listChapter")
    public Res listChapter(@RequestBody IdListDto subjectIds){
        return null;
    }
    
    /**
     * 删除一个章
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @DeleteMapping ("/deleteById")
    public Res deleteById(Long id){
        return null;
    }
    
    /**
     * 修改章的学科和名称
     * 需判断学科是否存在
     * @param chapter
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PutMapping("/update")
    private Res update(@Validated @RequestBody Chapter chapter){
        return null;
    }
}
