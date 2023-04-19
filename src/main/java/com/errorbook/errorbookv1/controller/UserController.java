package com.errorbook.errorbookv1.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Salt;
import com.errorbook.errorbookv1.entity.User;
import com.errorbook.errorbookv1.service.SaltService;
import com.errorbook.errorbookv1.service.UserService;
import com.errorbook.errorbookv1.shiro.GuestAccess;
import com.errorbook.errorbookv1.util.JwtUtils;
import com.errorbook.errorbookv1.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SaltService saltService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 用户登录
     *
     * @param user
     * @param response
     * @return
     */
    @GuestAccess
    @PostMapping("/loginByPwd")
    public Res login(@Validated @RequestBody User user, HttpServletResponse response) {
        
        String account = user.getAccount();
        String password = user.getPassword();
        
        User adminServiceOne = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
        
        if (adminServiceOne != null) {
            
            Salt saltEntity = saltService.getOne(new LambdaQueryWrapper<Salt>().eq(Salt::getAccount, account));
            
            String salt = saltEntity.getSaltValue();
            
            String md5Pwd = PasswordUtil.getMD5Encryption(password, salt);
            
            if (!adminServiceOne.getPassword().equals(md5Pwd)) {
                return Res.fail("密码不正确");
            }
            Long roleId = adminServiceOne.getRoleId();
            
            String jwt = jwtUtils.generateToken(adminServiceOne.getAccount(), roleId);
            
            response.setHeader("Authorization", jwt);
            
            response.setHeader("Access-control-Expose-Headers", "Authorization");
            String k = "student";
            
            if (roleId == 1) {
                k = "admin";
            } else if (roleId == 2) {
                k = "student";
            } else {
                k = "teacher";
            }
            
            return Res.succ(MapUtil.builder()
                    .put("token", jwt)
                    .put("refreshToken", jwt)
                    .put(k, jwt)
                    .put("userId", adminServiceOne.getId())
                    .map()
            );
        }
        
        Assert.notNull("用户不存在");
        return Res.fail("用户不存在");
    }
    
    /**
     * 用户注册
     * 需要判断账户是否存在，存在则注册失败
     *
     * @param user
     * @return
     */
    @GuestAccess
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody User user) {
        if (user.getRoleId() != 2) {
            return Res.fail("请重新选择用户的角色（只能为学生）");
        }
        String account = user.getAccount();
        
        //判断创建的用户是否已存在
        User newUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
        
        if (newUser != null) {
            return Res.fail("该账户已存在，请更改账户");
        }
        
        String password = user.getPassword();
        
        String salt = PasswordUtil.getSalt();
        //保存此随机盐
        Salt saltEntity = new Salt();
        
        saltEntity.setAccount(account);
        
        saltEntity.setSaltValue(salt);
        
        saltService.save(saltEntity);
        //保存用户信息
        String md5Pwd = PasswordUtil.getMD5Encryption(password, salt);
        
        user.setPassword(md5Pwd);
        
        userService.save(user);
        
        return Res.succ("成功创建用户");
    }
    
    /**
     * 新增管理员
     * 需要判断账户是否存在，存在则新增失败
     *
     * @param user
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PostMapping("/createAdmin")
    public Res createAdmin(@Validated @RequestBody User user) {
        if (user.getRoleId() != 1) {
            return Res.fail("请重新选择用户的角色（只能为管理员）");
        }
        String account = user.getAccount();
        
        //判断创建的管理员是否已存在
        User newUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
        
        if (newUser != null) {
            return Res.fail("该账户已存在，请更改账户");
        }
        
        String password = user.getPassword();
        
        String salt = PasswordUtil.getSalt();
        //保存此随机盐
        Salt saltEntity = new Salt();
        
        saltEntity.setAccount(account);
        
        saltEntity.setSaltValue(salt);
        
        saltService.save(saltEntity);
        //保存用户信息
        String md5Pwd = PasswordUtil.getMD5Encryption(password, salt);
        
        user.setPassword(md5Pwd);
        
        userService.save(user);
        
        return Res.succ("成功创建管理员");
    }
    
    /**
     * 更新学生的信息
     * 密码为空或只有空格则不改密码
     *
     * @param user
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"学生", "管理员"}, logical = Logical.OR)
    @PutMapping("/updateStudent")
    public Res updateStudent(@Validated @RequestBody User user) {
        if (user.getRoleId() != 2) {
            return Res.fail("请重新选择用户的角色（只能为学生）");
        }
        
        Long userId = user.getId();
        
        User newUser = userService.getById(userId);
        if (newUser == null) {
            return Res.fail("所要修改的学生不存在");
        }
        
        String password = user.getPassword();
        if (StringUtils.isNotBlank(password)) {//改密码
            String salt = PasswordUtil.getSalt();
            Salt saltEntity = saltService.getOne(new LambdaQueryWrapper<Salt>().eq(Salt::getAccount, newUser.getAccount()));
            if (saltEntity == null) {
                return Res.fail("所要修改的学生的密码对应的盐值不存在...");
            }
            //保存此随机盐
            
            saltEntity.setAccount(user.getAccount());
            
            saltEntity.setSaltValue(salt);
            
            saltService.updateById(saltEntity);
            //保存用户信息
            String md5Pwd = PasswordUtil.getMD5Encryption(password, salt);
            
            user.setPassword(md5Pwd);
        } else {
            user.setPassword(newUser.getPassword());
        }
        
        userService.updateById(user);
        
        return Res.succ("成功修改学生信息");
    }
    
    /**
     * 更新老师的信息
     * 密码为空或只有空格则不改密码
     *
     * @param user
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PutMapping("/updateTeacher")
    public Res updateTeacher(@Validated @RequestBody User user) {
        
        Long userId = user.getId();
        
        User newUser = userService.getById(userId);
        if (newUser == null) {
            return Res.fail("所要修改的用户不存在");
        }
        
        String password = user.getPassword();

        if (StringUtils.isNotBlank(password)) {//改密码
            String salt = PasswordUtil.getSalt();
            Salt saltEntity = saltService.getOne(new LambdaQueryWrapper<Salt>().eq(Salt::getAccount, newUser.getAccount()));
            if (saltEntity == null) {
                return Res.fail("所要修改的老师的密码对应的盐值不存在...");
            }
            //保存此随机盐
        
            saltEntity.setAccount(user.getAccount());
        
            saltEntity.setSaltValue(salt);
        
            saltService.updateById(saltEntity);
            //保存用户信息
            String md5Pwd = PasswordUtil.getMD5Encryption(password, salt);
        
            user.setPassword(md5Pwd);
        } else {
            user.setPassword(newUser.getPassword());
        }
        
        userService.updateById(user);
        
        return Res.succ("成功修改老师信息");
    }
    
    /**
     * 分页+账户模糊查询用户信息
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param roleId 角色id（非1，2，3则查询全部）
     * @param account 账户为空则查询全部
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PostMapping("/pageUser")
    public Res pageUser(@RequestParam int page, @RequestParam int pageSize, @RequestParam int roleId,  String account) {
        log.info("分页+账户模糊查询用户信息");
        Page<User> userIPage = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(roleId == 1 || roleId == 2 || roleId == 3, User::getRoleId, roleId)
                .like(StringUtils.isNotBlank(account), User::getAccount, account);
        
        userService.page(userIPage, queryWrapper);
        
        return Res.succ(userIPage);
        
    }
    
}
