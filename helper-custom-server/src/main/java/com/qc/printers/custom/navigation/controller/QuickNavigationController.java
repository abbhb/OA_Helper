package com.qc.printers.custom.navigation.controller;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.domain.vo.selectOptionsResult;
import com.qc.printers.common.navigation.domain.entity.QuickNavigationItem;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationCategorizeResult;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationItemResult;
import com.qc.printers.custom.navigation.domain.vo.QuickNavigationResult;
import com.qc.printers.custom.navigation.domain.vo.req.QuickNavigationCategorizeUpdateReq;
import com.qc.printers.custom.navigation.service.QuickNavigationCategorizeService;
import com.qc.printers.custom.navigation.service.QuickNavigationItemService;
import com.qc.printers.custom.navigation.service.QuickNavigationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController//@ResponseBody+@Controller
@RequestMapping("/quicknavigation")
@Api("导航页相关接口")
@CrossOrigin("*")
@Slf4j
public class QuickNavigationController {


    private final QuickNavigationCategorizeService quickNavigationCategorizeService;
    private final QuickNavigationService quickNavigationService;
    private final QuickNavigationItemService quickNavigationItemService;

    @Autowired
    public QuickNavigationController(QuickNavigationCategorizeService quickNavigationCategorizeService, QuickNavigationService quickNavigationService, QuickNavigationItemService quickNavigationItemService) {
        this.quickNavigationCategorizeService = quickNavigationCategorizeService;
        this.quickNavigationService = quickNavigationService;
        this.quickNavigationItemService = quickNavigationItemService;
    }

    @NeedToken
    @GetMapping("/list")
    @ApiOperation("返回可以展导航页内容")
    public R<List<QuickNavigationResult>> list() {
        return quickNavigationService.list();
    }

    /**
     * 导航分类管理系统\管理员
     * @return
     */
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-c:list")
    @GetMapping("/listnavfenlei")
    @ApiOperation("导航分类管理系统")
    //后期可以传回token拿到用户信息
    public R<PageData<QuickNavigationCategorizeResult>> listNavFenLei(Integer pageNum, Integer pageSize, String name) {
        return quickNavigationCategorizeService.listNavFenLei(pageNum,pageSize,name);

    }

    /**
     * 权限等用注解后期实现,通过过滤器
     * @param quickNavigationItem
     * @return
     */
    @ApiOperation("创建导航内容")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-item:add")
    @PostMapping("/createItem")
    public R<String> createItem(@RequestBody QuickNavigationItem quickNavigationItem){
//        System.out.println("quickNavigationItem = " + quickNavigationItem);

        return quickNavigationItemService.createNavItem(quickNavigationItem);

    }

    /**
     * 权限等用注解后期实现,通过过滤器
     *
     * @param quickNavigationCategorize
     * @return
     */
    @ApiOperation("创建导航分类")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-c:add")
    @PostMapping("/createCategorize")
    public R<String> createCategorize(@RequestBody QuickNavigationCategorizeUpdateReq quickNavigationCategorize) {
//        System.out.println("quickNavigationCategorize = " + quickNavigationCategorize);

        return quickNavigationCategorizeService.createNavCategorize(quickNavigationCategorize);

    }

    /**
     * 导航分类管理系统
     *
     * @return
     */
    @NeedToken
    @ApiOperation("返回导航分类item")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-item:list")
    @GetMapping("/listnavfenleiitem")
    //后期可以传回token拿到用户信息
    public R<PageData<QuickNavigationItemResult>> listNavFenLeiItem(Integer pageNum, Integer pageSize, String name, String selectCate) {
//        log.info("selectCate={}",selectCate);
        return quickNavigationItemService.listNavFenLeiItem(pageNum, pageSize, name, selectCate);

    }

    /**
     * @return 返回分类选择的列表
     */
    @NeedToken
    @GetMapping("/getCategorizeSelectOptionsList")
    //后期可以传回token拿到用户信息
    public R<List<selectOptionsResult>> getCategorizeSelectOptionsList() {
        return quickNavigationCategorizeService.getCategorizeSelectOptionsList();

    }


    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-c:update")
    @PutMapping("/updataforquicknavigationcategorize")
    public R<String> updataForQuickNavigationCategorize(@RequestBody QuickNavigationCategorizeUpdateReq quickNavigation) {

        if (StringUtils.isEmpty(quickNavigation.getQuickNavigationCategorize().getName())) {
            return R.error("更新失败");
        }
        if (quickNavigation.getQuickNavigationCategorize().getId() == null) {
            return R.error("更新失败");
        }
        if (quickNavigation.getQuickNavigationCategorize().getVisibility() == null) {
            throw new CustomException("更新失败-请设置可见性");
        }
        if (quickNavigation.getQuickNavigationCategorize().getVisibility().equals(1)) {
            if (quickNavigation.getVisDeptIds() == null || quickNavigation.getVisDeptIds().size() == 0) {
                throw new CustomException("更新失败-最少保证有部门可见");
            }
        }
        return quickNavigationCategorizeService.updataForQuickNavigationCategorize(quickNavigation);
    }

    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-item:update")
    @PutMapping("/updataforquicknavigationitem")
    public R<String> updataForQuickNavigationItem(@RequestBody QuickNavigationItem quickNavigationItem){

        if (StringUtils.isEmpty(quickNavigationItem.getName())){
            return R.error("更新失败");
        }
        if (quickNavigationItem.getId()==null){
            return R.error("更新失败");
        }

        if (quickNavigationItem.getType()==null){
            throw new CustomException("必参缺少");
        }
        if (quickNavigationItem.getType().equals(0)) {
            if(StringUtils.isEmpty(quickNavigationItem.getPath())){
                throw new CustomException("必参缺少");
            }
        }
        if (quickNavigationItem.getType().equals(1)) {
            if(StringUtils.isEmpty(quickNavigationItem.getContent())){
                throw new CustomException("必参缺少");
            }
        }
        return quickNavigationItemService.updataForQuickNavigationItem(quickNavigationItem);
    }

    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-c:delete")
    @DeleteMapping("/deleteCategorize")
    public R<String> deleteNavigationCategorize(String id){
        log.info("id = {}",id);
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        return quickNavigationCategorizeService.deleteNavigationCategorize(id);

    }

    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:nav-item:delete")
    @DeleteMapping("/deleteItem")
    public R<String> deleteNavigationItem(String id){
        log.info("id = {}",id);
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        return quickNavigationItemService.deleteNavigationItem(id);

    }

}
