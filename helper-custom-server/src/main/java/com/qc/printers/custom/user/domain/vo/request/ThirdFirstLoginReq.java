package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ThirdFirstLoginReq extends EmailReq implements Serializable {

    private String thirdSocialUid;

    private String thirdType;


}
