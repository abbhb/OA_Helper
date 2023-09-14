package com.qc.printers.custom.study.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.custom.study.domain.dto.AddClock30DTO;
import com.qc.printers.custom.study.domain.dto.ClockSelfDTO;
import com.qc.printers.custom.study.domain.vo.AdminDayDataParamsVO;
import com.qc.printers.custom.study.domain.vo.ClockSelfEchartsVO;
import com.qc.printers.custom.study.domain.vo.KeepDayDataVO;
import com.qc.printers.custom.study.service.StudyClockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController//@ResponseBody+@Controller
@RequestMapping("/study_clock")
@Slf4j
@CrossOrigin("*")
@Api("学习打卡相关")
public class StudyClockController {
    private final StudyClockService studyClockService;

    public StudyClockController(StudyClockService studyClockService) {
        this.studyClockService = studyClockService;
    }

    /**
     * 30秒打卡入库
     */
    @CrossOrigin("*")
    @PostMapping("/add_clock_30")
    @ApiOperation(value = "30秒打卡入库")
    @NeedToken
    public R<String> addClock30(@RequestBody AddClock30DTO addClock30DTO) {
        log.info("addClock30DTO={}", addClock30DTO);
        studyClockService.addClock30(addClock30DTO);
        return R.success("记录成功");
    }

    @CrossOrigin("*")
    @GetMapping("/get_self_clock")
    @ApiOperation(value = "获取本人情况")
    @NeedToken
    public R<ClockSelfDTO> getClockSelf() {
        log.info("获取本人情况");
        return R.success(studyClockService.getClockSelf());
    }

    @CrossOrigin("*")
    @GetMapping("/get_self_clock_all")
    @ApiOperation(value = "获取本人情况")
    @NeedToken
    public R<ClockSelfDTO> getClockSelfAll() {
        log.info("获取本人情况");
        return studyClockService.getClockSelfAll();
    }

    @CrossOrigin("*")
    @GetMapping("/get_self_clock_echarts")
    @ApiOperation(value = "获取本人Echarts折线图数据")
    @NeedToken
    public R<ClockSelfEchartsVO> getSelfClockEcharts() {
        log.info("获取本人Echarts折线图数据");
        return studyClockService.getSelfClockEcharts();
    }

    @CrossOrigin("*")
    @PostMapping("/get_admin_day_data")
    @ApiOperation(value = "获取日数据")
    @NeedToken
    @PermissionCheck(role = {"superadmin", "lsadmin"}, permission = "sys:study:list")
    public R<PageData<List<KeepDayDataVO>>> getAdminDayData(@RequestBody AdminDayDataParamsVO adminDayDataParamsVO) {
        log.info("获取日数据");
        return studyClockService.getAdminDayData(adminDayDataParamsVO);
    }

}
