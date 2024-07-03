package com.qc.printers.custom.user.service.impl;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.DataScope;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.utils.*;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.email.service.EmailService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.SummeryInfoDTO;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.SysUserRole;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.vo.request.user.SummeryInfoReq;
import com.qc.printers.common.user.service.*;
import com.qc.printers.common.user.service.cache.UserCache;
import com.qc.printers.custom.user.domain.dto.LoginDTO;
import com.qc.printers.custom.user.domain.vo.request.LoginByEmailCodeReq;
import com.qc.printers.custom.user.domain.vo.request.PasswordByOneTimeCodeReq;
import com.qc.printers.custom.user.domain.vo.request.PasswordR;
import com.qc.printers.custom.user.domain.vo.response.*;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.custom.user.service.DeptService;
import com.qc.printers.custom.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qc.printers.common.common.utils.ParamsCalibration.checkSensitiveWords;


@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final RestTemplate restTemplate;
    @Autowired
    private UserDao userDao;
    @Autowired
    private IUserService iUserService;

    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private UserCache userCache;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ISysUserRoleService iSysUserRoleService;

    @Autowired
    private DeptService deptService;

    @Autowired
    private ISysRoleService iSysRoleService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ITrLoginService iTrLoginService;

    @Autowired
    public UserServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


//    @Transactional
//    @Override
//    public R<UserResult> login(String code) {
//        if (code==null||code.equals("")){
//            throw new CustomException("认证失败",Code.DEL_TOKEN);
//        }
//        Token tokenByCode = casOauthUtil.getTokenByCode(restTemplate, code);
//        if (tokenByCode==null){
//            throw new CustomException("认证失败",Code.DEL_TOKEN);
//        }
//        JSONObject userObjectByToken = casOauthUtil.getUserObjectByToken(restTemplate, tokenByCode);
//        if (userObjectByToken==null){
//            throw new CustomException("认证失败",Code.DEL_TOKEN);
//        }
//        String openid = userObjectByToken.getString("openid");
//
//        if (userByToken==null){
//            throw new CustomException("认证失败",Code.DEL_TOKEN);
//        }
//        if (userByToken.getId()==null||StringUtils.isEmpty(userByToken.getName())||StringUtils.isEmpty(userByToken.getUsername())){
//            throw new CustomException("认证失败",Code.DEL_TOKEN);
//        }
//        log.info("userdata={}",userByToken);
//        if(userByToken.getStatus() == 0){
//            throw new CustomException("账号已禁用!");
//        }
//        UserResult userResult = new UserResult(String.valueOf(userByToken.getId()),userByToken.getUsername(),userByToken.getName(),userByToken.getPhone(),userByToken.getSex(),String.valueOf(userByToken.getStudentId()),userByToken.getStatus(),userByToken.getCreateTime(),userByToken.getUpdateTime(),userByToken.getPermission(),userByToken.getPermissionName(),tokenByCode.getAccessToken(),tokenByCode.getRefreshToken(),userByToken.getEmail(),userByToken.getAvatar());
//        return R.success(userResult);
//
//    }

//    @Transactional
//    @Override
//    public R<UserResult> loginFirst(User user) {
//        if (user==null){
//            throw new CustomException("认证失败");
//        }
//        if (user.getId()==null||StringUtils.isEmpty(user.getUsername())||StringUtils.isEmpty(user.getName())){
//            throw new CustomException("认证失败");
//        }
//        if (StringUtils.isEmpty(user.getSex())){
//            user.setSex("男");
//        }
//        if (user.getPermission()==null){
//            //默认给用户
//            user.setPermission(2);
//        }
//        if (user.getStatus()==null){
//            user.setStatus(1);
//        }
//        //此处当系统管理员创建
//        if (StringUtils.isEmpty(user.getName())){
//            throw new CustomException("yichang");
//        }
//
//        if (StringUtils.isEmpty(user.getUsername())){
//            throw new CustomException("yichang");
//        }
//        if (user.getUsername().contains("@")){
//            throw new CustomException("不可包含'@'");
//        }
//        checkSensitiveWords(user.getName());
//        boolean save = userDao.save(user);
//        if (!save){
//            throw new CustomException("认证失败");
//        }
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(User::getId,Long.valueOf(user.getId()));
//        User one = userDao.getOne(queryWrapper);
//        if (one==null){
//            throw new CustomException("登录业务异常!");
//        }
//        if(one.getStatus() == 0){
//            throw new CustomException("账号已禁用!");
//        }
//        UserResult userResult = ParamsCalibration.loginUtil1(one, iRedisService);
//        return R.success(userResult);
//    }


    @Transactional
    @Override
    public R<String> createUser(User user, Long userId) {
        if (user.getDeptId() == null) {
            throw new CustomException("yichang");
        }
        if (StringUtils.isEmpty(user.getName())) {
            throw new CustomException("yichang");
        }

        if (StringUtils.isEmpty(user.getUsername())) {
            throw new CustomException("yichang");
        }
        if (!StringUtils.isEmpty(user.getEmail())) {
            throw new CustomException("参数异常");
        }
        if (user.getUsername().contains("@")) {
            throw new CustomException("不可包含'@'");
        }
        checkSensitiveWords(user.getName());
        boolean save = userDao.save(user);
        if (save) {
            return R.success("创建成功");
        }
        throw new CustomException("yichang");
    }

    @Override
    public R<String> logout(String token) {
        if (StringUtils.isEmpty(token)) {
            return R.error(Code.DEL_TOKEN, "登陆过期");
        }
        RedisUtils.del(token);
        return R.successOnlyMsg("下线成功", Code.DEL_TOKEN);
    }

    @Override
    public R<LoginRes> loginByToken() {

        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            return R.error(0, "未登录");
        }
