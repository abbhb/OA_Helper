package com.qc.printers.custom.print.controller;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.entity.PrintDocumentTypeStatistic;
import com.qc.printers.common.print.domain.vo.CountTop10VO;
import com.qc.printers.common.print.domain.vo.request.PreUploadPrintFileReq;
import com.qc.printers.common.print.domain.vo.response.UnoServiceInfo;
import com.qc.printers.custom.print.domain.vo.PrinterResult;
import com.qc.printers.custom.print.domain.vo.request.PrintFileReq;
import com.qc.printers.custom.print.domain.vo.request.QueryHistoryPrintsReq;
import com.qc.printers.custom.print.domain.vo.response.*;
import com.qc.printers.custom.print.service.PrinterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController//@ResponseBody+@Controller
@RequestMapping("/printer")
@Slf4j
@CrossOrigin("*")
@Api("共享打印相关api")
public class PrintController {

    private final CommonService commonService;

    private final PrinterService printerService;

    @Autowired
    public PrintController(CommonService commonService, PrinterService printerService) {
        this.commonService = commonService;
        this.printerService = printerService;
    }

    @CrossOrigin("*")
    @PostMapping("/add_print_log")
    @ApiOperation(value = "打印日志提交接口")
    public R<Integer> addPrintLog(MultipartFile file, @RequestParam(value = "file_name") String fileName, @RequestParam(value = "duplex") String duplex,@RequestParam(value = "page_start") String pageStart, @RequestParam(value = "page_end") String pageEnd, @RequestParam(value = "copies_num") String copiesNum,@RequestParam(value = "username") String username) {
        log.info("pageStart={},pageEnd={},copiesNum={},username={},duplex={}", pageStart, pageEnd, copiesNum,username,duplex);
        return printerService.addPrinterLog(file, Integer.valueOf(pageStart), Integer.valueOf(pageEnd), Integer.valueOf(copiesNum),username,Integer.valueOf(duplex),fileName);
    }

    
    /**
     * 获取历史打印记录
     * 需要分页
     * 将管理员接口和用户接口分离 方便接入权限过滤器
     * 此处没必要校验token 后期通过权限注解标注1需要管理员权限即可
     *
     * @param pageNum  分页之当前页
     * @param pageSize 分页之页面最大
     * @return
     */
    @PostMapping("/getAllHistoryPrints/{page_num}/{page_size}")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:print:alldata")
    @ApiOperation(value = "获取历史打印记录", notes = "所有人历史记录：需要有管理员权限")
    public R<PageData<PrinterResult>> getAllHistoryPrints(@PathVariable("page_num") Integer pageNum, @PathVariable("page_size") Integer pageSize, @RequestBody QueryHistoryPrintsReq data) {
        if (pageNum == null) {
            return R.error("传参错误");
        }
        if (pageSize == null) {
            return R.error("传参错误");
        }
        if (pageSize > 100) {
            return R.error("传参错误");
        }
        //默认只展示时打印了的历史文件
        return printerService.listAllPrinter(pageNum, pageSize, data.getName(), data.getOnlyPrinted() == null ? 1 : data.getOnlyPrinted(), data.getStartDate(), data.getEndDate(), data.getOnlyUser());
    }
    

    @GetMapping("/getUserPrintTopList")
    @NeedToken
    @ApiOperation(value = "获取打印榜前30名用户", notes = "会排序好返回")
    public R<List<CountTop10VO>> getUserPrintTopList(@ApiParam("type:1为总，2为每天") Integer type) {
        return printerService.getUserPrintTopList(type);
    }

    @GetMapping("/getPrintDocumentTypeStatistics")
    @NeedToken
    @ApiOperation(value = "获取文件拓展名榜")
    public R<List<PrintDocumentTypeStatistic>> getPrintDocumentTypeStatistics() {
        return R.success((List<PrintDocumentTypeStatistic>) RedisUtils.get(MyString.print_document_type_statistic, List.class));
    }

    /**
     * 获取历史打印记录
     * 需要分页
     * 将管理员接口和用户接口分离 方便接入权限过滤器
     *
     * @param pageNum  分页之当前页
     * @param pageSize 分页之页面最大
     * @return
     */
    @PostMapping("/getMyHistoryPrints/{page_num}/{page_size}")
    @ApiOperation(value = "获取本人历史打印记录", notes = "因为没有token过不了needtoken，所以没必要再次校验token")
    @NeedToken
    public R<PageData<PrinterResult>> getMyHistoryPrints(@PathVariable("page_num") Integer pageNum, @PathVariable("page_size") Integer pageSize, @RequestBody QueryHistoryPrintsReq data) {
        if (pageNum == null) {
            return R.error("传参错误");
        }
        if (pageSize == null) {
            return R.error("传参错误");
        }
        if (pageSize > 100) {
            return R.error("传参错误");
        }
        return printerService.listPrinter(pageNum, pageSize, data.getName(), data.getOnlyPrinted() == null ? 1 : data.getOnlyPrinted(), data.getStartDate(), data.getEndDate());
    }

