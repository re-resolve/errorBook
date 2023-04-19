package com.errorbook.errorbookv1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Subject;
import com.errorbook.errorbookv1.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/subject")
public class SubjectController {
    
    @Autowired
    SubjectService subjectService;
    
    /**
     * 新增一个学科
     * 需判断学科的名称是否已存在
     *
     * @param subject
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody Subject subject) {
        String subjectName = subject.getSubjectName();
        
        Subject newSubject = subjectService.getOne(new LambdaQueryWrapper<Subject>().eq(Subject::getSubjectName, subjectName));
        if (newSubject != null) {
            return Res.fail("该学科已存在");
        }
        
        boolean sucToSave = subjectService.save(subject);
        if (sucToSave) return Res.succ("添加成功");
        else return Res.fail("添加成败");
    }
    
    /**
     * 查询所有学科
     *
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @GetMapping("/listSubject")
    public Res listSubject() {
        List<Subject> subjects = subjectService.list();
        return Res.succ(subjects);
    }
    
    /**
     * 删除一个学科
     *
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @DeleteMapping("/deleteById")
    public Res deleteById(Long id) {
        boolean sucToDel = subjectService.removeById(id);
        if (sucToDel) return Res.succ("删除成功");
        else return Res.fail("删除失败");
    }
    
    /**
     * 修改学科的名称
     *
     * @param subject
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PutMapping("/update")
    private Res update(@Validated @RequestBody Subject subject) {
        
        boolean sucToUpd = subjectService.updateById(subject);
        if (sucToUpd) return Res.succ("修改成功");
        else return Res.fail("修改失败");
        
    }
    
}
