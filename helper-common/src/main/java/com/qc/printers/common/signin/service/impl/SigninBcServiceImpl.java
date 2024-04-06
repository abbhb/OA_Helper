package com.qc.printers.common.signin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.signin.dao.SigninBcDao;
import com.qc.printers.common.signin.domain.dto.SigninGroupDto;
import com.qc.printers.common.signin.domain.entity.*;
import com.qc.printers.common.signin.mapper.SigninGroupRuleMapper;
import com.qc.printers.common.signin.service.SigninBcService;
import com.qc.printers.common.signin.service.SigninGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.qc.printers.common.signin.utlis.TimeUtil.stringTimeToSecond;

@Slf4j
@Service
public class SigninBcServiceImpl implements SigninBcService {
    @Autowired
    private SigninBcDao signinBcDao;

    @Autowired
    private SigninGroupService signinGroupService;

    @Autowired
    private SigninGroupRuleMapper signinBcLambdaUpdateWrapper;

    @Transactional
    public void check(SigninBc signinBc) {
        if (StringUtils.isEmpty(signinBc.getName())) {
            throw new CustomException("班次名称不能为空");
        }
        if (signinBc.getEveryDay() == null) {
            throw new CustomException("每天几次上下班不能为空");
        }
        if (signinBc.getRules() == null || signinBc.getRules().size() != signinBc.getEveryDay()) {
            throw new CustomException("请检查班次和每日上下班时间");
        }
        List<BcRule> rules = signinBc.getRules();
        BcRule lastRule = null;
        for (BcRule rule : rules) {
            if (rule.getSbTime() == null || rule.getSbStartTime() == null || rule.getSbEndTime() == null) {
                throw new CustomException("请注意上班时间和上班时间段都得填写");
            }
            if (rule.getXbTime() == null || rule.getXbStartTime() == null || rule.getXbEndTime() == null) {
                throw new CustomException("请注意下班前多少分钟和下班后多少分钟允许签到都得填写");
            }

            if (lastRule != null) {
                if (stringTimeToSecond(rule.getSbTime()) <= stringTimeToSecond(lastRule.getXbTime())) {
                    throw new CustomException("第" + lastRule.getCount() + "," + rule.getCount() + ",两次上下班时间重叠");
                }
            }
            if (rule.getCount() != rules.size()) {
                if (rule.getCiRi().equals(1)) {
                    throw new CustomException("仅允许最后一班下班时间到次日");
                }
            }
            lastRule = rule;
        }
    }

    @Transactional
    @Override
    public String addSigninBc(SigninBc signinBc) {
        signinBc.setBak(0);
        signinBc.setId(null);
        this.check(signinBc);
        signinBcDao.save(signinBc);
        return "新建班次成功";
    }

    @Transactional
    @Override
    public String deleteSigninBc(String id) {

        SigninBc byId = signinBcDao.getById(Long.valueOf(id));
        if (byId == null) {
            throw new CustomException("删除失败");
        }
        if (signinBcLambdaUpdateWrapper.countSigninGroupByJsonKey(String.valueOf(byId.getId())) > 0) {
            throw new CustomException("请先解除该班次的绑定!");
        }

        LambdaUpdateWrapper<SigninBc> signinBcLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        signinBcLambdaUpdateWrapper.set(SigninBc::getBak, 1);
        signinBcLambdaUpdateWrapper.eq(SigninBc::getId, byId.getId());
        signinBcDao.update(signinBcLambdaUpdateWrapper);
        return "删除成功";
    }

    @Transactional
    @Override
    public String updateSigninBc(SigninBc signinBc) {
        Long bakId = signinBc.getId();
        signinBc.setBak(0);
        signinBc.setId(null);
        this.check(signinBc);
        signinBcDao.save(signinBc);
        // 更新已绑定该规则的考勤组
        List<SigninGroupRule> signinGroupRules = signinBcLambdaUpdateWrapper.listSigninGroupByJsonKey(String.valueOf(bakId));
        if (signinGroupRules != null && signinGroupRules.size() > 0) {
            for (SigninGroupRule signinGroupRule : signinGroupRules) {
                int bg = 0;
                SigninGroupDto signinGroup = signinGroupService.getSigninGroup(signinGroupRule.getGroupId());
                SigninGroupRule signinGroupRule1 = signinGroup.getSigninGroupRule();
                RulesInfo rulesInfo2 = signinGroupRule1.getRulesInfo();
                List<KQSJRule> kqsj = rulesInfo2.getKqsj();
                List<KQSJRule> kqsjN = new ArrayList<>();
                for (KQSJRule kqsjRule : kqsj) {
                    if (kqsjRule.getBcId().equals(bakId)) {
                        kqsjRule.setBcId(signinBc.getId());
                        bg = 1;
                    }
                    kqsjN.add(kqsjRule);
                }
                rulesInfo2.setKqsj(kqsjN);
                signinGroupRule1.setRulesInfo(rulesInfo2);
                signinGroup.setOnlyBasic(false);
                signinGroup.setSigninGroupRule(signinGroupRule1);
                if (bg != 0) {
                    signinGroupService.updateSigninGroup(signinGroup);
                }
            }

        }
        return "更新班次成功";
    }

    @Override
    public List<SigninBc> listSigninBc() {
        LambdaQueryWrapper<SigninBc> signinBcLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinBcLambdaQueryWrapper.eq(SigninBc::getBak, 0);
        return signinBcDao.list(signinBcLambdaQueryWrapper);
    }
}
