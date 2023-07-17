package com.qc.printers.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.R;
import com.qc.printers.pojo.PageData;
import com.qc.printers.pojo.StudyClock;
import com.qc.printers.pojo.dto.AddClock30DTO;
import com.qc.printers.pojo.dto.ClockSelfDTO;
import com.qc.printers.pojo.vo.ClockSelfEchartsVO;
import com.qc.printers.pojo.vo.KeepDayDataVO;

import java.util.List;

public interface StudyClockService extends IService<StudyClock> {
    void addClock30(AddClock30DTO addClock30DTO);

    ClockSelfDTO getClockSelf();

    R<ClockSelfDTO> getClockSelfAll();

    R<ClockSelfEchartsVO> getSelfClockEcharts();

    R<PageData<List<KeepDayDataVO>>> getAdminDayData(Integer pageNum, Integer pageSize, String name, String date, String groupId);
}
