package com.qc.printers.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.R;
import com.qc.printers.pojo.StudyClock;
import com.qc.printers.pojo.dto.AddClock30DTO;
import com.qc.printers.pojo.dto.ClockSelfDTO;

public interface StudyClockService extends IService<StudyClock> {
    void addClock30(AddClock30DTO addClock30DTO);

    ClockSelfDTO getClockSelf();

    R<ClockSelfDTO> getClockSelfAll();
}
