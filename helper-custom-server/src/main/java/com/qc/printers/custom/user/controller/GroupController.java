package com.qc.printers.custom.user.controller;


import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.user.domain.entity.Group;
import com.qc.printers.custom.user.domain.vo.request.GroupAndUserVO;
import com.qc.printers.custom.user.domain.vo.response.Duplicate;
import com.qc.printers.custom.user.domain.vo.response.GroupAndUserFrontVO;
import com.qc.printers.custom.user.domain.vo.response.GroupTree;
import com.qc.printers.custom.user.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/group")
@Api("Group相关接口")
@CrossOrigin("*")
public class GroupController {
    @Autowired
    private GroupService groupService;

    /**
     * 增加群组
     *
     * @param
     * @return
     */
    @NeedToken
    @PostMapping("/addGroup")
    @PermissionCheck("1")
    @ApiOperation(value = "增加群组", notes = "")
    public R<String> addGroup(@RequestBody GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO == null) {
            return R.error("参数不能为空");
        }
        groupService.addGroup(groupAndUserVO);
        return R.success("增加成功");
    }

    /**
     * 删除群组
     *
     * @param
     * @return
     */
    @NeedToken
    @PostMapping("/deleteGroup")
    @PermissionCheck("1")
    @ApiOperation(value = "删除群组", notes = "")
    public R<String> deleteGroup(@RequestBody GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO == null) {
            return R.error("参数不能为空");
        }
        groupService.deleteGroup(groupAndUserVO);
        return R.successOnlyObject("删除成功");
    }

    /**
     * 强制删除群组
     *
     * @param
     * @return
     */
    @NeedToken
    @PostMapping("/forceDeleteGroup")
    @PermissionCheck("1")
    @ApiOperation(value = "删除群组", notes = "")
    public R<String> forceDeleteGroup(@RequestBody GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO == null) {
            return R.error("参数不能为空");
        }
        groupService.forceDeleteGroup(groupAndUserVO);
        return R.successOnlyObject("删除成功");
    }

    /**
     * 修改群组
     *
     * @param
     * @return
     */
    @NeedToken
    @PutMapping("/updateGroup")
    @PermissionCheck("1")
    @ApiOperation(value = "修改群组", notes = "")
    public R<String> updateGroup(@RequestBody GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO == null) {
            return R.error("参数不能为空");
        }
        groupService.updateGroup(groupAndUserVO);
        return R.successOnlyObject("修改成功");
    }

    /**
     * 查询群组
     *
     * @param
     * @return
     */
    @NeedToken
    @GetMapping("/queryGroup")
    @PermissionCheck("1")
    @ApiOperation(value = "查询群组", notes = "")
    public R<PageData<GroupAndUserFrontVO>> queryGroup(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize, String name) {
        if (pageNum == null || pageSize == null) {
            return R.error("参数不能为空");
        }
        return R.success(groupService.queryGroup(pageNum, pageSize, name));
    }

    /**
     * 查询群组可选列表
     *
     * @param
     * @return
     */
    @NeedToken
    @GetMapping("/queryGroupSelectList")
    @PermissionCheck("1")
    @ApiOperation(value = "查询群组可选列表", notes = "")
    public R<List<Group>> queryGroupSelectList() {
        return R.success(groupService.list());
    }

    @NeedToken
    @GetMapping("/is_duplicate")
    @PermissionCheck("1")
    @ApiOperation(value = "查询分组名是否重名", notes = "")
    public R<Duplicate> isDuplicate(String name) {
        if (StringUtils.isEmpty(name)) {
            return R.success(new Duplicate(false, null, null));
        }
        return R.success(groupService.isDuplicate(name));
    }


    /**
     * 获取待添加列表
     *
     * @param groupId 传入一个或多个groupId[使用”,“分割！],会自动将改组成员放入未分组!
     * @return
     */
    @NeedToken
    @GetMapping("/get_can_be_add")
    @PermissionCheck("1")
    @ApiOperation(value = "获取待添加列表", notes = "除了已有组的人全添加进未分组，包括本组")
    public R<List<GroupTree>> getCanBeAdd(String groupId) {
        return R.success(groupService.getCanBeAddWFZ(groupId));
    }

}
