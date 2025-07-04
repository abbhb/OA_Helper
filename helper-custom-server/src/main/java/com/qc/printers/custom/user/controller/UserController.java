package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.FrequencyControl;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.CASOauthUtil;
import com.qc.printers.common.common.utils.JWTUtil;
import com.qc.printers.common.common.utils.poi.ExcelUtil;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.email.service.EmailService;
import com.qc.printers.common.signin.domain.dto.SigninUserDataExcelDto;
import com.qc.printers.common.user.domain.dto.SummeryInfoDTO;
import com.qc.printers.common.user.domain.dto.UserInfoBaseExtDto;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.entity.UserExtBase;
import com.qc.printers.common.user.domain.vo.request.user.SummeryInfoReq;
import com.qc.printers.common.user.domain.vo.response.user.UserInfoBaseExtStateResp;
import com.qc.printers.common.user.domain.vo.response.user.UserVerificationStateResp;
import com.qc.printers.common.user.service.annotation.UserPermissionGradeCheck;
import com.qc.printers.common.vailcode.annotations.CheckVailCode;
import com.qc.printers.common.vailcode.domain.enums.VailType;
import com.qc.printers.custom.user.domain.dto.LoginDTO;
import com.qc.printers.custom.user.domain.vo.request.*;
import com.qc.printers.custom.user.domain.vo.response.*;
import com.qc.printers.custom.user.service.RoleService;
import com.qc.printers.custom.user.service.TrLoginService;
import com.qc.printers.custom.user.service.UserService;
import com.qc.printers.custom.user.service.strategy.ThirdLoginHandel;
import com.qc.printers.custom.user.service.strategy.ThirdLoginHandelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
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
    @Autowired
    private MinIoProperties minIoProperties;

    private final TrLoginService trLoginService;
    private final CASOauthUtil casOauthUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ThirdLoginHandelFactory thirdLoginHandelFactory;


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
     * @param email
     * @param vailCode 用于验证码注解验证,如果没开启验证码为空
     * @return 提示信息
     */
    @GetMapping("/get_email_code")
    @ApiOperation(value = "获取邮箱验证码", notes = "")
    @CheckVailCode(key = "#vailCode")
    @FrequencyControl(time = 10, count = 5, target = FrequencyControl.Target.EL, spEl = "#email")
    @FrequencyControl(time = 30, count = 10, target = FrequencyControl.Target.EL, spEl = "#email")
    @FrequencyControl(time = 60, count = 15, target = FrequencyControl.Target.EL, spEl = "#email")
    public R<String> getEmailCode(@RequestParam(name = "email") String email, @RequestParam(name = "vail_code", required = false) String vailCode) {
        if (StringUtils.isEmpty(email)) {
            return R.error("邮箱不能为空");
        }
        return R.successOnlyObject(emailService.getEmailCode(email));
    }


    @PostMapping("/register_email")
    @CheckVailCode(key = "#registerEmailRes.email", value = "#registerEmailRes.emailCode", type = VailType.EMAIL)
    @ApiOperation(value = "Email注册", notes = "")
    public R<RegisterResp> registerByEmail(@RequestBody RegisterEmailRes registerEmailRes) {
        /**
         * 直接注册就行，校验邮箱验证码通过验证码通用注解
         */
        return R.success(userService.emailRegister(registerEmailRes.getEmail(), registerEmailRes.getPassword()));
    }
    @PostMapping("/reset_password")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:reset")
    @ApiOperation(value = "重置密码", notes = "")
    public R<ResetResp> resetPassword(@RequestBody ResetReq resetReq) {
        /**
         * 直接注册就行，校验邮箱验证码通过验证码通用注解
         */
        return R.success(userService.resetPassword(resetReq));
    }


    @PostMapping("/login_by_email_code")
    @CheckVailCode(key = "#loginByEmailCodeReq.email", value = "#loginByEmailCodeReq.emailCode", type = VailType.EMAIL)
    @ApiOperation(value = "Email一键登录", notes = "如果没注册会一键注册")
    public R<LoginRes> loginByEmailCode(@RequestBody LoginByEmailCodeReq loginByEmailCodeReq) {
        /**
         * 直接注册就行，校验邮箱验证码通过验证码通用注解
         */
        return R.success(userService.loginByEmailCode(loginByEmailCodeReq));
    }

    //todo:注意注册邮箱的唯一性
    @PostMapping("/forget_password_email")
    @CheckVailCode(key = "#forgetPasswordEmailRes.email", value = "#forgetPasswordEmailRes.emailCode", type = VailType.EMAIL)
    @ApiOperation(value = "找回密码", notes = "")
    public R<ForgetPasswordResp> forgetPasswordByEmail(@RequestBody ForgetPasswordEmailRes forgetPasswordEmailRes) {
        /**
         * 直接注册就行，校验邮箱验证码通过验证码通用注解
         */
        return R.success(userService.forgetPasswordByEmail(forgetPasswordEmailRes.getEmail(), forgetPasswordEmailRes.getPassword()));
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

    @GetMapping("/third_login")
    @ApiOperation(value = "第三方oauth认证", notes = "目前使用的水滴聚合，这块第三方授权也需要绑定唯一的电子邮箱，防止之后换第三方oauth平台导致原账号数据丢失")
    public R<String> thirdLogin(String type, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(type)) {
            throw new CustomException("type异常");
        }
        ThirdLoginHandel instance = thirdLoginHandelFactory.getInstance(type);
        if (instance == null) {
            throw new CustomException("type不存在");
        }
        return R.successOnlyObject(instance.thirdLoginHandel(request, response));
    }

    /**
     * 水滴聚合登录回调
     *
     * @param type
     * @param code
     * @param state 水滴聚合官方没使用state，所以此字段为空
     * @return
     */
    @GetMapping("/uni_callback")
    @ApiOperation(value = "第三方oauth认证回调", notes = "目前使用的水滴聚合，这块第三方授权也需要绑定唯一的电子邮箱，防止之后换第三方oauth平台导致原账号数据丢失")
    public R<ThirdCallbackResp> uniCallback(String type, String code, String state) {
        if (StringUtils.isEmpty(type)) {
            throw new CustomException("type异常");
        }
        if (StringUtils.isEmpty(code)) {
            throw new CustomException("code异常");
        }
        return R.success(trLoginService.uniCallback(type, code));
    }


    /**
     * 不用手动校验email验证码，验证码校验通过注解
     *
     * @param thirdFirstLoginReq
     * @return
     */
    @CheckVailCode(key = "#thirdFirstLoginReq.email", value = "#thirdFirstLoginReq.emailCode", type = VailType.EMAIL)
    @PostMapping("/uni_first_login")
    @ApiOperation(value = "第三方oauth认证回调首次登录绑定", notes = "第三方oauth认证回调首次登录绑定，通过电子邮箱验证码来校验，如果该邮箱验证码正确且已绑定就直接登录，否则就一键创建新用户！")
    public R<LoginRes> uniFirstLogin(@RequestBody ThirdFirstLoginReq thirdFirstLoginReq) {
        return R.success(trLoginService.uniFirstLogin(thirdFirstLoginReq));
    }

    /**
     * 通用人员选择器，不返回敏感信息
     * @param pageNum
     * @param pageSize
     * @param name
     * @param deptId
     * @return
     */
    @GetMapping("/listForSelect")
    @NeedToken
