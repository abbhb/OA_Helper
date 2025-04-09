package com.qc.printers.common.ldap.domain.entity;

import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Set;

@Data
@Entry(objectClasses = {"groupOfNames"})
public class LdapDept {
    @Id
    private Name dn; // 自动生成DN

    @Attribute(name = "cn")
    private String cn; // 部门名

    @Attribute(name = "ou")
    private String ou; // 部门ID

    // rdn设置为ou


    @Attribute(name = "member")
    private Set<String> members = new HashSet<>(); // 至少包含1个用户

    // 生成DN的静态方法
    public static Name buildDn(String deptName, String parentDn) {
        return LdapNameBuilder.newInstance(parentDn)
                .add("ou", deptName)
                .build();
    }
}