//        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(currentUser.getPermission()));
//
////        UserResult UserResult = new UserResult(String.valueOf(currentUser.getId()),currentUser.getUsername(),currentUser.getName(),currentUser.getPhone(),currentUser.getSex(),String.valueOf(currentUser.getStudentId()),currentUser.getStatus(),currentUser.getCreateTime(),currentUser.getUpdateTime(),currentUser.getPermission(),permission.getName(),null,currentUser.getEmail(),currentUser.getAvatar());
//
//        UserLoginR userLoginR = new UserLoginR();
//        userLoginR.setToken();
        return R.success(null);
    }

    @Override
    public Integer count() {
        return userDao.count();
    }


    /**
     * updateUserStatu的公共判断
     *
     * @param id
     * @param currentUser
     */
    @Transactional
    public void updateUserStatuVerdict(Long id, UserInfo currentUser) {
        if (id.equals(currentUser.getId())) {
            throw new CustomException("相信我，你自己管不好自己!");
        }
        if (iUserService.isSuperAdmin(currentUser.getSysRoles(), currentUser.getId())) {
            // 禁止此处操作大管理
            throw new CustomException("你已越界");
        }
    }

    @Transactional
    @Override
    public boolean updateUserStatus(String id, String status) {
        if (StringUtils.isEmpty(id)) {
            throw new CustomException("无操作对象");
        }
        if (StringUtils.isEmpty(status)) {
            throw new CustomException("无操作对象");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("授权问题");
        }
        List<User> users = new ArrayList<>();
        boolean update = false;
        if (id.contains(",")) {
            String[] split = id.split(",");
            for (String s :
                    split) {
                updateUserStatuVerdict(Long.valueOf(s), currentUser);
                User user = new User();
                user.setId(Long.valueOf(s));
                user.setStatus(Integer.valueOf(status));
                users.add(user);
            }
            update = userDao.updateBatchById(users);
        } else {
            updateUserStatuVerdict(Long.valueOf(id), currentUser);
            LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(User::getStatus, Integer.valueOf(status));
            lambdaUpdateWrapper.eq(User::getId, Long.valueOf(id));
            update = userDao.update(lambdaUpdateWrapper);
        }
        if (update) {
            return true;
        }
        throw new CustomException("更新失败");
    }

    @Transactional
    @Override
    public R<UserResult> updateForUser(User user) {
        if (user.getId() == null) {
            return R.error("更新失败");
        }
        if (user.getUsername() == null) {
            return R.error("更新失败");
        }
        if (user.getName() == null) {
            return R.error("更新失败");
        }
        if (user.getSex() == null) {
            return R.error("更新失败");
        }
        if (user.getStudentId() == null) {
            return R.error("更新失败");
        }
        if (user.getPhone() == null) {
            return R.error("更新失败");
        }

        if (user.getStudentId().length() > 12) {
            return R.error("不能超过12位学号");
        }
        if (user.getId().equals(1L)) {
            return R.error("禁止操作admin");
        }

        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId, user.getId());
        lambdaUpdateWrapper.set(User::getName, user.getName());
        lambdaUpdateWrapper.set(User::getStudentId, user.getStudentId());
        lambdaUpdateWrapper.set(User::getUsername, user.getUsername());
        lambdaUpdateWrapper.set(User::getSex, user.getSex());
        lambdaUpdateWrapper.set(User::getDeptId, user.getDeptId());
        lambdaUpdateWrapper.set(User::getStatus, user.getStatus());
        lambdaUpdateWrapper.set(User::getPhone, user.getPhone());
        lambdaUpdateWrapper.set(User::getAvatar, OssDBUtil.toDBUrl(user.getAvatar()));
        boolean update = userDao.update(lambdaUpdateWrapper);
        if (update) {
            return R.success("更新成功");
        }
        return R.error("err");
    }

    @Transactional
    @Override
    public R<UserResult> updateForUserSelf(User user) {
        if (user.getId() == null) {
            return R.error("更新失败");
        }
        if (user.getUsername() == null) {
            return R.error("更新失败");
        }
        if (user.getName() == null) {
            return R.error("更新失败");
        }
        if (user.getSex() == null) {
            return R.error("更新失败");
        }
        if (user.getStudentId() == null) {
            return R.error("更新失败");
        }
        if (user.getPhone() == null) {
            return R.error("更新失败");
        }
        if (user.getStudentId().length() > 12) {
            return R.error("不能超过12位学号");
        }
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId, user.getId());
        lambdaUpdateWrapper.set(User::getName, user.getName());
        lambdaUpdateWrapper.set(User::getStudentId, user.getStudentId());
        lambdaUpdateWrapper.set(User::getUsername, user.getUsername());
        lambdaUpdateWrapper.set(User::getSex, user.getSex());
        lambdaUpdateWrapper.set(User::getDeptId, user.getDeptId());
        lambdaUpdateWrapper.set(User::getStatus, user.getStatus());
        lambdaUpdateWrapper.set(User::getPhone, user.getPhone());
        lambdaUpdateWrapper.set(User::getAvatar, OssDBUtil.toDBUrl(user.getAvatar()));
        boolean update = userDao.update(lambdaUpdateWrapper);
        if (update) {
            return R.success("更新成功");
        }
        return R.error("err");
    }

    @Override
    public R<String> updateUser(String userid, String name, String username, String phone, String idNumber, String status, String grouping, String sex, String token) {
        return null;
    }

    @DataScope(userAlias = "user")
    @Override
    public PageData<UserResult> getUserList(User ua,Integer pageNum, Integer pageSize, String name, Integer cascade, Long deptId) {
        if (pageNum == null) {
            throw new IllegalArgumentException("传参错误");
        }
        if (pageSize == null) {
            throw new IllegalArgumentException("传参错误");
        }
        if (cascade == null) {
            cascade = 0;// 0为不级联，1为级联
        }
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), User::getName, name);
        AssertUtil.notEqual(deptId, null, "请指定查询");
        if (!cascade.equals(1)) {
            lambdaQueryWrapper.eq(User::getDeptId, deptId);
        } else {
            List<DeptManger> deptMangers = deptService.getDeptListOnlyTree();
            Set<DeptManger> childTemp = new HashSet<>();
            Set<Long> childId = new HashSet<>();
            int isDeptIdOrChild = 0;
            while (deptMangers != null && deptMangers.size() > 0) {
                for (DeptManger det :
                        deptMangers) {
                    if (det == null) {
                        continue;
                    }
                    if (det.getChildren() != null && det.getChildren().size() > 0) {
                        childTemp.addAll(det.getChildren());
                    }
                    if (det.getId().equals(deptId)) {
                        if (det.getChildren() != null) {
                            childTemp = new HashSet<>(det.getChildren());
                        }
                        isDeptIdOrChild = 1;
                        childId.add(det.getId());
                        break;
                    }
                    if (isDeptIdOrChild == 1) {
                        childId.add(det.getId());
                    }
                }
                if (childTemp.size() == 0) {
                    break;
                }
                deptMangers = new ArrayList<>(childTemp);

                childTemp = new HashSet<>();
            }
            if (childId.size() > 0) {
                log.info("childId={}", childId);
                lambdaQueryWrapper.in(User::getDeptId, childId);
            }
        }
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(User::getCreateTime);//按照创建时间排序
        lambdaQueryWrapper.apply(StringUtils.isNotEmpty(ua.getExistSql()),ua.getExistSql());
        userDao.page(pageInfo, lambdaQueryWrapper);
        PageData<UserResult> pageData = new PageData<>();
        List<UserResult> results = new ArrayList<>();
        for (Object user : pageInfo.getRecords()) {
            User user1 = (User) user;
            //Todo:需要优化，将部门整个进缓存，在查询不到或者更改时更新单个缓存
            SysDept sysDept = iSysDeptService.getById(user1.getDeptId());
            //从部门继承的不能直接显示，或者需要却别开，不然很乱
            LambdaQueryWrapper<SysUserRole> sysRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysRoleLambdaQueryWrapper.eq(SysUserRole::getUserId, user1.getId());
            List<RoleResp> collect = new ArrayList<>();
            List<SysUserRole> list = iSysUserRoleService.list(sysRoleLambdaQueryWrapper);
            if (list != null && list.size() > 0) {
                List<Long> rolesId = list.stream().map(SysUserRole::getRoleId).toList();
                List<SysRole> sysRoles = iSysRoleService.listByIds(rolesId);
                collect = sysRoles.stream().sorted(Comparator.comparing(SysRole::getRoleSort)).map(sysRole -> new RoleResp(String.valueOf(sysRole.getId()), sysRole.getRoleName(), sysRole.getRoleKey(), sysRole.getRoleSort())).collect(Collectors.toList());
            }

            String avatar = user1.getAvatar();
            if (StringUtils.isNotEmpty(avatar)) {
                avatar = OssDBUtil.toUseUrl(avatar);
            } else {
                avatar = "";
            }
            UserResult userResult = new UserResult(String.valueOf(user1.getId()), user1.getUsername(), user1.getName(), user1.getPhone(), user1.getSex(), String.valueOf(user1.getStudentId()), user1.getStatus(), user1.getCreateTime(), user1.getUpdateTime(), String.valueOf(user1.getDeptId()), sysDept.getDeptName(), user1.getEmail(), avatar, collect);
            results.add(userResult);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return pageData;
    }

    @Transactional
    @Override
    public R<String> deleteUsers(String id) {
        if (StringUtils.isEmpty(id)) {
            return R.error("无操作对象");
        }

        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("权限异常");
        }
        Collection<Long> ids = new ArrayList<>();
        if (id.contains(",")) {
            String[] split = id.split(",");
            for (String s :
                    split) {

                if (s.equals("1")) {
                    throw new CustomException("admin不可删除");
                }
                ids.add(Long.valueOf(s));
            }
            userDao.removeByIds(ids);
        } else {
            if (Long.valueOf(id).equals(1L)) {
                throw new CustomException("admin不可删除");
            }
            LambdaQueryWrapper<User> lambdaUpdateWrapper = new LambdaQueryWrapper<>();
            lambdaUpdateWrapper.eq(User::getId, Long.valueOf(id));
            userDao.remove(lambdaUpdateWrapper);
        }

        return R.success("删除成功");
    }

    @Override
    public R<String> hasUserName(String username) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername, username);
        int count = userDao.count(userLambdaQueryWrapper);
        if (count == 0) {
            return R.success("可用");
        }
        return R.error("请换一个用户名试试!");

    }

    @Transactional
    @Override
    public R<String> emailWithUser(String emails, String code, String token) {
        if (StringUtils.isEmpty(emails) || StringUtils.isEmpty(code) || StringUtils.isEmpty(token)) {
            throw new CustomException("参数异常");
        }

        try {
            DecodedJWT decodedJWT = JWTUtil.deToken(token);
            Claim id = decodedJWT.getClaim("id");
            if (!((String) RedisUtils.get("emailcode:" + id.asString(), String.class)).equals(code)) {
                throw new CustomException("验证码错误");
            }
            LambdaQueryWrapper<User> userLambdaQueryWrapperCount = new LambdaQueryWrapper<>();
            userLambdaQueryWrapperCount.eq(User::getEmail, emails);
            int count = userDao.count(userLambdaQueryWrapperCount);
            if (count > 0) {
                throw new CustomException("该账号已经绑定过帐号了!");
            }
            LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(User::getEmail, emails);
            lambdaUpdateWrapper.eq(User::getId, Long.valueOf(id.asString()));
            boolean update = userDao.update(lambdaUpdateWrapper);
            if (update) {
                return R.success("绑定成功");
            }
            return R.error("异常");
        } catch (Exception e) {
            return R.error(Code.DEL_TOKEN, e.getMessage());
        }
    }


    @Override
    public R<LoginRes> login(LoginDTO user) {
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            return R.error("参数异常");
        }
        User one = this.loginPublic(user.getUsername(), user.getPassword());
        String token = JWTUtil.getToken(String.valueOf(one.getId()));
        if (user.getWeek()) {
            RedisUtils.set(token, String.valueOf(one.getId()), 7 * 24 * 3600L, TimeUnit.SECONDS);
        } else {
            RedisUtils.set(token, String.valueOf(one.getId()), 12 * 3600L, TimeUnit.SECONDS);
        }
        LoginRes loginRes = new LoginRes();
        loginRes.setToken(token);
        return R.success(loginRes);
    }

    @Override
    public R<UserResult> info() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            return R.error(Code.DEL_TOKEN, "请先登录");
        }
        SysDept sysDept = iSysDeptService.getById(currentUser.getDeptId());

        String avatar = currentUser.getAvatar();
        if (StringUtils.isNotEmpty(avatar)) {
            avatar = OssDBUtil.toUseUrl(avatar);
        } else {
            avatar = "";
        }
        List<RoleResp> collect = currentUser.getSysRoles().stream().sorted(Comparator.comparing(SysRole::getRoleSort)).map(sysRole -> new RoleResp(String.valueOf(sysRole.getId()), sysRole.getRoleName(), sysRole.getRoleKey(), sysRole.getRoleSort())).collect(Collectors.toList());
        UserResult userResult = new UserResult(String.valueOf(currentUser.getId()), currentUser.getUsername(), currentUser.getName(), currentUser.getPhone(), currentUser.getSex(), String.valueOf(currentUser.getStudentId()), currentUser.getStatus(), currentUser.getCreateTime(), currentUser.getUpdateTime(), String.valueOf(currentUser.getDeptId()), sysDept.getDeptName(), currentUser.getEmail(), avatar, collect);

        return R.success(userResult);
    }

    @Transactional
    @Override
    public boolean updateUserInfo(User user) {
        log.info("user={}", user);
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        if (StringUtils.isEmpty(user.getPhone()) || StringUtils.isEmpty(user.getSex()) || StringUtils.isEmpty(user.getStudentId())) {
            throw new CustomException("请输入完整!");
        }
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId, currentUser.getId());
        lambdaUpdateWrapper.set(User::getName, user.getName());
        lambdaUpdateWrapper.set(User::getPhone, user.getPhone());
        //判空
        if (!StringUtils.isEmpty(user.getAvatar())) {
            lambdaUpdateWrapper.set(User::getAvatar, OssDBUtil.toDBUrl(user.getAvatar()));
        }
        lambdaUpdateWrapper.set(User::getSex, user.getSex());
        lambdaUpdateWrapper.set(User::getStudentId, user.getStudentId());
        boolean update = userDao.update(lambdaUpdateWrapper);
        return update;
    }

    @Override
    public Integer userPassword() {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        log.info("cu={}", currentUser);
        if (StringUtils.isEmpty(currentUser.getPassword()) && StringUtils.isEmpty(currentUser.getSalt())) {
            return 1;
        }
        return 0;
    }

    @Transactional
    @Override
    public boolean setPassword(PasswordR passwordR) {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录失效!", Code.DEL_TOKEN);
        }
        if (StringUtils.isNotEmpty(currentUser.getPassword()) || StringUtils.isNotEmpty(currentUser.getSalt())) {
            if (StringUtils.isEmpty(passwordR.getPassword())) {
                throw new CustomException("请输入原密码!");
            }
            String salt = currentUser.getSalt();
            String password = currentUser.getPassword();
            String md5Encryption = PWDMD5.getMD5Encryption(passwordR.getPassword(), salt);
            if (!md5Encryption.equals(password)) {
                throw new CustomException("原密码错误!");
            }
        }
        if (StringUtils.isEmpty(passwordR.getNewPassword()) || StringUtils.isEmpty(passwordR.getRePassword())) {
            throw new CustomException("请保证新密码和确认密码不为空!");
        }
        if (!passwordR.getNewPassword().equals(passwordR.getRePassword())) {
            throw new CustomException("请保证新密码和确认密码一致!");
        }
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.eq(User::getId, currentUser.getId());
        String salt = PWDMD5.getSalt();
        String md5Encryptions = PWDMD5.getMD5Encryption(passwordR.getNewPassword(), salt);
        userLambdaUpdateWrapper.set(User::getPassword, md5Encryptions);
        userLambdaUpdateWrapper.set(User::getSalt, salt);
        boolean update = userDao.update(userLambdaUpdateWrapper);
        return update;
    }


    @Transactional
    @Override
    public boolean updateByAdmin(UserResult user) {
        if (StringUtils.isEmpty(user.getId())) {
            throw new CustomException("无操作对象");
        }
        if (StringUtils.isEmpty(user.getName())) {
            throw new CustomException("用户昵称不能为空");
        }
        List<RoleResp> roles = user.getRoles();
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId, Long.valueOf(user.getId()));
        //admin
        if (user.getUsername().equals("admin")) {
            UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
            if (currentUser == null) {
                throw new CustomException("禁止未授权用户操作admin");
            }
            if (!currentUser.getUsername().equals("admin")) {
                throw new CustomException("禁止未授权用户操作admin");
            }
            // admin
            lambdaUpdateWrapper.set(User::getAvatar, OssDBUtil.toDBUrl(user.getAvatar()));
            lambdaUpdateWrapper.set(User::getPhone, user.getPhone());
            lambdaUpdateWrapper.set(User::getStudentId, user.getStudentId());
            lambdaUpdateWrapper.set(User::getSex, user.getSex());

            LambdaQueryWrapper<SysUserRole> sysUserRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysUserRoleLambdaQueryWrapper.eq(SysUserRole::getUserId, Long.valueOf(user.getId()));
            iSysUserRoleService.remove(sysUserRoleLambdaQueryWrapper);
            int haveSuper = 0;
            for (RoleResp role : roles) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(Long.valueOf(user.getId()));
                if (Long.valueOf(role.getId()).equals(1L)) {
                    haveSuper = 1;
                }
                sysUserRole.setRoleId(Long.valueOf(role.getId()));
                iSysUserRoleService.save(sysUserRole);
            }
            if (haveSuper == 0) {
                throw new CustomException("禁止删除超级管理员角色");
            }
        } else {
            //非admin
            lambdaUpdateWrapper.set(User::getEmail, user.getEmail());
            lambdaUpdateWrapper.set(User::getDeptId, user.getDeptId());
            lambdaUpdateWrapper.set(User::getAvatar, OssDBUtil.toDBUrl(user.getAvatar()));
            lambdaUpdateWrapper.set(User::getPhone, user.getPhone());
            lambdaUpdateWrapper.set(User::getName, user.getName());
            lambdaUpdateWrapper.set(User::getStudentId, user.getStudentId());
            lambdaUpdateWrapper.set(User::getStatus, user.getStatus());
            lambdaUpdateWrapper.set(User::getSex, user.getSex());

            LambdaQueryWrapper<SysUserRole> sysUserRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysUserRoleLambdaQueryWrapper.eq(SysUserRole::getUserId, Long.valueOf(user.getId()));
            iSysUserRoleService.remove(sysUserRoleLambdaQueryWrapper);
            for (RoleResp role : roles) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(Long.valueOf(user.getId()));
                if (Long.valueOf(role.getId()).equals(1L)) {
                    throw new CustomException("禁止添加超级管理员角色！");
                }
                sysUserRole.setRoleId(Long.valueOf(role.getId()));
                iSysUserRoleService.save(sysUserRole);
            }

        }
        boolean update = userDao.update(lambdaUpdateWrapper);

        return update;

    }


    @Override
    public List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req) {
        //需要前端同步的uid
        List<Long> uidList = getNeedSyncUidList(req.getReqList());
        //加载用户信息
        Map<Long, UserInfo> userInfoBatch = userCache.getUserInfoBatch(uidList.stream().collect(Collectors.toSet()));
        return req.getReqList()
                .stream()
                .map(a -> userInfoBatch.containsKey(a.getUid()) ? new SummeryInfoDTO(a.getUid(), true, userInfoBatch.get(a.getUid()).getName(),OssDBUtil.toUseUrl(userInfoBatch.get(a.getUid()).getAvatar()), "未知") : SummeryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Long> getNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList) {
        List<Long> result = new ArrayList<>();
        List<Long> userModifyTime = userCache.getUserModifyTime(reqList.stream().map(SummeryInfoReq.infoReq::getUid).collect(Collectors.toList()));
        for (int i = 0; i < reqList.size(); i++) {
            SummeryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = userModifyTime.get(i);
            if (Objects.isNull(infoReq.getLastModifyTime()) || (Objects.nonNull(modifyTime) && modifyTime > infoReq.getLastModifyTime())) {
                result.add(infoReq.getUid());
            }
        }
        return result;
    }


    @Transactional
    @Override
    public RegisterResp emailRegister(String email, String password) {
        if (StringUtils.isEmpty(email)) {
            throw new CustomException("邮箱不能为空");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CustomException("密码不能为空");
        }
        if (!email.contains("@")) {
            throw new CustomException("邮箱格式不正确");
        }
        if (password.length() < 6) {
            throw new CustomException("密码长度不能小于6");
        }
        if (password.length() > 30) {
            throw new CustomException("密码长度不能大于30");
        }
        String regex = "(?=.*[a-zA-Z])(?=.*[0-9]).{6,30}";
        // 要验证的字符串
        // 创建Pattern对象
        Pattern pattern = Pattern.compile(regex);
        // 创建Matcher对象
        Matcher matcher = pattern.matcher(password);

        // 进行匹配
        if (!matcher.matches()) {
            throw new CustomException("密码需要包含字母数字，且6位以上");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getEmail, email);
        // 此处会自动排除逻辑删除的数据
        if (userDao.count(userLambdaQueryWrapper) > 0) {
            throw new CustomException("该邮箱已经注册，若密码忘记可尝试找回密码!");
        }
        // 用户名也得全局唯一，默认为邮箱，要是重复了就用uuid
        User user = new User();
        LambdaQueryWrapper<User> userLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper1.eq(User::getUsername, email);
        user.setUsername(email);
        if (userDao.count(userLambdaQueryWrapper1) > 0) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15);
            user.setUsername(uuid);
        }
        user.setDeptId(1L);
        // 自己创建
        user.setCreateUser(1L);
        user.setAvatar("");
        user.setEmail(email.toLowerCase());
        user.setName("亲爱的用户，请改名");
        String salt = PWDMD5.getSalt();
        String md5Encryption = PWDMD5.getMD5Encryption(password, salt);
        user.setPassword(md5Encryption);
        user.setSalt(salt);
        user.setStatus(1);
        user.setSex("未知");
        user.setPhone("");
        user.setStudentId("");
        userDao.save(user);
        RegisterResp registerResp = new RegisterResp();
        String token = JWTUtil.getToken(String.valueOf(user.getId()));
        RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);
        registerResp.setToken(token);
        return registerResp;
    }

    @Transactional
    @Override
    public ForgetPasswordResp forgetPasswordByEmail(String email, String password) {
        if (StringUtils.isEmpty(email)) {
            throw new CustomException("邮箱不能为空");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CustomException("密码不能为空");
        }
        if (!email.contains("@")) {
            throw new CustomException("邮箱格式不正确");
        }
        if (password.length() < 6) {
            throw new CustomException("密码长度不能小于6");
        }
        if (password.length() > 30) {
            throw new CustomException("密码长度不能大于30");
        }
        String regex = "(?=.*[a-zA-Z])(?=.*[0-9]).{6,30}";
        // 要验证的字符串
        // 创建Pattern对象
        Pattern pattern = Pattern.compile(regex);
        // 创建Matcher对象
        Matcher matcher = pattern.matcher(password);

        // 进行匹配
        if (!matcher.matches()) {
            throw new CustomException("密码需要包含字母数字，且6位以上");
        }
        User user = userDao.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new CustomException("用户不存在");
        }
        String salt = PWDMD5.getSalt();
        String md5Encryption = PWDMD5.getMD5Encryption(password, salt);
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId, user.getId());
        lambdaUpdateWrapper.set(User::getPassword, md5Encryption);
        lambdaUpdateWrapper.set(User::getSalt, salt);
        userDao.update(lambdaUpdateWrapper);
        ForgetPasswordResp forgetPasswordResp = new ForgetPasswordResp();
        String token = JWTUtil.getToken(String.valueOf(user.getId()));
        RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);
        forgetPasswordResp.setToken(token);
        return forgetPasswordResp;
    }

    @Override
    public User loginPublic(String username, String password) {
        User one = null;
        // 先判断电子邮箱
        if (username.contains("@")) {
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getEmail, username);
            int count = userDao.count(userLambdaQueryWrapper);
            log.info("count{}", count);
            one = userDao.getOne(userLambdaQueryWrapper);
            if (one == null) {
                // 用户名登录
                LambdaQueryWrapper<User> userLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                userLambdaQueryWrapper1.eq(User::getUsername, username);
                one = userDao.getOne(userLambdaQueryWrapper1);
            }
        } else {
            // 用户名登录
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getUsername, username);
            one = userDao.getOne(userLambdaQueryWrapper);
        }

        if (one == null) {
            throw new CustomException("用户名或密码错误");
        }
        if (one.getPassword() == null || one.getSalt() == null) {
            throw new CustomException("此用户未设置密码,请使用oauth2登录");
        }
        String Spassword = PWDMD5.getMD5Encryption(password, one.getSalt());
        if (!one.getPassword().equals(Spassword)) {
            throw new CustomException("用户名或密码错误");
        }
        if (one.getStatus().equals(0)) {
            throw new CustomException("账号已被禁用");
        }

        return one;
    }

    @Transactional
    @Override
    public boolean setPasswordByOneTimeCodeReq(PasswordByOneTimeCodeReq passwordR) {
        String oneTimeCode = passwordR.getOneTimeCode();
        if (StringUtils.isEmpty(oneTimeCode)) {
            throw new CustomException("一次性验证码为空!");
        }
        UserInfo userInfo = RedisUtils.get(MyString.one_time_code_key + oneTimeCode, UserInfo.class);
        if (userInfo == null) {
            throw new CustomException("一次性验证码已被使用或已过期!");
        }

        if (StringUtils.isEmpty(passwordR.getPassword()) || StringUtils.isEmpty(passwordR.getRePassword())) {
            throw new CustomException("请保证新密码和确认密码不为空!");
        }
        if (!passwordR.getPassword().equals(passwordR.getRePassword())) {
            throw new CustomException("请保证新密码和确认密码一致!");
        }
        if (passwordR.getPassword().length() < 6) {
            throw new CustomException("密码长度不能小于6");
        }
        if (passwordR.getPassword().length() > 30) {
            throw new CustomException("密码长度不能大于30");
        }
        String regex = "(?=.*[a-zA-Z])(?=.*[0-9]).{6,30}";
        // 要验证的字符串
        // 创建Pattern对象
        Pattern pattern = Pattern.compile(regex);
        // 创建Matcher对象
        Matcher matcher = pattern.matcher(passwordR.getPassword());

        // 进行匹配
        if (!matcher.matches()) {
            throw new CustomException("密码需要包含字母数字，且6位以上");
        }
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.eq(User::getId, userInfo.getId());
        String salt = PWDMD5.getSalt();
        String md5Encryptions = PWDMD5.getMD5Encryption(passwordR.getPassword(), salt);
        userLambdaUpdateWrapper.set(User::getPassword, md5Encryptions);
        userLambdaUpdateWrapper.set(User::getSalt, salt);
        boolean update = userDao.update(userLambdaUpdateWrapper);
        if (update) {
            RedisUtils.del(MyString.one_time_code_key + oneTimeCode);
        }
        return update;
    }

    @Override
    public LoginRes loginByEmailCode(LoginByEmailCodeReq loginByEmailCodeReq) {
        if (StringUtils.isEmpty(loginByEmailCodeReq.getEmail()) || StringUtils.isEmpty(loginByEmailCodeReq.getEmailCode())) {
            throw new CustomException("邮箱或验证码为空");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getEmail, loginByEmailCodeReq.getEmail());
        LoginRes loginRes = new LoginRes();

        // 此处会自动排除逻辑删除的数据
        if (userDao.count(userLambdaQueryWrapper) > 0) {
            User user = userDao.getOne(userLambdaQueryWrapper);
            String token = JWTUtil.getToken(String.valueOf(user.getId()));

            //老用户
            //token分为半天和一周的
            if (loginByEmailCodeReq.getWeek()) {
                RedisUtils.set(token, String.valueOf(user.getId()), 7 * 12 * 3600L, TimeUnit.SECONDS);
            } else {
                RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);
            }
            loginRes.setToken(token);
            loginRes.setToSetPassword(0);
            //300s内有效，每次邮箱验证码登录没有设置密码都会弹出此一次。
            String oneTimeSetPasswordCode = OneTimeSetPasswordCodeUtil.createOneTimeSetPasswordCode(user);
            if (StringUtils.isNotEmpty(oneTimeSetPasswordCode)) {
                loginRes.setToSetPassword(1);
                loginRes.setOneTimeSetPasswordCode(oneTimeSetPasswordCode);
            }
            return loginRes;
        }
        // 用户名也得全局唯一，默认为邮箱，要是重复了就用uuid
        User user = new User();
        LambdaQueryWrapper<User> userLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper1.eq(User::getUsername, loginByEmailCodeReq.getEmail());
        user.setUsername(loginByEmailCodeReq.getEmail().toLowerCase());
        if (userDao.count(userLambdaQueryWrapper1) > 0) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15);
            user.setUsername(uuid);
        }
        user.setDeptId(1L);
        // 自己创建
        user.setCreateUser(1L);
        user.setAvatar("");
        user.setEmail(loginByEmailCodeReq.getEmail().toLowerCase());
        user.setName("亲爱的用户，请改名");
        user.setStatus(1);
        user.setSex("未知");
        user.setPhone("");
        user.setStudentId("");
        userDao.save(user);
        LoginRes loginRes1 = new LoginRes();
        String token = JWTUtil.getToken(String.valueOf(user.getId()));
        RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);
        loginRes1.setToken(token);
        //300s内有效，每次邮箱验证码登录没有设置密码都会弹出此一次。
        String oneTimeSetPasswordCode = OneTimeSetPasswordCodeUtil.createOneTimeSetPasswordCode(user);
        loginRes1.setToSetPassword(0);
        if (StringUtils.isNotEmpty(oneTimeSetPasswordCode)) {
            loginRes1.setToSetPassword(1);
            loginRes1.setOneTimeSetPasswordCode(oneTimeSetPasswordCode);
        }
        return loginRes1;
    }

    @Override
    public UserSelectListResp userSelectList(String name) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(User::getName, name);
        List<User> list = userDao.list(lambdaQueryWrapper);
        UserSelectListResp userSelectListResp = new UserSelectListResp();
        userSelectListResp.setOptions(list);
        return userSelectListResp;
    }

    @Override
    public PageData<UserResult> getUserListForBpm(Integer pageNum, Integer pageSize, String name, Long deptId) {
        if (pageNum == null) {
            throw new IllegalArgumentException("传参错误");
        }
        if (pageSize == null) {
            throw new IllegalArgumentException("传参错误");
        }
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), User::getName, name);
        if (deptId != null) {
            lambdaQueryWrapper.eq(User::getDeptId, deptId);
        }

        //添加排序条件
        lambdaQueryWrapper.orderByAsc(User::getCreateTime);//按照创建时间排序
        userDao.page(pageInfo, lambdaQueryWrapper);
        PageData<UserResult> pageData = new PageData<>();
        List<UserResult> results = new ArrayList<>();
        for (Object user : pageInfo.getRecords()) {
            User user1 = (User) user;
            //Todo:需要优化，将部门整个进缓存，在查询不到或者更改时更新单个缓存
            SysDept sysDept = iSysDeptService.getById(user1.getDeptId());
            //从部门继承的不能直接显示，或者需要却别开，不然很乱
            LambdaQueryWrapper<SysUserRole> sysRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysRoleLambdaQueryWrapper.eq(SysUserRole::getUserId, user1.getId());
            List<RoleResp> collect = new ArrayList<>();
            List<SysUserRole> list = iSysUserRoleService.list(sysRoleLambdaQueryWrapper);
            if (list != null && list.size() > 0) {
                List<Long> rolesId = list.stream().map(SysUserRole::getRoleId).toList();
                List<SysRole> sysRoles = iSysRoleService.listByIds(rolesId);
                collect = sysRoles.stream().sorted(Comparator.comparing(SysRole::getRoleSort)).map(sysRole -> new RoleResp(String.valueOf(sysRole.getId()), sysRole.getRoleName(), sysRole.getRoleKey(), sysRole.getRoleSort())).collect(Collectors.toList());
            }

            String avatar = user1.getAvatar();
            if (StringUtils.isNotEmpty(avatar)) {
                avatar = OssDBUtil.toUseUrl(avatar);
            } else {
                avatar = "";
            }
            UserResult userResult = new UserResult(String.valueOf(user1.getId()), user1.getUsername(), user1.getName(), user1.getPhone(), user1.getSex(), String.valueOf(user1.getStudentId()), user1.getStatus(), user1.getCreateTime(), user1.getUpdateTime(), String.valueOf(user1.getDeptId()), sysDept.getDeptNameAll(), user1.getEmail(), avatar, collect);
            results.add(userResult);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return pageData;
    }


    @Override
    public UserSelectListResp userSelectOnlyXUserList(Long deptId) {
        List<DeptManger> deptListOnlyTree = deptService.getDeptListOnlyTree();
        UserSelectListResp userSelectListResp = new UserSelectListResp();
        List<Long> ids = new ArrayList<>();
        if (deptListOnlyTree.get(0) == null) {
            throw new CustomException("根部门不能为空");
        }
        Queue<DeptManger> queue = new LinkedList<>();
        queue.offer(deptListOnlyTree.get(0));
        DeptManger zhaodao = null;

        while (!queue.isEmpty()) {
            DeptManger node = queue.poll();
            if (node.getId().equals(deptId)) {
                zhaodao = node;
                break;
            }
            if (node.getChildren() != null) {
                for (DeptManger child : node.getChildren()) {
                    queue.offer(child);
                }
            }
        }
        if (zhaodao == null) {
            userSelectListResp.setOptions(new ArrayList<>());
            return userSelectListResp;
        }
        Queue<DeptManger> queue2 = new LinkedList<>();
        queue2.offer(zhaodao);

        while (!queue2.isEmpty()) {
            DeptManger node = queue2.poll();
            ids.add(node.getId());
            if (node.getChildren() != null) {
                for (DeptManger child : node.getChildren()) {
                    queue2.offer(child);
                }
            }
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.in(User::getDeptId, ids);
        List<User> list = userDao.list(userLambdaQueryWrapper);
        if (list == null) {
            list = new ArrayList<>();
        }
        userSelectListResp.setOptions(list);
        return userSelectListResp;
    }


}
