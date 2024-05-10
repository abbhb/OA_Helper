package com.qc.printers.common.signin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.signin.dao.SigninGroupDao;
import com.qc.printers.common.signin.dao.SigninGroupRuleDao;
import com.qc.printers.common.signin.domain.dto.SigninGroupDto;
import com.qc.printers.common.signin.domain.entity.KQSJRule;
import com.qc.printers.common.signin.domain.entity.RulesInfo;
import com.qc.printers.common.signin.domain.entity.SigninGroup;
import com.qc.printers.common.signin.domain.entity.SigninGroupRule;
import com.qc.printers.common.signin.service.SigninGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class SigninGroupServiceImpl implements SigninGroupService {

    @Autowired
    private SigninGroupDao signinGroupDao;

    @Autowired
    private SigninGroupRuleDao signinGroupRuleDao;

    @Transactional
    public void check(SigninGroupDto signinGroupDto) {
        if (StringUtils.isEmpty(signinGroupDto.getSigninGroup().getName())) {
            throw new CustomException("考勤组名称不能为空");
        }
        if (signinGroupDto.getSigninGroupRule().getRulesInfo() == null) {
            throw new CustomException("考勤组规则详细不能为空");
        }
        if (signinGroupDto.getSigninGroupRule().getRulesInfo().getUserIds() == null) {
            throw new CustomException("考勤组绑定人员不能为空");
        }
        if (signinGroupDto.getSigninGroupRule().getRulesInfo().getUserIds().size() <= 0) {
            throw new CustomException("考勤组绑定人员不能为空");
        }
        if (signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninType() == null) {
            throw new CustomException("考勤组考勤模式不能为空");
        }
        if (signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninWays() == null || signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninWays().size() <= 0) {
            throw new CustomException("考勤组考勤方式不能为空");
        }
        if (signinGroupDto.getSigninGroupRule().getRulesInfo().getKqsj() == null || signinGroupDto.getSigninGroupRule().getRulesInfo().getKqsj().size() <= 0) {
            throw new CustomException("考勤最少包含一个时间段");
        }
        List<KQSJRule> kqsj = signinGroupDto.getSigninGroupRule().getRulesInfo().getKqsj();
        for (KQSJRule kqsjRule : kqsj) {
            Map<String, Objects> stringObjectsMap = new HashMap<>();
            if (StringUtils.isEmpty(kqsjRule.getXq())) {
                throw new CustomException("星期不能为空");
            }
            for (String s : kqsjRule.getXq().split(",")) {
                if (!stringObjectsMap.containsKey(s)) {
                    stringObjectsMap.put(s, null);
                } else {
                    throw new CustomException("星期重复!");
                }

            }

        }

    }

    @Transactional
    @Override
    public String addSigninGroup(SigninGroupDto signinGroupDto) {
        this.check(signinGroupDto);
        SigninGroup signinGroup = new SigninGroup();
        signinGroup.setName(signinGroupDto.getSigninGroup().getName());
        signinGroupDao.save(signinGroup);
        SigninGroupRule signinGroupRule = new SigninGroupRule();
        signinGroupRule.setGroupId(signinGroup.getId());
        signinGroupRule.setRev(1);
        signinGroupRule.setStartTime(LocalDate.now());
        RulesInfo rulesInfo = new RulesInfo();
        signinGroupRule.setRulesInfo(rulesInfo);
        rulesInfo.setSigninWays(signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninWays());
        rulesInfo.setSigninType(signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninType());
        rulesInfo.setKqsj(signinGroupDto.getSigninGroupRule().getRulesInfo().getKqsj());
        rulesInfo.setUserIds(signinGroupDto.getSigninGroupRule().getRulesInfo().getUserIds());
        signinGroupRuleDao.save(signinGroupRule);
        return "添加成功";
    }

    @Transactional
    @Override
    public String deleteSigninGroup(String id) {
        SigninGroup byId = signinGroupDao.getById(id);
        if (byId == null) {
            throw new CustomException("对象不存在");
        }
        byId.setIsRev(1);
        signinGroupDao.updateById(byId);
        return "删除成功";
    }

    public Boolean checkSigninGroupBasicUpdate(SigninGroupDto signinGroupDto, SigninGroup now) {
        if (!signinGroupDto.getSigninGroup().getName().equals(now.getName())) {
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public String updateSigninGroup(SigninGroupDto signinGroupDto) {
        if (signinGroupDto.getSigninGroup().getId() == null) {
            throw new CustomException("核心id不能为空");
        }
        SigninGroup signinGroup = signinGroupDao.getById(signinGroupDto.getSigninGroup().getId());
        if (signinGroup == null) {
            throw new CustomException("对象不能为空");
        }
        if (this.checkSigninGroupBasicUpdate(signinGroupDto, signinGroup)) {
            SigninGroup signinGroup1 = signinGroupDto.getSigninGroup();
            signinGroup1.setId(signinGroup.getId());
            signinGroup1.setIsRev(0);
            signinGroupDao.updateById(signinGroup1);

        }
        if (signinGroupDto.getOnlyBasic()) {
            return "更新成功";
        }
        LocalDate nowDate = LocalDate.now();
        this.check(signinGroupDto);

        LambdaQueryWrapper<SigninGroupRule> signinGroupRuleLambdaQueryWrapperSS = new LambdaQueryWrapper<>();
        signinGroupRuleLambdaQueryWrapperSS.eq(SigninGroupRule::getGroupId, signinGroup.getId());
        signinGroupRuleLambdaQueryWrapperSS.eq(SigninGroupRule::getEndTime, nowDate.plusDays(1));
        SigninGroupRule signinGroupRuleDaoOne = signinGroupRuleDao.getOne(signinGroupRuleLambdaQueryWrapperSS);
        if (signinGroupRuleDaoOne == null) {
            // 生成新的规则组,当今天没有做过更改，也就是没有从明天开始的规则的时候
            LambdaQueryWrapper<SigninGroupRule> signinGroupRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            signinGroupRuleLambdaQueryWrapper.eq(SigninGroupRule::getGroupId, signinGroup.getId());
            signinGroupRuleLambdaQueryWrapper.orderByDesc(SigninGroupRule::getRev); // 按照目标字段降序排列，以找到最大值
            signinGroupRuleLambdaQueryWrapper.select(SigninGroupRule::getId, SigninGroupRule::getRev);
            List<SigninGroupRule> list = signinGroupRuleDao.list(signinGroupRuleLambdaQueryWrapper);
            int maxs = 1;

            if (list.size() != 0) {
                maxs = list.get(0).getRev() + 1;
                SigninGroupRule byId = signinGroupRuleDao.getById(list.get(0).getId());
                if (byId == null) {
                    throw new CustomException("异常err:15");
                }
                byId.setEndTime(nowDate.plusDays(1));
                signinGroupRuleDao.updateById(byId);
            }
            SigninGroupRule signinGroupRule = new SigninGroupRule();
            signinGroupRule.setGroupId(signinGroup.getId());
            signinGroupRule.setRev(maxs);
            signinGroupRule.setStartTime(nowDate.plusDays(1));
            RulesInfo rulesInfo = new RulesInfo();
            signinGroupRule.setRulesInfo(rulesInfo);
            rulesInfo.setSigninWays(signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninWays());
            rulesInfo.setSigninType(signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninType());
            rulesInfo.setKqsj(signinGroupDto.getSigninGroupRule().getRulesInfo().getKqsj());
            rulesInfo.setUserIds(signinGroupDto.getSigninGroupRule().getRulesInfo().getUserIds());
            signinGroupRuleDao.save(signinGroupRule);
            return "更新成功";
        }
        // 否则就是更新规则，而不是直接生成了
        RulesInfo rulesInfo = new RulesInfo();
        rulesInfo.setSigninWays(signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninWays());
        rulesInfo.setSigninType(signinGroupDto.getSigninGroupRule().getRulesInfo().getSigninType());
        rulesInfo.setKqsj(signinGroupDto.getSigninGroupRule().getRulesInfo().getKqsj());
        rulesInfo.setUserIds(signinGroupDto.getSigninGroupRule().getRulesInfo().getUserIds());
        signinGroupRuleDaoOne.setRulesInfo(rulesInfo);
        signinGroupRuleDao.updateById(signinGroupRuleDaoOne);
        return "更新成功";

    }

    @Override
    public List<SigninGroupDto> listSigninGroup() {
        LambdaQueryWrapper<SigninGroup> signinGroupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinGroupLambdaQueryWrapper.eq(SigninGroup::getIsRev, 0);
        List<SigninGroup> list = signinGroupDao.list(signinGroupLambdaQueryWrapper);
        List<SigninGroupDto> signinGroupDtos = new ArrayList<>();
        for (SigninGroup signinGroup : list) {
            SigninGroupDto signinGroupDto = new SigninGroupDto();
            signinGroupDtos.add(signinGroupDto);
            signinGroupDto.setSigninGroup(signinGroup);
            LambdaQueryWrapper<SigninGroupRule> signinGroupRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            signinGroupRuleLambdaQueryWrapper.eq(SigninGroupRule::getGroupId, signinGroup.getId());
            signinGroupRuleLambdaQueryWrapper.orderByDesc(SigninGroupRule::getRev); // 按照目标字段降序排列，以找到最大值
            signinGroupRuleLambdaQueryWrapper.last("LIMIT 1");
            SigninGroupRule signinGroupRule = signinGroupRuleDao.getOne(signinGroupRuleLambdaQueryWrapper);
            signinGroupDto.setSigninGroupRule(signinGroupRule);
        }
        return signinGroupDtos;
    }

    @Override
    public SigninGroupDto getSigninGroup(Long groupId) {
        LambdaQueryWrapper<SigninGroup> signinGroupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinGroupLambdaQueryWrapper.eq(SigninGroup::getIsRev, 0);
        signinGroupLambdaQueryWrapper.eq(SigninGroup::getId, groupId);
        SigninGroup signinGroup = signinGroupDao.getOne(signinGroupLambdaQueryWrapper);
        SigninGroupDto signinGroupDto = new SigninGroupDto();
        signinGroupDto.setSigninGroup(signinGroup);
        LambdaQueryWrapper<SigninGroupRule> signinGroupRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinGroupRuleLambdaQueryWrapper.eq(SigninGroupRule::getGroupId, signinGroup.getId());
        signinGroupRuleLambdaQueryWrapper.orderByDesc(SigninGroupRule::getRev); // 按照目标字段降序排列，以找到最大值
        signinGroupRuleLambdaQueryWrapper.last("LIMIT 1");
        SigninGroupRule signinGroupRule = signinGroupRuleDao.getOne(signinGroupRuleLambdaQueryWrapper);
        signinGroupDto.setSigninGroupRule(signinGroupRule);
        return signinGroupDto;
    }
}
