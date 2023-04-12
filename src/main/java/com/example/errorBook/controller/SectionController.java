package com.example.errorBook.controller;

import com.example.errorBook.common.dto.IdListDto;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Section;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("/section")
public class SectionController {
    
    /**
     * 新增一个节
     * 需判断节的名称是否已存在
     * @param section
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody Section section){
        return null;
    }
    
    /**
     * 根据多个节id查询节
     * id为0则查询全部
     * @param chapterIds
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @GetMapping("/listSection")
    public Res listSection(@RequestBody IdListDto chapterIds){
        return null;
    }
    
    /**
     * 删除一个节
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
     * 修改节的章和名称
     * 需判断章是否存在
     * @param section
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PutMapping("/update")
    private Res update(@Validated @RequestBody Section section){
        return null;
    }
}

