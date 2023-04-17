package com.example.errorBook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.errorBook.common.dto.IdListDto;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Chapter;
import com.example.errorBook.entity.Section;
import com.example.errorBook.entity.Subject;
import com.example.errorBook.entity.User;
import com.example.errorBook.mapper.SectionMapper;
import com.example.errorBook.service.ChapterService;
import com.example.errorBook.service.SectionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController("/section")
public class SectionController {
    @Autowired
    SectionService sectionService;

    @Autowired
    ChapterService chapterService;
    
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

        String sectionName = section.getSectionName();
        Section newSection = sectionService.getOne(new LambdaQueryWrapper<Section>().eq(Section::getChapterId,section.getChapterId()).eq(Section::getSectionName, sectionName));


        if(newSection != null){
            return  Res.fail("该节已存在");
        }

        boolean sucToSave = sectionService.save(section);
        if(sucToSave) return Res.succ("添加成功");
        else return Res.fail("添加成败");
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
        Collection<Section> sections = sectionService.listByIds(Arrays.asList(chapterIds.getIds()));

        return Res.succ(sections);

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
        boolean sucToDel = sectionService.removeById(id);
        if(sucToDel) return Res.succ("删除成功");
        else return Res.fail("删除失败");
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

        Chapter chapter = chapterService.getById(section.getChapterId());
        if(chapter == null){
            return  Res.fail("该章节不存在");
        }
        boolean sucToUpd = sectionService.updateById(section);
        if(sucToUpd) return Res.succ("修改成功");
        else return Res.fail("修改失败");

    }
}

