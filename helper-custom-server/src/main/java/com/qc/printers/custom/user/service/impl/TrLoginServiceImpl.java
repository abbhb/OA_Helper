package com.qc.printers.custom.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.Token;
import com.qc.printers.common.common.utils.*;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.TrLogin;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ITrLoginService;
import com.qc.printers.custom.user.domain.entity.UniquekerLoginInfo;
import com.qc.printers.custom.user.domain.vo.request.ThirdFirstLoginReq;
import com.qc.printers.custom.user.domain.vo.response.LoginRes;
import com.qc.printers.custom.user.domain.vo.response.ThirdCallbackResp;
import com.qc.printers.custom.user.service.TrLoginService;
import com.qc.printers.custom.user.utils.UniquekerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 第三方登录服务之ENROOM
 */
@Service
@Slf4j
public class TrLoginServiceImpl implements TrLoginService {
    @Autowired
    private CASOauthUtil casOauthUtil;
    private final RestTemplate restTemplate;

    private final ITrLoginService iTrLoginService;

    @Autowired
    MinIoProperties minIoProperties;


    @Autowired
    private UserDao userDao;

    public TrLoginServiceImpl(RestTemplate restTemplate, ITrLoginService iTrLoginService) {
        this.restTemplate = restTemplate;
        this.iTrLoginService = iTrLoginService;
    }

