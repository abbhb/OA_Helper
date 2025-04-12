package com.qc.printers.common.ldap.utils;

import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DnBuilder {
    // todo: 后面变得不再写死
    // 用户DN示例：cn=zhangsan,ou=users
    public static LdapName buildUserDn(String groupName,User user) {
        return LdapNameBuilder.newInstance("ou=%s".formatted(groupName))
                .add("cn=" + user.getUsername())
                .build();
    }


    public static LdapName buildDn(String rdn) {
        return LdapNameBuilder.newInstance(rdn)
                .build();
    }

    // 组织DN示例：cn=技术部,ou=groups
    public static LdapName buildGroupDn(List<String> deptHierarchy) {
        if (deptHierarchy == null || deptHierarchy.isEmpty()) {
            throw new IllegalArgumentException("部门层级列表不能为空");
        }

        // 逆序处理部门层级（根部门在最后）
        List<String> reversedHierarchy = new ArrayList<>(deptHierarchy);
        Collections.reverse(reversedHierarchy);

        // 构建DN
        LdapNameBuilder builder = LdapNameBuilder.newInstance("ou=groups");
        reversedHierarchy.forEach(deptName -> {
            String encodedName = LdapEncoder.nameEncode(deptName); // 处理特殊字符
            builder.add("cn=" + encodedName);
        });

        return builder.build();
    }
}
