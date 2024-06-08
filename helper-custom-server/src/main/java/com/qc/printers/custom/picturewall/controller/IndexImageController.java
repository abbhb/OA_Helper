package com.qc.printers.custom.picturewall.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.picturewall.domain.entity.IndexImage;
import com.qc.printers.custom.picturewall.domain.vo.IndexImageAddReq;
import com.qc.printers.custom.picturewall.domain.vo.IndexImageAddResp;
import com.qc.printers.custom.picturewall.service.IndexImageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController//@ResponseBody+@Controller
@RequestMapping("/index_image")
@Slf4j
@CrossOrigin("*")
@Api("首页图片通知")
public class IndexImageController {
    private final IndexImageService indexImageService;

    public IndexImageController(IndexImageService indexImageService) {
        this.indexImageService = indexImageService;
    }

    @GetMapping("/all_label")
    @NeedToken
    @ApiOperation(value = "获取所有的不同标签", notes = "")
    public R<List<String>> allLabel() {
        log.info("获取所有的不同标签");
        return R.success(indexImageService.allLabel());
    }

    @GetMapping("/label_all")
    @NeedToken
    @ApiOperation(value = "获取该标签的image", notes = "")
    public R<IndexImage> labelImage(String label) {
        return R.success(indexImageService.labelImage(label));
    }

    @PostMapping("/add")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:indeximage:add")
    @ApiOperation(value = "添加首页图片通知", notes = "")
    public R<String> addIndexImage(@RequestBody IndexImageAddReq indexImage) {
        log.info("indexImage{}",indexImage);
        if (indexImage == null) {
            return R.error("请检查");
        }
        if (indexImage.getIndexImage().getData()==null||indexImage.getIndexImage().getData().size()==0) {
            return R.error("必须包含图片");
        }
        if (StringUtils.isEmpty(indexImage.getIndexImage().getLabel())) {
            return R.error("必须包含标签");
        }
        if (indexImage.getIndexImage().getSort() == null) {
            return R.error("请输入排序");
        }
        return R.successOnlyObject(indexImageService.addIndexImage(indexImage));
    }

    @PutMapping("/update")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:indeximage:update")
    @ApiOperation(value = "更新首页图片通知", notes = "")
    public R<String> updateIndexImage(@RequestBody IndexImageAddReq indexImage) {
        if (indexImage == null) {
            return R.error("请检查");
        }
        if (indexImage.getIndexImage()==null) {
            return R.error("IndexImage不能为空");
        }
        return R.successOnlyObject(indexImageService.updateIndexImage(indexImage));
    }

    @DeleteMapping("/delete")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:indeximage:delete")
    @ApiOperation(value = "删除首页图片通知", notes = "")
    public R<String> deleteIndexImage(@RequestParam(name = "id", required = true) Long id) {
        return R.successOnlyObject(indexImageService.deleteIndexImage(id));
    }

    @GetMapping("/list")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:indeximage:list")
    @ApiOperation(value = "首页图片通知列表", notes = "")
    public R<List<IndexImageAddResp>> deleteIndexImage() {
        return R.success(indexImageService.list());
    }
}
