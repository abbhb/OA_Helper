package com.qc.printers.custom.user.service;


import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.user.domain.dto.SummeryInfoDTO;
import com.qc.printers.common.user.domain.dto.UserInfoBaseExtDto;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.entity.UserExtBase;
import com.qc.printers.common.user.domain.vo.request.user.SummeryInfoReq;
import com.qc.printers.common.user.domain.vo.response.user.UserInfoBaseExtStateResp;
import com.qc.printers.custom.user.domain.dto.LoginDTO;
import com.qc.printers.custom.user.domain.vo.request.LoginByEmailCodeReq;
import com.qc.printers.custom.user.domain.vo.request.PasswordByOneTimeCodeReq;
import com.qc.printers.custom.user.domain.vo.request.PasswordR;
import com.qc.printers.custom.user.domain.vo.request.ResetReq;
import com.qc.printers.custom.user.domain.vo.response.*;

import java.util.List;

public interface UserService {
//    R<UserResult> login(String code);

//    R<UserResult> loginFirst(User user);

    R<String> createUser(User user, Long userId);

    R<String> logout(String token);

    R<LoginRes> loginByToken();

    Integer count();

    boolean updateUserStatus(String id, String status);

    R<UserResult> updateForUser(User user);

    R<UserResult> updateForUserSelf(User user);


    R<String> updateUser(String userid, String name, String username, String phone, String idNumber, String status, String grouping, String sex, String token);

    PageData<UserResult> getUserList(User ua,Integer pageNum, Integer pageSize, String name, Integer mustHaveStudentId, Integer cascade, Long deptId,String level);

    R<String> deleteUsers(String id);

    R<String> hasUserName(String username);

    R<String> emailWithUser(String emails, String code, String token);

    R<LoginRes> login(LoginDTO user);

    R<UserResult> info();

    boolean updateUserInfo(User user);

    Integer userPassword();

    boolean setPassword(PasswordR passwordR);

    boolean updateByAdmin(UserResult user);

    List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req);

    RegisterResp emailRegister(String email, String password);


    ForgetPasswordResp forgetPasswordByEmail(String email, String password);

    User loginPublic(String username, String password);

    boolean setPasswordByOneTimeCodeReq(PasswordByOneTimeCodeReq passwordR);

    LoginRes loginByEmailCode(LoginByEmailCodeReq loginByEmailCodeReq);

    UserSelectListResp userSelectList(String name);

    PageData<UserResult> getUserListForBpm(Integer pageNum, Integer pageSize, String name, Long deptId);

    UserSelectListResp userSelectOnlyXUserList(Long deptId);

    UserInfoBaseExtStateResp userinfoExtMy();

    String userinfoExtMyApplyFor(UserInfoBaseExtDto userExtBase);

    String userinfoExtMyWithDraw();

    UserInfoBaseExtDto approvalUserinfoExtData(String taskId);

    void updateUserInfoExt(Long userId, UserInfoBaseExtDto userInfoBaseExtDto);

    List<User> exportAllData();

    String importUserData(List<User> dataList);

    ResetResp resetPassword(ResetReq resetReq);

    List<String> levels();
}