    @Transactional
    @Override
    public R<LoginRes> casLogin(String code) {
        if (code==null||code.equals("")){
            throw new CustomException("认证失败", Code.DEL_TOKEN);
        }
        Token tokenByCode = casOauthUtil.getTokenByCode(restTemplate, code);
        if (tokenByCode==null){
            throw new CustomException("认证失败", Code.DEL_TOKEN);
        }
        JSONObject userObjectByToken = casOauthUtil.getUserObjectByToken(restTemplate, tokenByCode);
        if (userObjectByToken==null){
            throw new CustomException("认证失败",Code.DEL_TOKEN);
        }
        String openid = userObjectByToken.getString("openid");
        LambdaQueryWrapper<TrLogin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrLogin::getTrId,openid);
        TrLogin one = iTrLoginService.getOne(wrapper);
        if (one!=null&&one.getStatus().equals(1)) {
            //已经绑定
            User byId = userDao.getById(one.getUserId());
            if (byId == null) {
                throw new CustomException("认证失败", Code.DEL_TOKEN);
            }
            String token = JWTUtil.getToken(String.valueOf(byId.getId()));
            RedisUtils.set(token, String.valueOf(byId.getId()), 12 * 3600L, TimeUnit.SECONDS);
            LoginRes userResult = new LoginRes();
            userResult.setToken(token);
            return R.success(userResult);
        }
        //下面是新建账号,后面改为自定义信息
        User user = new User();
        //正常是不会出现用户名重复的情况的
        user.setUsername(userObjectByToken.getString("username"));
        user.setName(userObjectByToken.getString("name"));
        String avatar = null;
        String phone;
        //直接不要头像
//        try{
//            String string = userObjectByToken.getString("avatar");
//            avatar = string;
//            MultipartFile multipartFile = ImageUtil.base64ImageToM(avatar);
//            avatar = commonService.uploadFileTOMinio(multipartFile).getData();
//        }catch (Exception e){
//            avatar = null;
//        }
        try {
            phone = userObjectByToken.getString("phone");
        } catch (Exception e) {
            phone = null;
        }
        user.setPhone(phone);
        // 设置头像
        user.setAvatar(avatar);
        user.setStatus(1);
        user.setSex(userObjectByToken.getString("sex"));
        user.setEmail(userObjectByToken.getString("email"));
//        String permissionName = userObjectByToken.getString("permission_name");
        user.setDeptId(1L);
        user.setCreateUser(1L);
        boolean save = userDao.save(user);
        if (save) {
            if (one == null) {
                //添加账号并绑定然后登录
                TrLogin trLogin = new TrLogin();
                trLogin.setStatus(1);
                trLogin.setTrId(openid);
                trLogin.setType("qq");
                trLogin.setIsDeleted(0);
                trLogin.setUserId(user.getId());
                boolean save1 = iTrLoginService.save(trLogin);
                if(!save1){
                    throw new CustomException("认证失败",Code.DEL_TOKEN);
                }
            }else if (one.getStatus().equals(0)){
                LambdaUpdateWrapper<TrLogin> trLoginLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                trLoginLambdaUpdateWrapper.eq(TrLogin::getUserId,user.getId());
                //修改状态为1
                trLoginLambdaUpdateWrapper.set(TrLogin::getStatus,1);
                boolean update = iTrLoginService.update(trLoginLambdaUpdateWrapper);
                if (!update){
                    throw new CustomException("认证失败",Code.DEL_TOKEN);
                }
            }
            String token = JWTUtil.getToken(String.valueOf(user.getId()));
            RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);
            LoginRes userResult = new LoginRes();
            userResult.setToken(token);
            return R.success(userResult);
        }
        return R.error("错误");
    }

    @Override
    public ThirdCallbackResp uniCallback(String type, String code) {
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(code)) {
            throw new CustomException("参数异常");
        }
        ThirdCallbackResp thirdCallbackResp = new ThirdCallbackResp();

        UniquekerLoginInfo uniquekerLoginInfo = UniquekerUtil.getUniquekerLoginInfo(code, type);
        AssertUtil.notEqual(uniquekerLoginInfo, null, "获取用户信息失败");
        String socialUid = uniquekerLoginInfo.getSocialUid();


        thirdCallbackResp.setThirdType(uniquekerLoginInfo.getType());
        thirdCallbackResp.setThirdAvatar(uniquekerLoginInfo.getFaceimg());
        thirdCallbackResp.setThirdName(uniquekerLoginInfo.getNickname());

        LambdaQueryWrapper<TrLogin> trLoginLambdaQueryWrapper = new LambdaQueryWrapper<>();
        trLoginLambdaQueryWrapper.eq(TrLogin::getTrId, socialUid);
        trLoginLambdaQueryWrapper.eq(TrLogin::getType, type);
        TrLogin trLogin = iTrLoginService.getOne(trLoginLambdaQueryWrapper);

        if (trLogin == null) {
            // 当前为新用户，起码就是没绑定电子邮箱，通过电子邮箱再去查user，绑定用户id

            thirdCallbackResp.setNewUser(true);
            thirdCallbackResp.setThirdSocialUid(uniquekerLoginInfo.getSocialUid());
            thirdCallbackResp.setCanLogin(false);
            return thirdCallbackResp;
        }
        // 登录
        User user = userDao.getById(trLogin.getUserId());
        if (user == null) {
            throw new CustomException("oauth异常-第三方登录无法找到本地用户");
        }
        String token = JWTUtil.getToken(String.valueOf(user.getId()));
        RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);

        thirdCallbackResp.setToken(token);
        thirdCallbackResp.setNewUser(false);
        thirdCallbackResp.setThirdSocialUid(uniquekerLoginInfo.getSocialUid());
        thirdCallbackResp.setCanLogin(true);
        return thirdCallbackResp;
    }

    @Transactional
    @Override
    public LoginRes uniFirstLogin(ThirdFirstLoginReq thirdFirstLoginReq) {
        if (thirdFirstLoginReq == null) {
            throw new CustomException("参数异常");
        }
        UniquekerLoginInfo uniquekerLoginInfoBySocialUid = UniquekerUtil.getUniquekerLoginInfoBySocialUid(thirdFirstLoginReq.getThirdSocialUid(), thirdFirstLoginReq.getThirdType());
        LambdaQueryWrapper<TrLogin> trLoginLambdaQueryWrapper = new LambdaQueryWrapper<>();
        trLoginLambdaQueryWrapper.eq(TrLogin::getTrId, uniquekerLoginInfoBySocialUid.getSocialUid());
        trLoginLambdaQueryWrapper.eq(TrLogin::getType, thirdFirstLoginReq.getThirdType());
        LoginRes loginRes = new LoginRes();
        // 如果这个用户绑定过这个第三方id直接登录，避免老六用户抓接口
        TrLogin trLogin = iTrLoginService.getOne(trLoginLambdaQueryWrapper);
        if (trLogin != null) {
            User user = userDao.getById(trLogin.getUserId());
            if (user != null) {
                String token = JWTUtil.getToken(String.valueOf(user.getId()));
                RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);

                loginRes.setToken(token);
                return loginRes;
            }
            // 绑定过，但用户不存在，直接删除绑定关系
            iTrLoginService.removeById(trLogin.getId());
        }
        // 现在就当没这个trLogin了
        // 先去判断邮箱是否已经在用户表存在，直接绑定
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getEmail, thirdFirstLoginReq.getEmail());
        User user = userDao.getOne(userLambdaQueryWrapper);
        if (user != null) {
            //用户已经存在，直接绑定即可！
            TrLogin trLogin1 = new TrLogin();
            trLogin1.setType(uniquekerLoginInfoBySocialUid.getType());
            trLogin1.setTrId(uniquekerLoginInfoBySocialUid.getSocialUid());
            trLogin1.setUserId(user.getId());
            trLogin1.setIsDeleted(0);
            trLogin1.setStatus(1);
            iTrLoginService.save(trLogin1);
            String token = JWTUtil.getToken(String.valueOf(user.getId()));
            RedisUtils.set(token, String.valueOf(user.getId()), 12 * 3600L, TimeUnit.SECONDS);

            loginRes.setToken(token);
            loginRes.setToSetPassword(0);
            String oneTimeSetPasswordCode = OneTimeSetPasswordCodeUtil.createOneTimeSetPasswordCode(user);
            if (StringUtils.isNotEmpty(oneTimeSetPasswordCode)) {
                loginRes.setOneTimeSetPasswordCode(oneTimeSetPasswordCode);
                loginRes.setToSetPassword(1);
            }
            return loginRes;
        }
        // 这个邮箱也是新邮箱，没创建过用户
        // 用户名也得全局唯一，默认为邮箱，要是重复了就用uuid
        User userNew = new User();
        LambdaQueryWrapper<User> userLambdaQueryWrapperN = new LambdaQueryWrapper<>();
        userLambdaQueryWrapperN.eq(User::getUsername, thirdFirstLoginReq.getEmail());
        userNew.setUsername(thirdFirstLoginReq.getEmail().toLowerCase());
        if (userDao.count(userLambdaQueryWrapperN) > 0) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15);
            userNew.setUsername(uuid);
        }
        userNew.setDeptId(1L);
        // 自己创建
        userNew.setCreateUser(1L);
        // 头像优化
        userNew.setAvatar(getUniAvatarToLocal(uniquekerLoginInfoBySocialUid.getFaceimg()));
        userNew.setEmail(thirdFirstLoginReq.getEmail().toLowerCase());
        userNew.setName(uniquekerLoginInfoBySocialUid.getNickname());
        userNew.setStatus(1);
        userNew.setSex("未知");
        userNew.setPhone("");
        userNew.setStudentId("");
        userDao.save(userNew);
        LoginRes loginRes1 = new LoginRes();
        String token = JWTUtil.getToken(String.valueOf(userNew.getId()));
        RedisUtils.set(token, String.valueOf(userNew.getId()), 12 * 3600L, TimeUnit.SECONDS);
        loginRes1.setToken(token);
        //300s内有效，每次邮箱验证码登录没有设置密码都会弹出此一次。
        loginRes1.setToSetPassword(0);
        String oneTimeSetPasswordCode = OneTimeSetPasswordCodeUtil.createOneTimeSetPasswordCode(user);
        if (StringUtils.isNotEmpty(oneTimeSetPasswordCode)) {
            loginRes.setOneTimeSetPasswordCode(oneTimeSetPasswordCode);
            loginRes.setToSetPassword(1);
        }
        return loginRes1;
    }

    /**
     * 此处桶名称写死，如果改了桶名称aistudio也得改
     *
     * @param avatar
     * @return
     */
    private String getUniAvatarToLocal(String avatar) {
        if (!avatar.startsWith("http")) {
            return "";
        }
        try {
            // 下载图片到临时文件
            URL url = new URL(avatar);
            Path tempFile = Files.createTempFile("temp", ".jpg");
            InputStream in;
            in = url.openStream();
            Files.copy(in, tempFile);
            String iamgeUrl = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15);

            String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), iamgeUrl, in);
            log.info("imageUrl={}", fileUrl);
            String[] split = fileUrl.split("\\?");
            // 上传临时文件到 Minio

            // 删除临时文件
            Files.delete(tempFile);
            in.close();
            return split[0].split("/aistudio/")[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
