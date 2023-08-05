package com.qc.printers.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.R;
import com.qc.printers.pojo.PageData;
import com.qc.printers.pojo.User;
import com.qc.printers.pojo.dto.LoginDTO;
import com.qc.printers.pojo.vo.LoginRes;
import com.qc.printers.pojo.vo.PasswordR;
import com.qc.printers.pojo.vo.UserResult;

public interface UserService extends IService<User> {
//    R<UserResult> login(String code);

//    R<UserResult> loginFirst(User user);

    R<String> createUser(User user,Long userId);

    R<String> logout(String token);

    R<LoginRes> loginByToken();

    boolean updateUserStatus(String id, String status);

    R<UserResult> updateForUser(User user);

    R<UserResult> updateForUserSelf(User user);


    R<String> updateUser(String userid, String name, String username, String phone, String idNumber, String status, String grouping, String sex, String token);

    PageData<UserResult> getUserList(Integer pageNum, Integer pageSize, String name);

    R<String> deleteUsers(String id,Long userId);

    R<String> hasUserName(String username);

    R<String> emailWithUser(String emails, String code, String token);

    R<LoginRes> login(LoginDTO user);

    R<UserResult> info();

    boolean updateUserInfo(User user);

    Integer userPassword();

    boolean setPassword(PasswordR passwordR);
}