//    @PermissionCheck(role = {"superadmin"}, permission = "sys:bpm:add")
    public R<PageData<UserResult>> listForSelect(
            Integer pageNum,
            Integer pageSize,
            @RequestParam(required = false) String name
            , @RequestParam(required = false, name = "mustHaveStudentId") Integer mustHaveStudentId
            , @RequestParam(required = false, name = "cascade", defaultValue = "0") Integer cascade //必须和deptId同时出现才有用 0：不级联
            , @RequestParam(required = false, name = "deptId") Long deptId
            , @RequestParam(required = false, name = "level") String level

    ) {
        return R.success(userService.getUserListForBpm(
                pageNum,
                pageSize,
                name,
                deptId,
                cascade,
                mustHaveStudentId,
                level
        ));
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
    @UserPermissionGradeCheck(checkUserId = "#user.id")
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
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:update")
    @UserPermissionGradeCheck(checkUserId = "#id")
    @GetMapping("/updateStatus")
    @ApiOperation(value = "更新用户的状态", notes = "成功或者失败告诉前端")
    public R<String> updateStatus(String id, String status) {
        boolean isTrue = userService.updateUserStatus(id, status);
        if (isTrue) {
            return R.successOnlyObject("修改成功");
        }
        return R.successOnlyObject("修改失败");
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

    @CrossOrigin("*")
    @PostMapping("/setPasswordByOneTimeCodeReq")
    public R<String> setPasswordByOneTimeCodeReq(@RequestBody PasswordByOneTimeCodeReq passwordByOneTimeCodeReq) {
        boolean isTrue = userService.setPasswordByOneTimeCodeReq(passwordByOneTimeCodeReq);
        if (isTrue) {
            return R.successOnlyObject("设置成功");
        }
        return R.error("设置失败");
    }

    @CrossOrigin("*")
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

    /**
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @param mustHaveStudentId
     * @param cascade
     * @param deptId
     * @param level 单个就直接模糊查询，多个都好分割in
     * @return
     */
    @GetMapping("/user_manger")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:list")
    @ApiOperation(value = "用户管理获取所有用户", notes = "")
    public R<PageData<UserResult>> userManger(Integer pageNum
            , Integer pageSize
            , @RequestParam(required = false, name = "name") String name
            , @RequestParam(required = false, name = "mustHaveStudentId") Integer mustHaveStudentId
            , @RequestParam(required = false, name = "cascade", defaultValue = "0") Integer cascade
            , @RequestParam(required = false, name = "deptId") Long deptId
            , @RequestParam(required = false, name = "level") String level
    ) {
        log.info("用户管理获取所有用户");
        return R.success(userService.getUserList(new User(),pageNum, pageSize, name,mustHaveStudentId, cascade, deptId,level));
    }


    @GetMapping("/user_password")
    @NeedToken
    @ApiOperation(value = "是否需要输入密码", notes = "")
    public R<Integer> userPassword() {
        log.info("是否需要输入密码");
        return R.success(userService.userPassword());
    }

    @PostMapping("/public/summary/userInfo/batch")
    @ApiOperation("用户聚合信息-返回的代表需要刷新的")
    public R<List<SummeryInfoDTO>> getSummeryUserInfo(@Valid @RequestBody SummeryInfoReq req) {
        return R.success(userService.getSummeryUserInfo(req));
    }

    @NeedToken
    @FrequencyControl(time = 2, count = 1, target = FrequencyControl.Target.UID)
    @FrequencyControl(time = 60, count = 30, target = FrequencyControl.Target.UID)
    @FrequencyControl(time = 180, count = 60, target = FrequencyControl.Target.UID)
    @ApiOperation(value = "筛选用户列表", notes = "")
    @GetMapping("/user_select_list")
    public R<UserSelectListResp> userSelectList(String name) {
        if (StringUtils.isEmpty(name)) {
            UserSelectListResp userSelectListResp = new UserSelectListResp();
            userSelectListResp.setOptions(new ArrayList<>());
            return R.successOnlyObject(userSelectListResp);
        }
        return R.successOnlyObject(userService.userSelectList(name));
    }


    @NeedToken
    @ApiOperation(value = "只返回所传部门id以及下级部门的所有用户", notes = "")
    @GetMapping("/user_select_list_only_x_user")
    public R<UserSelectListResp> userSelectOnlyXUserList(Long deptId) {
        if (deptId == null) {
            throw new CustomException("请传入部门id");
        }
        return R.successOnlyObject(userService.userSelectOnlyXUserList(deptId));
    }

    @NeedToken
    @ApiOperation(value = "返回需要审核修改的个人信息_ext", notes = "")
    @GetMapping("/userinfo_ext_my")
    public R<UserInfoBaseExtStateResp> userinfoExtMy() {
        return R.successOnlyObject(userService.userinfoExtMy());
    }

    @NeedToken
    @ApiOperation(value = "返回实名认证状态", notes = "")
    @GetMapping("/user_verification_state")
    public R<UserVerificationStateResp> getUserVerificationState() {
        return R.successOnlyObject(userService.getUserVerificationState());
    }
    @NeedToken
    @ApiOperation(value = "修改个人信息_ext", notes = "")
    @PostMapping("/userinfo_ext_my_apply_for")
    public R<String> userinfoExtMyApplyFor(@Valid @RequestBody UserInfoBaseExtDto userExtBase) {
        return R.success(userService.userinfoExtMyApplyFor(userExtBase));
    }

    @NeedToken
    @ApiOperation(value = "撤回个人信息_ext修改申请", notes = "")
    @PostMapping("/userinfo_ext_my_withdraw")
    public R<String> userinfoExtMyWithDraw() {
        return R.success(userService.userinfoExtMyWithDraw());
    }

    @NeedToken
    @ApiOperation(value = "返回需要审核修改的个人信息_ext", notes = "")
    @GetMapping("/approval_userinfo_ext_data/{taskId}")
    public R<UserInfoBaseExtDto> approvalUserinfoExtData(@PathVariable String taskId) {
        return R.successOnlyObject(userService.approvalUserinfoExtData(taskId));
    }



    @NeedToken
    @ApiOperation(value = "返回所有级，用于筛选", notes = "")
    @GetMapping("/levels")
    public R<List<String>> levels() {
        return R.successOnlyObject(userService.levels());
    }



    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:export")
    @PostMapping("/export")
    @ApiOperation(value = "导出数据", notes = "")
    public R<String> export()
    {
        List<User> list = userService.exportAllData();
        ExcelUtil<User> util = new ExcelUtil<User>(User.class,minIoProperties.getBucketName());
        return util.exportExcel(list, "生物数据");
    }

    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:import")
    @PostMapping("/importData")
    @ApiOperation(value = "导入数据", notes = "")
    public R<String> importData(MultipartFile file) throws Exception
    {
        ExcelUtil<User> util = new ExcelUtil<User>(User.class,minIoProperties.getBucketName());
        List<User> dataList = util.importExcel(file.getInputStream());
        String s = userService.importUserData(dataList);
        log.info("dattas{}",dataList);

        return R.successOnlyObject(s);
    }
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:user:import")
    @GetMapping("/importTemplate")
    @ApiOperation(value = "导入模板", notes = "")
    public R<String> importTemplate()
    {
        ExcelUtil<User> util = new ExcelUtil<User>(User.class,minIoProperties.getBucketName());
        return util.importTemplateExcel("用户数据");
    }
}
