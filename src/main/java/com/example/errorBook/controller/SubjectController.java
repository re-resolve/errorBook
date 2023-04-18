package com.example.errorBook.controller;

import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Subject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/subject")
public class SubjectController {
    
    /**
     * 新增一个学科
     * 需判断学科的名称是否已存在
     * @param subject
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody Subject subject){
        return null;
    }
    
    /**
     * 查询所有学科
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @GetMapping("/listSubject")
    public Res listSubject(){
        return null;
    }
    
    /**
     * 删除一个学科
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
     * 修改学科的名称
     * @param subject
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PutMapping("/update")
    private Res update(@Validated @RequestBody Subject subject){
        return null;
    }
}
