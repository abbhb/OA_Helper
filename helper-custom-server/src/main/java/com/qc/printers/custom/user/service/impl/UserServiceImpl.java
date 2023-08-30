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
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.utils.*;
import com.qc.printers.common.user.domain.entity.Permission;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.IUserService;
import com.qc.printers.custom.user.domain.dto.LoginDTO;
import com.qc.printers.custom.user.domain.vo.request.PasswordR;
import com.qc.printers.custom.user.domain.vo.response.LoginRes;
import com.qc.printers.custom.user.domain.vo.response.UserResult;
import com.qc.printers.custom.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.qc.printers.common.common.utils.ParamsCalibration.checkSensitiveWords;


@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final RestTemplate restTemplate;
    private final IUserService iUserService;
    @Autowired
    private CASOauthUtil casOauthUtil;
    @Autowired
    private CommonService commonService;

    @Autowired
    public UserServiceImpl(RestTemplate restTemplate, IUserService iUserService) {
        this.restTemplate = restTemplate;
        this.iUserService = iUserService;
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
//        boolean save = iUserService.save(user);
//        if (!save){
//            throw new CustomException("认证失败");
//        }
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(User::getId,Long.valueOf(user.getId()));
//        User one = iUserService.getOne(queryWrapper);
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
        if (user.getPermission() == null) {
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

        if (user.getPermission().equals(1)) {
            User byId = iUserService.getById(userId);
            if (!byId.getPermission().equals(1)) {
                return R.error("权限不足");
            }
        }
        checkSensitiveWords(user.getName());
        boolean save = iUserService.save(user);
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
        return iUserService.count();
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
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("授权问题");
        }
        List<User> users = new ArrayList<>();
        boolean update = false;
        if (id.contains(",")) {
            String[] split = id.split(",");
            for (String s :
                    split) {
                //这部分代码感觉需要优化👇和下面的块是重复的
                User byId = iUserService.getById(Long.valueOf(s));
                if (byId == null) {
                    //don't hava object
                    throw new CustomException("没有对象");
                }
                if (byId.getPermission().equals(10)) {
                    // 禁止操作大管理
                    throw new CustomException("你已越界");
                }
                if (byId.getId().equals(currentUser.getId())) {
                    throw new CustomException("相信我，你自己管不好自己!");
                }
                //这部分代码感觉需要优化👆


                User user = new User();
                user.setId(Long.valueOf(s));
                user.setStatus(Integer.valueOf(status));
                users.add(user);
            }
            update = iUserService.updateBatchById(users);
        } else {
            //这部分代码感觉需要优化👇和上面块是重复的
            User byId = iUserService.getById(Long.valueOf(id));
            if (byId == null) {
                //don't hava object
                throw new CustomException("没有对象");
            }
            if (byId.getPermission().equals(10)) {
                // 禁止操作大管理
                throw new CustomException("你已越界");
            }
            if (byId.getId().equals(currentUser.getId())) {
                throw new CustomException("相信我，你自己管不好自己!");
            }
            //这部分代码感觉需要优化👆


            LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(User::getStatus, Integer.valueOf(status));
            lambdaUpdateWrapper.eq(User::getId, Long.valueOf(id));
            update = iUserService.update(lambdaUpdateWrapper);
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
        lambdaUpdateWrapper.set(User::getPermission, user.getPermission());
        lambdaUpdateWrapper.set(User::getStatus, user.getStatus());
        lambdaUpdateWrapper.set(User::getPhone, user.getPhone());
        lambdaUpdateWrapper.set(User::getAvatar, user.getAvatar());
        boolean update = iUserService.update(lambdaUpdateWrapper);
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
        lambdaUpdateWrapper.set(User::getPermission, user.getPermission());
        lambdaUpdateWrapper.set(User::getStatus, user.getStatus());
        lambdaUpdateWrapper.set(User::getPhone, user.getPhone());
        lambdaUpdateWrapper.set(User::getAvatar, user.getAvatar());
        boolean update = iUserService.update(lambdaUpdateWrapper);
        if (update) {
            return R.success("更新成功");
        }
        return R.error("err");
    }

    @Override
    public R<String> updateUser(String userid, String name, String username, String phone, String idNumber, String status, String grouping, String sex, String token) {
        return null;
    }

    @Override
    public PageData<UserResult> getUserList(Integer pageNum, Integer pageSize, String name) {
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
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(User::getCreateTime);//按照创建时间排序
        iUserService.page(pageInfo, lambdaQueryWrapper);
        PageData<UserResult> pageData = new PageData<>();
        List<UserResult> results = new ArrayList<>();
        for (Object user : pageInfo.getRecords()) {
            User user1 = (User) user;
            Permission permission = RedisUtils.hget(MyString.permission_key, String.valueOf(user1.getPermission()), Permission.class);
            UserResult userResult = new UserResult(String.valueOf(user1.getId()), user1.getUsername(), user1.getName(), user1.getPhone(), user1.getSex(), String.valueOf(user1.getStudentId()), user1.getStatus(), user1.getCreateTime(), user1.getUpdateTime(), user1.getPermission(), permission.getName(), user1.getEmail(), user1.getAvatar());
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
    public R<String> deleteUsers(String id, Long userId) {
        if (StringUtils.isEmpty(id)) {
            return R.error("无操作对象");
        }
        if (userId == null) {
            throw new CustomException("环境异常");
        }
        User byId = iUserService.getById(userId);
        if (byId.getPermission().equals(2)) {
            //当前是User身份,不返回数据
            return R.error("你好像没权限欸!");
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
            iUserService.removeByIds(ids);
        } else {
            if (Long.valueOf(id).equals(1L)) {
                throw new CustomException("admin不可删除");
            }
            LambdaQueryWrapper<User> lambdaUpdateWrapper = new LambdaQueryWrapper<>();
            lambdaUpdateWrapper.eq(User::getId, Long.valueOf(id));
            iUserService.remove(lambdaUpdateWrapper);
        }

        return R.success("删除成功");
    }

    @Override
    public R<String> hasUserName(String username) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername, username);
        int count = iUserService.count(userLambdaQueryWrapper);
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
            int count = iUserService.count(userLambdaQueryWrapperCount);
            if (count > 0) {
                throw new CustomException("该账号已经绑定过帐号了!");
            }
            LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(User::getEmail, emails);
            lambdaUpdateWrapper.eq(User::getId, Long.valueOf(id.asString()));
            boolean update = iUserService.update(lambdaUpdateWrapper);
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
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername, user.getUsername());
        User one = iUserService.getOne(userLambdaQueryWrapper);
        if (one == null) {
            return R.error("用户名或密码错误");
        }
        if (one.getPassword() == null || one.getSalt() == null) {
            return R.error("此用户未设置密码,请使用oauth2登录");
        }
        String password = PWDMD5.getMD5Encryption(RSAUtil.decryptDataOnJava(user.getPassword()), one.getSalt());
        if (!one.getPassword().equals(password)) {
            return R.error("用户名或密码错误");
        }
        if (one.getStatus().equals(0)) {
            return R.error("账号已被禁用");
        }
        String token = JWTUtil.getToken(String.valueOf(one.getId()), String.valueOf(one.getPermission()));
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
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            return R.error(Code.DEL_TOKEN, "请先登录");
        }
        Permission permission = (Permission) RedisUtils.hget(MyString.permission_key, String.valueOf(currentUser.getPermission()), Permission.class);
        String avatar = currentUser.getAvatar();
        if (StringUtils.isNotEmpty(avatar)) {
            if (!avatar.contains("http")) {
                String imageUrl = commonService.getImageUrl(avatar);
                avatar = imageUrl;
            }
        } else {
            avatar = "";
        }

        UserResult userResult = new UserResult(String.valueOf(currentUser.getId()), currentUser.getUsername(), currentUser.getName(), currentUser.getPhone(), currentUser.getSex(), String.valueOf(currentUser.getStudentId()), currentUser.getStatus(), currentUser.getCreateTime(), currentUser.getUpdateTime(), currentUser.getPermission(), permission.getName(), currentUser.getEmail(), avatar);

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
            lambdaUpdateWrapper.set(User::getAvatar, user.getAvatar());
            if (user.getAvatar().contains("http")) {
                lambdaUpdateWrapper.set(User::getAvatar, user.getAvatar().split("aistudio/")[1]);
            }
        }
        lambdaUpdateWrapper.set(User::getSex, user.getSex());
        lambdaUpdateWrapper.set(User::getStudentId, user.getStudentId());
        boolean update = iUserService.update(lambdaUpdateWrapper);
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
        boolean update = iUserService.update(userLambdaUpdateWrapper);
        return update;
    }

}
