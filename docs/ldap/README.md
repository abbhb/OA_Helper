# 同步组织架构至LDAP

## 密码获取方式
新加一个RSA密码字段在user表中，密码字段的值为用户的密码，密码加密方式为RSA非堆成加密

## 同步用户至ldap
仅同步存在RSA密码的用户（新版本后登录过，登录时密码核对通过既顺便写入该表）

- 存在的部门，从根部门一层层往下，如果没变化跳过，mysql不存在但ldap存在的的删除，反之新增
- 存在的用户，如果没变化跳过，mysql不存在但ldap存在的的删除，反之新增 （对应OU）

公钥（加密）：
```plaintext
-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3kfWDkmf4o6O54vqImtC6fWutPdqZxPaSB4VC7BqatQX9/8KXsSZ2+2zd9pOhPcULY/F1QNl2XAnguMZ+b99KKkJ1IpgPD7gLzslo+okj2a0hTfzJBIQw/UOofKWWepExW1JCY3w1Ah2E1SbplsyLtXAAjh7ouV0iUhvZn+JRmwIDAQAB
-----END PUBLIC KEY-----
```
私钥（解密）：
```plaintext
-----BEGIN RSA PRIVATE KEY-----
MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALeR9YOSZ/ijo7ni+oia0Lp9a6092pnE9pIHhULsGpq1Bf3/wpexJnb7bN32k6E9xQtj8XVA2XZcCeC4xn5v30oqQnUimA8PuAvOyWj6iSPZrSFN/MkEhDD9Q6h8pZZ6kTFbUkJjfDUCHYTVJumWzIu1cACOHui5XSJSG9mf4lGbAgMBAAECgYA9XmzjIw9iNqa2LrUN9R/BsMtOG+cYUBoUYLJC2LbeMJWDwDy4RK90yIIxRE0/cuyMbcmbpuXsZUGiIHOvckwFqK0uyF6BPeeV+QkPVmAL1wO3N8kEbMxakPbeNfFsYu/vG9esDe8jHUj65UPcGtQoGd7jQ/Jcy87kUH4UdoMcAQJBAORxhz/9+nc+06XYnXJrVmdPXi3D/szDixeurYQbU9ZrXgerJWrmq8pr8g6Nntkznyt7cR/l2x3k0VY5GSB2BZsCQQDNtrc4DXaAIAuVGN5HtpRrkLWeYDjvjRqP5psROMxoeLzXuvRcTXpBrUh1vtLoo8YXSNIpilDF887BqFqLT6QBAkEA1W40PNdfoPVz7GkbgQFD8rW2ee+6KTkwxOmQd/LIO3aInYWLKftl2XNM7cfm92tBdPCZ2oF4XM+hvXsPPMLHrQJAAhf492YTrawl0gelw38VNZ8Maic6jR2Xhp1nOJ6mXe3UpjFt6T6UnvR/h0tA5EM+ceA421lgBxO7J/dprH9MAQJAelhM7iRDXDTZOGb3K4Kk8cXOD6yc1yzKv3yqeAQlsR8vf5dFKHSbDXVqElK7RQ6RTXuHbn2Eu5uFAHSc/c3/EQ==
-----END RSA PRIVATE KEY-----
```


## LDAP 目录结构设计

```ldif
# 用户容器
dn: ou=users,dc=easyus,dc=top
objectClass: organizationalUnit
ou: users

# 组织容器
dn: ou=groups,dc=easyus,dc=top
objectClass: organizationalUnit
ou: groups
```

### 用户组对象示例
```ldif

dn: cn=技术部,ou=groups,dc=easyus,dc=top
objectClass: groupOfNames
cn: 技术部
member: uid=admin,ou=users,dc=easyus,dc=top  # 初始占位用户
```