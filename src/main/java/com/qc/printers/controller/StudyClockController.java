package com.qc.printers.controller;

import com.qc.printers.common.R;
import com.qc.printers.common.annotation.NeedToken;
import com.qc.printers.pojo.dto.AddClock30DTO;
import com.qc.printers.pojo.dto.ClockSelfDTO;
import com.qc.printers.service.StudyClockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}
