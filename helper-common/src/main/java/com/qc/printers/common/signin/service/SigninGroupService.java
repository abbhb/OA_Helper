package com.qc.printers.common.signin.service;


import com.qc.printers.common.holidays.domain.Holidays;
import com.qc.printers.common.signin.domain.dto.SigninGroupDto;

import java.time.LocalDate;
import java.util.List;

public interface SigninGroupService {
    String addSigninGroup(SigninGroupDto signinGroupDto);

    String deleteSigninGroup(String id);

    String updateSigninGroup(SigninGroupDto signinGroupDto);

    List<SigninGroupDto> listSigninGroup();

    SigninGroupDto getSigninGroup(Long groupId);

    List<Holidays> listHolidays(Long groupId, LocalDate startDate, LocalDate endDate);

    String updateHolidays(Long groupId, Holidays data);

    String deleteHolidays(Long groupId,Long id);
}
