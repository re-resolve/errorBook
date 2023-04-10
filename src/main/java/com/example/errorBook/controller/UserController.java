package com.example.errorBook.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Salt;
import com.example.errorBook.entity.User;
import com.example.errorBook.service.SaltService;
import com.example.errorBook.service.UserService;
import com.example.errorBook.shiro.GuestAccess;
import com.example.errorBook.util.JwtUtils;
import com.example.errorBook.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController("/user")
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
            
            String jwt = jwtUtils.generateToken(adminServiceOne.getId(), roleId);
            
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
     * 用户注册(学生或老师)
     *
     * @param user
     * @return
     */
    @GuestAccess
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody User user) {
        if (user.getRoleId() != 2 && user.getRoleId() != 3) {
            return Res.fail("请重新选择用户的角色（只能为学生或老师）");
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
        
        if (user.getRoleId() == 2) return Res.succ("成功创建学生用户");
        return Res.succ("成功创建老师用户");
    }
    
    /**
     * 新增管理员
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
        
        userService.updateById(user);
        
        return Res.succ("成功修改学生信息");
    }
    
    /**
     * 更新老师的信息
     *
     * @param user
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @PutMapping("/updateTeacher")
    public Res updateTeacher(@Validated @RequestBody User user) {
        if (user.getRoleId() != 3) {
            return Res.fail("请重新选择用户的角色（只能为老师）");
        }
        
        Long userId = user.getId();
        
        User newUser = userService.getById(userId);
        if (newUser == null) {
            return Res.fail("所要修改的老师不存在");
        }
        
        String password = user.getPassword();
        
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
        
        userService.updateById(user);
        
        return Res.succ("成功修改老师信息");
    }
}
