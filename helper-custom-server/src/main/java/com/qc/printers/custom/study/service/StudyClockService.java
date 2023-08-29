package com.qc.printers.custom.study.service;


import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.custom.study.domain.dto.AddClock30DTO;
import com.qc.printers.custom.study.domain.dto.ClockSelfDTO;
import com.qc.printers.custom.study.domain.vo.AdminDayDataParamsVO;
import com.qc.printers.custom.study.domain.vo.ClockSelfEchartsVO;
import com.qc.printers.custom.study.domain.vo.KeepDayDataVO;

import java.util.List;

public interface StudyClockService {
    void addClock30(AddClock30DTO addClock30DTO);

    ClockSelfDTO getClockSelf();

    R<ClockSelfDTO> getClockSelfAll();

    R<ClockSelfEchartsVO> getSelfClockEcharts();

    R<PageData<List<KeepDayDataVO>>> getAdminDayData(AdminDayDataParamsVO adminDayDataParamsVO);
}
