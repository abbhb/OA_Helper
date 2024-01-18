package com.qc.printers.common.oauth.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysOauth implements Serializable {
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 客户端id
     */
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_name")
    private String clientName;


    /**
     * 客户端秘钥
     */
    @JsonProperty("client_secret")
    private String clientSecret;
    /**
     * 回调地址，成功回调会自动加上返回参数code
     */
    @JsonProperty("redirect_uri")
    private String redirectUri;

    /**
     * 客户端name
     */
    @JsonProperty("client_image")
    private String clientImage;

    @JsonProperty("domain_name")
    private String domainName;

    @JsonProperty("no_sert_redirect")
    private Integer noSertRedirect;
    /**
     * 是否强制配置的回调地址，默认为0
     * 1为强制使用配置回调
     */
    @JsonProperty("force_configuration_redirect")
    private Integer forceConfigurationRedirect;


    /**
     * 授权类型：grant_type ，code为authorization_code（授权码模式）
     */
    @JsonProperty("grant_type")
    private String grantType;


    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @JsonProperty("is_deleted")
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;

    @JsonProperty("status")
    private Integer status;
}
