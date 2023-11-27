# 各类需要确认的东西均可复用

不提供controller,只提供service服务

sql为：

```sql
CREATE TABLE `sys_confirm` (
`id` bigint NOT NULL,
`key` varchar(255) NOT NULL COMMENT '要用户确认的key',
`user_id` bigint NOT NULL COMMENT '用户确认',
`create_time` datetime(3) NOT NULL COMMENT '确认时间',
PRIMARY KEY (`id`) USING BTREE,
UNIQUE KEY `key_userid` (`key`,`user_id`) USING BTREE,
KEY `userid` (`user_id`) USING BTREE,
KEY `key` (`key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

```

service:

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class ConfirmServiceImpl implements ConfirmService {

    @Autowired
    private ConfirmMapper confirmMapper;

    @Override
    public void save(Confirm confirm) {
        confirmMapper.insert(confirm);
    }

    @Override
    public void delete(Long id) {
        confirmMapper.deleteById(id);
    }

    @Override
    public void deleteByKey(String key) {
    }
}
