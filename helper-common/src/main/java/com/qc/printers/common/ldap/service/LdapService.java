package com.qc.printers.common.ldap.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.ikuai.dao.SysIkuaiNetAllowDao;
import com.qc.printers.common.ikuai.domain.entity.SysIkuaiNetAllow;
import com.qc.printers.common.ldap.domain.dto.LdapDetpVO;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private SysIkuaiNetAllowDao sysIkuaiNetAllowDao;

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
            if (attrs.get("employeeNumber") != null) {
                user.setId(Long.valueOf(attrs.get("employeeNumber").get().toString()));
            }
            if (attrs.get("departmentNumber") != null) {
                user.setDeptId(Long.valueOf(attrs.get("departmentNumber").get().toString()));
            }
            return user;
        }
    }


    /**
     * 执行同步
     */
    public void syncDataToLdap(){
        // 清空缓存
        deptPathCache.clear();
        // 从数据库获取有效用户（密码不为空）
        List<User> dbUsers = userDao.findByRsaPasswordIsNotNull();

        // --- 1.用户同步 ---
        syncUsersToLdap("users",dbUsers);
        List<SysDept> list = iSysDeptService.list();

        // --- 2.用户组同步 ---
        syncDepts(list);

        // --- 3.用户-组织关联关系同步 ---
        Map<String, List<String>> targetMemberMap = buildUserDeptMapping(dbUsers,list);
        syncDeptMembers(targetMemberMap);

        // --- 4.再写一份用户到ikuaier组 ---
        try {
            syncUserToIkuai();
        }catch (Exception e){
            // 失败不影响整个同步
            log.error("syncUserToIkuai error: {}", e.getMessage());
        }
    }

    // 同步ikuai相关用户到ldap
    private void syncUserToIkuai(){
        // 判断有哪些符合条件的用户
        List<User> dbUsers = userDao.findByRsaPasswordIsNotNull();
        // 过滤出有权限的用户
        List<User> filteredUsers = new ArrayList<>();
        for (User dbUser : dbUsers) {
            LambdaQueryWrapper<SysIkuaiNetAllow> sysIkuaiNetAllowLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 用户ID or 部门Id
            sysIkuaiNetAllowLambdaQueryWrapper.eq(SysIkuaiNetAllow::getLinkType, 1);
            sysIkuaiNetAllowLambdaQueryWrapper.eq(SysIkuaiNetAllow::getLinkId,dbUser.getId());
            sysIkuaiNetAllowLambdaQueryWrapper.or(sysIkuaiNetAllowLambdaQueryWrapper1 -> {
                sysIkuaiNetAllowLambdaQueryWrapper1.eq(SysIkuaiNetAllow::getLinkType, 2);
                sysIkuaiNetAllowLambdaQueryWrapper1.eq(SysIkuaiNetAllow::getLinkId,dbUser.getDeptId());
            });
            long count = sysIkuaiNetAllowDao.count(sysIkuaiNetAllowLambdaQueryWrapper);
            if (count > 0L) {
                // 用户有权限
                filteredUsers.add(dbUser);
            }
        }
        // --- 用户同步 ---
        syncUsersToLdap("ikuaier",filteredUsers);
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
    public List<LdapDetpVO> generateAllDeptDns(List<DeptManger> deptTree) {
        List<LdapDetpVO> dns = new ArrayList<>();
        buildDnsRecursive(deptTree, "ou=groups", dns);
        return dns;
    }

    private void buildDnsRecursive(List<DeptManger> depts, String parentDn, List<LdapDetpVO> dns) {
        for (DeptManger dept : depts) {
            String currentDn = "cn=" + String.valueOf(dept.getId()) + "," + parentDn;
            LdapDetpVO ldapDept = new LdapDetpVO();
            ldapDept.setDn(currentDn);
            ldapDept.setDeptId(dept.getId());
            ldapDept.setDeptAllName(dept.getDeptNameAll());
            ldapDept.setDeptName(dept.getDeptName());
            dns.add(ldapDept);
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
            pathSegments.add("cn=" + escapeDN(String.valueOf(currentDept.getId())));
            currentDept = iSysDeptService.getById(currentDept.getParentId());
        }

        // 根部门特殊处理
        if (currentDept != null) {
            pathSegments.add("cn=" + escapeDN(String.valueOf(currentDept.getId())));
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

        // 生成MySQL部门DN列表(带部门ID)
        List<LdapDetpVO> mysqlDns = generateAllDeptDns(mysqlDeptTree);
        List<String> mysqlDnsString = mysqlDns.stream().map(LdapDetpVO::getDn).toList();

        List<String> ldapDns = getAllExistingDeptDns();
        // 新增部门
        mysqlDns.stream()
                .filter(dn -> !ldapDns.contains(dn.getDn()))
                .forEach(this::createDeptInLdap);

        // 删除多余部门（逆序保证先删子部门）
        Collections.reverse(ldapDns);
        ldapDns.stream()
                .filter(dn -> !mysqlDnsString.contains(dn))
                .forEach(this::deleteDeptFromLdap);


    }

    // 创建部门条目
    private void createDeptInLdap(LdapDetpVO dn) {
        LdapDept dept = new LdapDept();
        dept.setDn(LdapUtils.newLdapName(dn.getDn()));
        dept.setCn(String.valueOf(dn.getDeptId()));
        dept.setOu(String.valueOf(dn.getDeptAllName()));
        dept.setDescription(String.valueOf(dn.getDeptAllName()));
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
        user.setDeptId(1L);
        user.setId(1090L);
        try {
            String encrypt = PasswordRsaUtil.encrypt("s123456"+currentTimeMillis());
            user.setRsaPassword(encrypt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private void syncUsersToLdap(String groupName,List<User> users) {
        List<User> dbUsers = new ArrayList<>();
        // 此处复制数据，避免直接添加的占位用户影响后续绑定
        dbUsers.addAll(users);
        // 这里可以添加一个占位用户，避免LDAP中没有用户的情况
        dbUsers.add(mockUser());
        // 从LDAP获取现有用户
        List<User> ldapUsers = ldapTemplate.search(
                "ou=%s".formatted(groupName),
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
                ldapTemplate.unbind(buildUserDn(groupName,u));
            }catch (Exception e){
                log.error("LDAP user deletion failed for user: {}", u.getUsername(), e);
            }

        });

        // 新增用户
        toAdd.forEach(u -> {
            try {
                DirContextAdapter ctx = new DirContextAdapter(buildUserDn(groupName,u));
                mapUserAttributes(u, ctx);
                ldapTemplate.bind(ctx);
            }catch (Exception e){
                log.error("LDAP user creation failed for user: {}", u.getUsername(), e);
            }

        });

        // 更新用户
        toUpdate.forEach(u -> {
            try {
                DirContextOperations ctx = ldapTemplate.lookupContext(buildUserDn(groupName,u));
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
        ctx.setAttributeValue("employeeNumber", String.valueOf(user.getId()));
        ctx.setAttributeValue("departmentNumber", String.valueOf(user.getDeptId()));
        ctx.setAttributeValue("mail", user.getEmail());
        ctx.setAttributeValue("userPassword",(user.getJieMiPassword()));
        ctx.setAttributeValue("objectClass", "inetOrgPerson");
    }
    private boolean needUpdate(User dbUser, User ldapUser) {
        return !Objects.equals(dbUser.getName(), ldapUser.getName()) ||
                !Objects.equals(dbUser.getEmail(), ldapUser.getEmail()) ||
                !Objects.equals(dbUser.getId(), ldapUser.getId()) ||
                !Objects.equals(dbUser.getDeptId(), ldapUser.getDeptId()) ||
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
