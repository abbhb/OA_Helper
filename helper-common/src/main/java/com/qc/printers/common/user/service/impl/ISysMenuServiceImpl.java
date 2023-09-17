package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.domain.entity.SysMenu;
import com.qc.printers.common.user.mapper.SysMenuMapper;
import com.qc.printers.common.user.service.ISysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ISysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public Integer getHierarchicalSeries(Long thisId) {
        if (thisId.equals(0L)) {
            return 0;
        }
        int cengji = 0;
        SysMenu byId = this.getById(thisId);
        if (byId == null) {
            throw new CustomException("层级获取失败");
        }
        cengji += 1;
        while (byId != null) {
            byId = this.getById(byId.getParentId());
            if (byId == null) {
                break;
            }
            cengji++;
        }

        return cengji;
    }

}
