package com.errorbook.errorbookv1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.errorbook.errorbookv1.common.dto.IdListDto;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Chapter;
import com.errorbook.errorbookv1.entity.Section;
import com.errorbook.errorbookv1.service.ChapterService;
import com.errorbook.errorbookv1.service.SectionService;
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
@RequestMapping("/section")
public class SectionController {
    @Autowired
    SectionService sectionService;
    
    @Autowired
    ChapterService chapterService;
    
    /**
     * 新增一个节
     * 需判断节的名称是否已存在
     *
     * @param section
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody Section section) {
        
        Chapter chapter = chapterService.getById(section.getChapterId());
        if (chapter == null) {
            return Res.fail("该节的章不存在");
        }
        String sectionName = section.getSectionName();
        Section newSection = sectionService.getOne(new LambdaQueryWrapper<Section>()
                .eq(Section::getChapterId, section.getChapterId())
                .eq(Section::getSectionName, sectionName));
        
        
        if (newSection != null) {
            return Res.fail("该节已存在");
        }
        
        boolean sucToSave = sectionService.save(section);
        if (sucToSave) return Res.succ("添加成功");
        else return Res.fail("添加成败");
    }
    
    /**
     * 根据多个章id查询节
     * id为0则查询全部
     *
     * @param chapterIds
     * @return
     */
    @RequiresAuthentication
    //@RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PostMapping("/listSection")
    public Res listSection(@RequestBody IdListDto chapterIds) {
        if (chapterIds.getIds().length == 0) {
            return Res.succ(sectionService.list());
        }
        Collection<Section> sections = sectionService.list(new LambdaQueryWrapper<Section>().in(Section::getChapterId, Arrays.asList(chapterIds.getIds())));
        
        return Res.succ(sections);
        
    }
    
    /**
     * 删除一个节
     *
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @DeleteMapping("/deleteById")
    public Res deleteById(Long id) {
        boolean sucToDel = sectionService.removeById(id);
        if (sucToDel) return Res.succ("删除成功");
        else return Res.fail("删除失败");
    }
    
    /**
     * 修改节的章和名称
     * 需判断章是否存在
     *
     * @param section
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PutMapping("/update")
    public Res update(@RequestBody @Validated Section section) {
        log.info(String.valueOf(section));
        
        Chapter chapter = chapterService.getById(section.getChapterId());
        if (chapter == null) {
            return Res.fail("要修改的节的章不存在");
        }
        boolean sucToUpd = sectionService.updateById(section);
        if (sucToUpd) return Res.succ("修改成功");
        else return Res.fail("修改失败");
        
    }
}

