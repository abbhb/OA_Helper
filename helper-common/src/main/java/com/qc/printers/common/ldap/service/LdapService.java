package com.qc.printers.common.ldap.service;

import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.ldap.domain.entity.LdapDept;
import com.qc.printers.common.ldap.utils.PasswordRsaUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.common.user.utils.DeptMangerHierarchyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qc.printers.common.ldap.utils.DnBuilder.buildUserDn;
import static java.lang.System.currentTimeMillis;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
@Slf4j
public class LdapService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ISysDeptService iSysDeptService;
    @Autowired
    private LdapTemplate ldapTemplate;

    // 构建部门路径缓存（需在服务初始化时加载）
    private Map<Long, String> deptPathCache = new ConcurrentHashMap<>();

    private class UserAttributesMapper implements AttributesMapper<User> {
        @Override
        public User mapFromAttributes(Attributes attrs) throws NamingException {
            User user = new User();
            // 从 LDAP 属性映射到 Java 对象字段
            if (attrs.get("cn") != null) {
                user.setUsername(attrs.get("cn").get().toString());
            }
            if (attrs.get("mail") != null) {
                user.setEmail(attrs.get("mail").get().toString());
            }
            return user;
        }
    }
    private class GroupAttributesMapper implements AttributesMapper<LdapDept> {
        @Override
        public LdapDept mapFromAttributes(Attributes attrs) throws NamingException {
            LdapDept ldapDept = new LdapDept();
            // 从 LDAP 属性映射到 Java 对象字段
            if (attrs.get("cn") != null) {
                // 部门id
                ldapDept.setCn(attrs.get("cn").get().toString());
            }
            if (attrs.get("mail") != null) {
                ldapDept.setName(attrs.get("mail").get().toString());
            }
//            ldapDept.setDn();
//            Object cn = (LdapAttribute)attrs.get("cn").get();

            return ldapDept;
        }
    }

    public List<String> getAllExistingDeptDns() {
        return ldapTemplate.search(
                query().base("ou=groups")
                        .where("objectClass").is("groupOfNames"),
                new AbstractContextMapper<String>() {
                    @Override
                    protected String doMapFromContext(DirContextOperations dirContextOperations) {
                        // 通过 DirContextAdapter 获取 DN
                        DirContextAdapter adapter = (DirContextAdapter) dirContextOperations;
                        return adapter.getDn().toString();
                    }
                }
        );
    }


    // 获取全量部门DN路径（从MySQL递归生成）
    public List<String> generateAllDeptDns(List<DeptManger> deptTree) {
        List<String> dns = new ArrayList<>();
        buildDnsRecursive(deptTree, "ou=groups", dns);
        return dns;
    }

    private void buildDnsRecursive(List<DeptManger> depts, String parentDn, List<String> dns) {
        for (DeptManger dept : depts) {
            String currentDn = "cn=" + dept.getDeptName() + "," + parentDn;
            dns.add(currentDn);
            if (dept.getChildren() != null) {
                buildDnsRecursive(dept.getChildren(), currentDn, dns);
            }
        }
    }

    private String escapeDN(String name) {
        // 转义逗号、等号等特殊字符（RFC4514规范）
        return name.replace(",", "\\,")
                .replace("=", "\\=")
                .replace("#", "\\#")
                .replace("/", "\\/");
    }

    public String buildDeptDn(Long deptId) {
        if (deptPathCache.containsKey(deptId)) {
            return deptPathCache.get(deptId);
        }

        List<String> pathSegments = new ArrayList<>();
        SysDept currentDept = iSysDeptService.getById(deptId);

        // 递归查询父部门直到根节点
        while (currentDept != null && !currentDept.getParentId().equals(0L)) {
            pathSegments.add("cn=" + escapeDN(currentDept.getDeptName()));
            currentDept = iSysDeptService.getById(currentDept.getParentId());
        }

        // 根部门特殊处理
        if (currentDept != null) {
            pathSegments.add("cn=" + escapeDN(currentDept.getDeptName()));
        }

//        Collections.reverse(pathSegments); // 反转路径顺序（从根到子）
        log.info("dn: {}", pathSegments);
        String dn = String.join(",", pathSegments) + ",ou=groups";
        deptPathCache.put(deptId, dn);
        return dn;
    }

    // 执行同步
    public void syncDepts(List<SysDept> list) {
        // 递归生成部门树
        DeptMangerHierarchyBuilder deptMangerHierarchyBuilder = new DeptMangerHierarchyBuilder(list, null, null, 0);
        List<DeptManger> mysqlDeptTree = deptMangerHierarchyBuilder.buildHierarchy();


        List<String> mysqlDns = generateAllDeptDns(mysqlDeptTree);


        List<String> ldapDns = getAllExistingDeptDns();
        // 新增部门
        mysqlDns.stream()
                .filter(dn -> !ldapDns.contains(dn))
                .forEach(this::createDeptInLdap);

        // 删除多余部门（逆序保证先删子部门）
        Collections.reverse(ldapDns);
        ldapDns.stream()
                .filter(dn -> !mysqlDns.contains(dn))
                .forEach(this::deleteDeptFromLdap);


    }

    // 创建部门条目
    private void createDeptInLdap(String dn) {
        LdapDept dept = new LdapDept();
        dept.setDn(LdapUtils.newLdapName(dn));
        dept.setCn(dn.split(",")[0].split("=")[1]);
        dept.setName(dn.split(",")[0].split("=")[1]);
        dept.getMembers().add("cn=ldapsynczhanwei,ou=users"); // 绑定ldapsynczhanwei用户
        ldapTemplate.create(dept);
    }

    // 删除部门条目
    private void deleteDeptFromLdap(String dn) {
        ldapTemplate.delete(LdapUtils.newLdapName(dn));
    }
    private User mockUser(){
        // 创建一个ldapsynczhanwei的占位用户
        User user = new User();
        user.setUsername("ldapsynczhanwei");
        user.setName("LDAPSyncUser");
        user.setEmail("ldapsynczhanwei@easyus.top");
        user.setStudentId("202115040299");
        user.setActiveStatus(1);
        user.setId(1090L);
        try {
            String encrypt = PasswordRsaUtil.encrypt("s123456"+currentTimeMillis());
            user.setRsaPassword(encrypt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private void syncUsersToLdap(List<User> users) {
        List<User> dbUsers = new ArrayList<>();
        // 此处复制数据，避免直接添加的占位用户影响后续绑定
        dbUsers.addAll(users);
        // 这里可以添加一个占位用户，避免LDAP中没有用户的情况
        dbUsers.add(mockUser());
        // 从LDAP获取现有用户
        List<User> ldapUsers = ldapTemplate.search(
                "ou=users",
                "(objectClass=inetOrgPerson)",
                new UserAttributesMapper()
        );

        Map<String, User> dbUserMap = dbUsers.stream()
                .collect(Collectors.toMap(User::getUsername, Function.identity()));

        // 映射LDAP用户到数据库用户
        Map<String, User> ldapUserMap = ldapUsers.stream()
                .collect(Collectors.toMap(User::getUsername, Function.identity()));

        // 需要删除的用户（LDAP存在但数据库不存在）
        List<User> toDelete = ldapUsers.stream()
                .filter(u -> !dbUserMap.containsKey(u.getUsername()))
                .toList();

        // 需要新增的用户（数据库存在但LDAP不存在）
        List<User> toAdd = dbUsers.stream()
                .filter(u -> !ldapUserMap.containsKey(u.getUsername()))
                .toList();

        // 需要更新的用户（两者都存在）
        List<User> toUpdate = dbUsers.stream()
                .filter(u -> ldapUserMap.containsKey(u.getUsername()))
                .filter(u -> needUpdate(u, ldapUserMap.get(u.getUsername())))
                .toList();


        // 删除多余用户
        toDelete.forEach(u -> {
            try {
                ldapTemplate.unbind(buildUserDn(u));
            }catch (Exception e){
                log.error("LDAP user deletion failed for user: {}", u.getUsername(), e);
            }

        });

        // 新增用户
        toAdd.forEach(u -> {
            try {
                DirContextAdapter ctx = new DirContextAdapter(buildUserDn(u));
                mapUserAttributes(u, ctx);
                ldapTemplate.bind(ctx);
            }catch (Exception e){
                log.error("LDAP user creation failed for user: {}", u.getUsername(), e);
            }

        });

        // 更新用户
        toUpdate.forEach(u -> {
            try {
                DirContextOperations ctx = ldapTemplate.lookupContext(buildUserDn(u));
                mapUserAttributes(u, ctx);
                ldapTemplate.modifyAttributes(ctx);
            }catch (Exception e){
                log.error("LDAP user update failed for user: {}", u.getUsername(), e);
            }
        });
    }
    private Map<String, List<String>> buildUserDeptMapping(List<User> users,List<SysDept> depts) {

        // 构建部门ID到DN的映射
        Map<Long, String> deptIdToDn = depts.stream()
                .collect(Collectors.toMap(
                        SysDept::getId,
                        dept -> buildDeptDn(dept.getId()) // 使用之前实现的部门DN生成方法
                ));

        return users.stream()
                .filter(u -> u.getDeptId() != null)
                .collect(Collectors.groupingBy(
                        u -> deptIdToDn.get(u.getDeptId()),
                        Collectors.mapping(
                                u -> "cn=" + u.getUsername() + ",ou=users",
                                Collectors.toList()
                        )
                ));
    }

    /**
     * 执行同步
     */
    public void syncDataToLdap(){
        // 清空缓存
        deptPathCache.clear();
        // 从数据库获取有效用户（密码不为空）
        List<User> dbUsers = userDao.findByRsaPasswordIsNotNull();
        // 校验用户名和邮箱，用户名不能包含空格，邮箱得符合邮箱正则
        dbUsers = dbUsers.stream()
                .filter(user -> StringUtils.isNotBlank(user.getUsername()) && !user.getUsername().contains(" "))
                .filter(user -> StringUtils.isNotBlank(user.getEmail()) && user.getEmail().matches("^[\\w-\\.]+@[\\w-]+\\.[a-z]{2,4}$"))
                .collect(Collectors.toList());
        // --- 1.用户同步 ---
        syncUsersToLdap(dbUsers);
        List<SysDept> list = iSysDeptService.list();


        // --- 2.用户组同步 ---
        syncDepts(list);


        // --- 3.用户-组织关联关系同步 ---
        Map<String, List<String>> targetMemberMap = buildUserDeptMapping(dbUsers,list);
        syncDeptMembers(targetMemberMap);
    }
    private void syncDeptMembers(Map<String, List<String>> targetMemberMap) {
        targetMemberMap.forEach((deptDn, targetMembers) -> {
            // 获取当前部门成员
            List<String> currentMembers = ldapTemplate.search(
                    query().base(deptDn).where("objectclass").is("groupOfNames"),
                    (AttributesMapper<List<String>>) attrs ->
                            Collections.list(attrs.get("member").getAll()).stream()
                                    .map(Object::toString)
                                    .collect(Collectors.toList())
            ).get(0);

            // 计算差异（网页1的引用式关联策略）
            List<String> toAdd = targetMembers.stream()
                    .filter(m -> !currentMembers.contains(m))
                    .collect(Collectors.toList());
            List<String> toRemove = currentMembers.stream()
                    .filter(m -> !targetMembers.contains(m) && !m.contains("ldapsynczhanwei"))
                    .collect(Collectors.toList());

            // 执行更新操作（网页5的关联策略）
            DirContextOperations ctx = ldapTemplate.lookupContext(deptDn);
            toAdd.forEach(m -> ctx.addAttributeValue("member", m));
            toRemove.forEach(m -> ctx.removeAttributeValue("member", m));
            ldapTemplate.modifyAttributes(ctx);
        });
    }

    private void mapUserAttributes(User user, DirContextOperations ctx) {
        ctx.setAttributeValue("cn", user.getUsername());
        ctx.setAttributeValue("sn", user.getName());
        ctx.setAttributeValue("mail", user.getEmail());
        ctx.setAttributeValue("userPassword",(user.getJieMiPassword()));
        ctx.setAttributeValue("objectClass", "inetOrgPerson");
    }
    private boolean needUpdate(User dbUser, User ldapUser) {
        return !Objects.equals(dbUser.getName(), ldapUser.getName()) ||
                !Objects.equals(dbUser.getEmail(), ldapUser.getEmail()) ||
                isPasswordChanged(dbUser, ldapUser);
    }

    private boolean isPasswordChanged(User dbUser, User ldapUser) {
        boolean bond = validatePasswordByBind(dbUser.getUsername(), dbUser.getJieMiPassword());
        // 密码验证失败就认为密码发生了改变，重设密码
        return !bond;
    }

    // 通过绑定操作验证密码
    private boolean validatePasswordByBind(String username, String password) {
        LdapContext context = null;
        try {

            String userDn = "cn=" + username + ",ou=users";
            context = (LdapContext) ldapTemplate.getContextSource().getContext(userDn, password);
            return true;
        }
        catch (AuthenticationException exception){
            log.error("LDAP Authentication failed for user: {}", username, exception);
            return false;
        }
        catch (Exception e) {
            log.error("LDAP Authentication error for user: {}", username, e);
            return false;
        }
        finally {
            LdapUtils.closeContext(context);
        }
    }

}
