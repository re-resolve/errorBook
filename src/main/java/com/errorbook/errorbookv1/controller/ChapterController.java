package com.errorbook.errorbookv1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.errorbook.errorbookv1.common.dto.IdListDto;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Chapter;
import com.errorbook.errorbookv1.entity.Subject;
import com.errorbook.errorbookv1.service.ChapterService;
import com.errorbook.errorbookv1.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/chapter")
public class ChapterController {
    @Autowired
    ChapterService chapterService;
    
    @Autowired
    SubjectService subjectService;
    
    /**
     * 新增一个章
     * 需判断章的名称是否已存在
     *
     * @param chapter
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody Chapter chapter) {
        Subject subject = subjectService.getById(chapter.getSubjectId());
        if (subject == null) {
            return Res.fail("该章的学科不存在");
        }
        
        String chapterName = chapter.getChapterName();
        Chapter newChapter = chapterService.getOne(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getSubjectId, chapter.getSubjectId())
                .eq(Chapter::getChapterName, chapterName));
        
        
        if (newChapter != null) {
            return Res.fail("该章已存在");
        }
        
        boolean sucToSave = chapterService.save(chapter);
        if (sucToSave) return Res.succ("添加成功");
        else return Res.fail("添加成败");
    }
    
    /**
     * 根据多个学科id查询章
     * id为0则查询全部
     *
     * @param subjectIds
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PostMapping("/listChapter")
    public Res listChapter(@RequestBody IdListDto subjectIds) {
        if (subjectIds.getIds().length == 0) {
            return Res.succ(chapterService.list());
        }
        Collection<Chapter> chapters = chapterService.list(new LambdaQueryWrapper<Chapter>().in(Chapter::getSubjectId, Arrays.asList(subjectIds.getIds())));
        return Res.succ(chapters);
    }
    
    /**
     * 删除一个章
     *
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @DeleteMapping("/deleteById")
    public Res deleteById(Long id) {
        boolean sucToDel = chapterService.removeById(id);
        if (sucToDel) return Res.succ("删除成功");
        else return Res.fail("删除失败");
    }
    
    /**
     * 修改章的学科和名称
     * 需判断学科是否存在
     *
     * @param chapter
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PutMapping("/update")
    public Res update(@Validated @RequestBody Chapter chapter) {
        
        Subject subject = subjectService.getById(chapter.getSubjectId());
        
        if (subject == null) {
            return Res.fail("要修改的章的学科不存在");
        }
        boolean sucToUpd = chapterService.updateById(chapter);
        if (sucToUpd) return Res.succ("修改成功");
        else return Res.fail("修改失败");
        
    }
}
