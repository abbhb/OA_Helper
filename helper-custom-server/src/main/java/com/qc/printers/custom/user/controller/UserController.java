package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.CASOauthUtil;
import com.qc.printers.common.common.utils.JWTUtil;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.user.domain.dto.LoginDTO;
import com.qc.printers.custom.user.domain.vo.request.PasswordR;
import com.qc.printers.custom.user.domain.vo.response.LoginRes;
import com.qc.printers.custom.user.domain.vo.response.UserResult;
import com.qc.printers.custom.user.service.RoleService;
import com.qc.printers.custom.user.service.TrLoginService;
import com.qc.printers.custom.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController//@ResponseBody+@Controller
@RequestMapping("/user")
@Api("和用户相关的接口")
@CrossOrigin("*")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    private RoleService roleService;

    private final TrLoginService trLoginService;
    private final CASOauthUtil casOauthUtil;


    public UserController(UserService userService, TrLoginService trLoginService, CASOauthUtil casOauthUtil) {
        this.userService = userService;
        this.trLoginService = trLoginService;
        this.casOauthUtil = casOauthUtil;
    }

    /**
     * 登录分为账密和Oauth2.0授权登录
     */
    @PostMapping("/login")
    @ApiOperation(value = "登录", notes = "")
    public R<LoginRes> login(@RequestBody LoginDTO user) {
        log.info("user:{}", user);
        /**
         * 对密码进行加密传输
         */
        String password = user.getPassword();
        if (StringUtils.isEmpty(password)) {
            return R.error("密码不能为空");
        }
        return userService.login(user);
    }

    /**
     * 2023-04-22 13:29:25 升级此接口为CAS认证
     *
     * @return
     */
    @PostMapping("/loginbycode")
    @ApiOperation(value = "登录", notes = "")
    public R<LoginRes> loginByCode(@RequestBody Map<String, Object> ticket) {
        log.info("ticket:{}", ticket);
        /**
         * 对密码进行加密传输
         */
        String code = (String) ticket.get("code");
        if (StringUtils.isEmpty(code)) {
            return R.error("认证失败");
        }
        return trLoginService.casLogin(code);
    }

    @NeedToken
    @PostMapping("/login_by_token")
    @ApiOperation(value = "token校验", notes = "没过期就data返回1告诉前端一声")
    public R<LoginRes> loginByToken() {

        return userService.loginByToken();
    }

    @NeedToken
    @PutMapping("/update")
    @ApiOperation(value = "用户更新自己信息", notes = "不是管理员更新接口")
    public R<String> update(@RequestBody User user) {
        boolean isTrue = userService.updateUserInfo(user);
        if (isTrue) {
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:update")
    @PutMapping("/updateByAdmin")
    @ApiOperation(value = "管理员更新用户信息", notes = "管理员更新接口")
    public R<String> updateByAdmin(@RequestBody UserResult user) {
        log.info("user:{}", user);
        boolean isTrue = userService.updateByAdmin(user);
        if (isTrue) {
            return R.successOnlyObject("修改成功");
        }
        return R.error("修改失败");
    }

    @NeedToken
    @GetMapping("/updateStatus")
    @ApiOperation(value = "更新用户的状态", notes = "成功或者失败告诉前端")
    public R<String> updateStatus(String id, String status) {
        boolean isTrue = userService.updateUserStatus(id, status);
        if (isTrue) {
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    @NeedToken
    @PutMapping("/setPassword")
    public R<String> setPassword(@RequestBody PasswordR passwordR) {
        boolean isTrue = userService.setPassword(passwordR);
        if (isTrue) {
            return R.success("修改成功,下次登录生效!");
        }
        return R.error("修改失败");
    }

    @NeedToken
    @PostMapping("/info")
    @ApiOperation(value = "获取用户信息", notes = "")
    public R<UserResult> info() {
        log.info("获取用户信息");
        return userService.info();
    }

    @PostMapping("/logout")
    @ApiOperation(value = "退出登录", notes = "")
    public R<String> logout(HttpServletRequest request) {
        log.info("退出登录");
        final String authHeader = request.getHeader(JWTUtil.AUTH_HEADER_KEY);
        log.info("## authHeader= {}", authHeader);
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JWTUtil.TOKEN_PREFIX)) {
            log.info("### 用户未登录，请先登录 ###");
            throw new CustomException("请先登录!", Code.DEL_TOKEN);
        }
        // 获取token
        final String token = authHeader.substring(7);
        return userService.logout(token);
    }

    @GetMapping("/user_count")
    @ApiOperation(value = "获取用户数量", notes = "")
    public R<Integer> userCount() {
        log.info("获取用户数量");
        return R.success(userService.count());
    }

    @GetMapping("/role_name")
    @NeedToken
    @ApiOperation(value = "获取角色的name", notes = "")
    public R<List<String>> getroleNameByKey(@RequestParam(required = true, name = "role_key") String key) {
        log.info("获取角色的name");
        return R.success(roleService.getroleNameByKey(key));
    }

    @GetMapping("/user_manger")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:list")
    @ApiOperation(value = "用户管理获取所有用户", notes = "")
    public R<PageData<UserResult>> userManger(Integer pageNum, Integer pageSize, @RequestParam(required = false, name = "name") String name, @RequestParam(required = false, name = "deptId") Long deptId) {
        log.info("用户管理获取所有用户");
        return R.success(userService.getUserList(pageNum, pageSize, name, deptId));
    }


    @GetMapping("/user_password")
    @NeedToken
    @ApiOperation(value = "是否需要输入密码", notes = "")
    public R<Integer> userPassword() {
        log.info("是否需要输入密码");
        return R.success(userService.userPassword());
    }


}