    @DeleteMapping("/deleteHistoryPrints/{id}")
    @NeedToken
    public R<String> deleteHistoryPrints(@PathVariable("id") Long id) {
        if (id == null) {
            throw new CustomException("传参错误");
        }
        return R.success(printerService.deleteHistoryPrints(id));
    }

    //获取当日打印数
    @GetMapping("/day_print_count")
    @ApiOperation(value = "获取当日打印数", notes = "因为没有token过不了needtoken，所以没必要再次校验token")
    @NeedToken
    public R<Integer> getTodayPrintCount() {
        return printerService.getTodayPrintCount();
    }


    /**
     * 此接口升级，兼容性添加hash，如果没传就后端动态添加
     * @param file
     * @param hash 默认认为前端应该携带md5的hash值
     * @return
     */
    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/uploadPrintFile")
    @ApiOperation("上传需要打印的文件")
    public R<String> uploadPrintFile(MultipartFile file,@RequestParam(name = "hash",required = false) String hash) {
        return R.successOnlyObject(printerService.uploadPrintFile(file,hash));
    }
    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/pre-uploadPrintFile")
    @ApiOperation("打印预处理")
    public R<String> preUploadPrintFile(@RequestBody PreUploadPrintFileReq printFileReq) {
        return R.successOnlyObject(printerService.preUploadPrintFile(printFileReq));
    }

    /**
     * 这个接口会直接开始打印，用于win客户端
     * @param file
     * @param copies
     * @param duplex
     * @param startNum
     * @param endNum
     * @param landscape
     * @param total
     * @param deviceId
     * @return
     */
    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/uploadPrintFileForWindows")
    @ApiOperation("已经确保为pdf格式了，直接到第五步打印")
    public R<String> uploadPrintFileForWindows(  @RequestParam("file") MultipartFile file,
                                                 @RequestParam("copies") Integer copies,
                                                 @RequestParam("duplex") Integer duplex,
                                                 @RequestParam("start_num") Integer startNum,
                                                 @RequestParam("end_num") Integer endNum,
                                                 @RequestParam("landscape") Integer landscape,
                                                 @RequestParam("total") Integer total,
                                                 @RequestParam("device_id") String deviceId) {
        PrintFileReq printFileReq = new PrintFileReq();
        printFileReq.setCopies(copies);
        printFileReq.setLandscape(landscape);
        printFileReq.setDeviceId(deviceId);
        printFileReq.setEndNum(endNum);
        printFileReq.setStartNum(startNum);
        printFileReq.setIsDuplex(duplex);
        return R.successOnlyObject(printerService.uploadPrintFileForWin(file,printFileReq,total));
    }

    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/print_file")
    @ApiOperation("打印文件")
    public R<String> printFile(@RequestBody PrintFileReq printFileReq) {
        return R.successOnlyObject(printerService.printFile(printFileReq));
    }

    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/print_device polling")
    @ApiOperation("设备轮询接口，获取哪些打印机注册了服务，且正常")
    public R<List<PrintDeviceResp>> printDevicePolling() {
        return R.success(printerService.printDevicePolling());
    }

    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/print_device_info polling/{id}")
    @ApiOperation("设备轮询接口，获取哪些打印机注册了服务，且正常")
    public R<PrintDeviceInfoResp> printDeviceInfoPolling(@PathVariable("id") String id) {
        return R.success(printerService.printDeviceInfoPolling(id));
    }


    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/uno_service_info polling")
    @ApiOperation("转换消费者指标等信息")
    public R<UnoServiceInfo> unoServiceInfo() {
        return R.success(printerService.unoServiceInfo());
    }

    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/print_cancel/{id}/{deviceId}")
    @ApiOperation("取消打印任务")
    public R<String> printCancel(@PathVariable("id") String id, @PathVariable("deviceId") String deviceId) {
        printerService.cancelPrint(id, deviceId);
        return R.successOnlyObject("取消成功，请不要重复点击！");
    }


    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/thumbnail polling")
    @ApiOperation("缩略图轮询接口，查询缩略图状态，有就返回，没告诉前端")
    public R<PrinterBaseResp<PrintImageResp>> thumbnailPolling(@RequestParam(name = "id") Long id) {
        return R.success(printerService.thumbnailPolling(id));
    }

    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/file_configuration_polling")
    @ApiOperation("文件配置轮询接口，有就返回，没告诉前端")
    public R<PrinterBaseResp<PrintFileConfigResp>> fileConfigurationPolling(@RequestParam(name = "id") Long id) {
        return R.success(printerService.fileConfigurationPolling(id));
    }

}
