package com.qc.printers.common.signin.service;


import com.qc.printers.common.signin.domain.dto.SigninGroupDto;

import java.util.List;

public interface SigninGroupService {
    String addSigninGroup(SigninGroupDto signinGroupDto);

    String deleteSigninGroup(String id);

    String updateSigninGroup(SigninGroupDto signinGroupDto);

    List<SigninGroupDto> listSigninGroup();

    SigninGroupDto getSigninGroup(Long groupId);
}
