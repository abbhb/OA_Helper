package com.qc.printers.custom.user.domain.vo.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ThirdCallbackResp extends LoginRes implements Serializable {
    private String thirdType;

    /**
     * 如果是新用户先要进行绑定，如果绑定的电子邮箱是已经有的账户直接绑定，不然就一键注册并绑定
     */
    private boolean isNewUser;

    private String token;

    private boolean isCanLogin;

    /**
     * 用于新用户绑定一键注册时再次获取用户信息
     */
    private String thirdSocialUid;

    /**
     * 第三方当前昵称
     */
    private String thirdName;

    /**
     * 第三方当前头像
     */
    private String thirdAvatar;
}
