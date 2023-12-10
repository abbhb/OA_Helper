/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.12.12
 Source Server Type    : MySQL
 Source Server Version : 80031
 Source Host           : 192.168.12.12:3306
 Source Schema         : easy_oa

 Target Server Type    : MySQL
 Target Server Version : 80031
 File Encoding         : 65001

 Date: 27/11/2023 17:57:10
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`
(
    `id`          bigint                                                       NOT NULL,
    `content`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
    `label`       varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签内容',
    `type`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签颜色',
    `is_deleted`  int                                                          NOT NULL DEFAULT 0 COMMENT '1为删除',
    `create_time` datetime                                                     NOT NULL,
    `update_time` datetime                                                     NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement`
VALUES (1, '测试公告', '消息', 'cyan', 0, '2023-05-23 12:54:36', '2023-05-23 12:54:39');
INSERT INTO `announcement`
VALUES (2, '测试活动', '活动', 'orangered', 0, '2023-05-23 12:54:57', '2023-05-23 12:54:59');
INSERT INTO `announcement`
VALUES (3, '测试通知', '通知', 'blue', 0, '2023-05-23 12:55:18', '2023-05-23 12:55:20');

-- ----------------------------
-- Table structure for common_config
-- ----------------------------
DROP TABLE IF EXISTS `common_config`;
CREATE TABLE `common_config`
(
    `config_key`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'config key',
    `config_value`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'config value',
    `config_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
    PRIMARY KEY (`config_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of common_config
-- ----------------------------
INSERT INTO `common_config`
VALUES ('study_clock_model', 'stay',
        'stay:保持模式，需要挂浏览器计时 sign:签到打卡模式，只用记录上下班打卡时间和按时打卡签到，不记录学习时间');
INSERT INTO `common_config`
VALUES ('study_clock_sign_in_morning_time_enabled', 'true', 'sign模式早上签到是否开启');
INSERT INTO `common_config`
VALUES ('study_clock_sign_in_morning_time_end', '8,0', 'sign模式早上签到时间8:00截止禁止签入');
INSERT INTO `common_config`
VALUES ('study_clock_sign_in_morning_time_start', '7,20', 'sign模式早上签到时间开始于7：20');

-- ----------------------------
-- Table structure for contact
-- ----------------------------
DROP TABLE IF EXISTS `contact`;
CREATE TABLE `contact`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `uid`         bigint NOT NULL COMMENT 'uid',
    `room_id`     bigint NOT NULL COMMENT '房间id',
    `read_time`   datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '阅读到的时间',
    `active_time` datetime(3) NULL DEFAULT NULL COMMENT '会话内消息最后更新的时间(只有普通会话需要维护，全员会话不需要维护)',
    `last_msg_id` bigint NULL DEFAULT NULL COMMENT '会话最新消息id',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_uid_room_id`(`uid` ASC, `room_id` ASC) USING BTREE,
    INDEX         `idx_room_id_read_time`(`room_id` ASC, `read_time` ASC) USING BTREE,
    INDEX         `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX         `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of contact
-- ----------------------------

-- ----------------------------
-- Table structure for doc_classification
-- ----------------------------
DROP TABLE IF EXISTS `doc_classification`;
CREATE TABLE `doc_classification`
(
    `id`         bigint                                                        NOT NULL COMMENT '分类业务id',
    `name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名',
    `is_deleted` int                                                           NOT NULL COMMENT '假删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doc_classification
-- ----------------------------

-- ----------------------------
-- Table structure for doc_notification
-- ----------------------------
DROP TABLE IF EXISTS `doc_notification`;
CREATE TABLE `doc_notification`
(
    `id`                    bigint                                                        NOT NULL COMMENT '业务id',
    `title`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
    `type`                  int                                                           NOT NULL COMMENT '文章类型1：不急  2：一般  3：重要',
    `content`               text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '基于md内容',
    `create_time`           datetime                                                      NOT NULL,
    `update_time`           datetime                                                      NOT NULL,
    `create_user`           bigint                                                        NOT NULL COMMENT '谁创建的',
    `update_user`           bigint                                                        NOT NULL COMMENT '谁最后编辑',
    `is_deleted`            int                                                           NOT NULL DEFAULT 0 COMMENT '假删除',
    `doc_classification_id` bigint NULL DEFAULT NULL COMMENT '分类Id',
    `see_type`              int                                                           NOT NULL DEFAULT 0 COMMENT '0表示全都可见（无需密码），1表示包含仅哪些部门可见（密码也无效），2表示包含的部门可见，不可见可通过密码，3表示预发布，所有人可见，但都要密码',
    `password`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可选，访问密码，2，3必须有密码，0，1状态无需密码',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doc_notification
-- ----------------------------

-- ----------------------------
-- Table structure for doc_notification_dept
-- ----------------------------
DROP TABLE IF EXISTS `doc_notification_dept`;
CREATE TABLE `doc_notification_dept`
(
    `id`                  bigint NOT NULL,
    `doc_notification_id` bigint NOT NULL,
    `dept_id`             bigint NOT NULL,
    PRIMARY KEY (`id`, `doc_notification_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doc_notification_dept
-- ----------------------------

-- ----------------------------
-- Table structure for group_member
-- ----------------------------
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `group_id`    bigint NOT NULL COMMENT '群主id',
    `uid`         bigint NOT NULL COMMENT '成员uid',
    `role`        int    NOT NULL COMMENT '成员角色 1群主 2管理员 3普通成员',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX         `idx_group_id_role`(`group_id` ASC, `role` ASC) USING BTREE,
    INDEX         `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX         `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '群成员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_member
-- ----------------------------

-- ----------------------------
-- Table structure for index_image
-- ----------------------------
DROP TABLE IF EXISTS `index_image`;
CREATE TABLE `index_image`
(
    `id`    bigint                                                        NOT NULL,
    `label` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'label',
    `image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片key',
    `sort`  int                                                           NOT NULL DEFAULT 0 COMMENT '排序',
    `extra` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '额外内容',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of index_image
-- ----------------------------

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log`
(
    `id`          bigint NOT NULL,
    `user_id`     bigint NOT NULL,
    `method`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `params`      varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `ip`          varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `type`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `model`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
    `result`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `url`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of log
-- ----------------------------

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`
(
    `id`           bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `room_id`      bigint NOT NULL COMMENT '会话表id',
    `from_uid`     bigint NOT NULL COMMENT '消息发送者uid',
    `content`      varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
    `reply_msg_id` bigint NULL DEFAULT NULL COMMENT '回复的消息内容',
    `status`       int    NOT NULL COMMENT '消息状态 0正常 1删除',
    `gap_count`    int NULL DEFAULT NULL COMMENT '与回复的消息间隔多少条',
    `type`         int NULL DEFAULT 1 COMMENT '消息类型 1正常文本 2.撤回消息',
    `extra`        json NULL COMMENT '扩展信息',
    `create_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX          `idx_room_id`(`room_id` ASC) USING BTREE,
    INDEX          `idx_from_uid`(`from_uid` ASC) USING BTREE,
    INDEX          `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX          `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 154 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message
-- ----------------------------

-- ----------------------------
-- Table structure for message_mark
-- ----------------------------
DROP TABLE IF EXISTS `message_mark`;
CREATE TABLE `message_mark`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `msg_id`      bigint NOT NULL COMMENT '消息表id',
    `uid`         bigint NOT NULL COMMENT '标记人uid',
    `type`        int    NOT NULL COMMENT '标记类型 1点赞 2举报',
    `status`      int    NOT NULL COMMENT '消息状态 0正常 1取消',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX         `idx_msg_id`(`msg_id` ASC) USING BTREE,
    INDEX         `idx_uid`(`uid` ASC) USING BTREE,
    INDEX         `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX         `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息标记表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message_mark
-- ----------------------------

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`
(
    `id`                bigint                                                         NOT NULL,
    `title`             varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '通知标题',
    `content`           longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '富文本',
    `status`            int                                                            NOT NULL DEFAULT 0 COMMENT '0为草稿，1为预发布，2为发布/定时发布，3为禁止查看',
    `is_deleted`        int                                                            NOT NULL DEFAULT 0 COMMENT '1为逻辑删除',
    `amount`            int                                                            NOT NULL DEFAULT 0 COMMENT '默认阅读量为0，只记录正式阅读接口，预发布不算',
    `is_annex`          int                                                            NOT NULL DEFAULT 0 COMMENT '0：不存在附件，1存在附件',
    `create_time`       datetime                                                       NOT NULL,
    `update_time`       datetime                                                       NOT NULL,
    `create_user`       bigint                                                         NOT NULL,
    `update_user`       bigint                                                         NOT NULL,
    `tag`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'tag	否	string	，分割，如果多个tag，比如紧急，AI,默认不携带就是按顺序排序\nrelease_user	否		发布人id，发布时天上',
    `release_user`      bigint NULL DEFAULT NULL COMMENT '发布人id，发布时天上',
    `release_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人发布时的name【冗余字段】',
    `release_time`      datetime NULL DEFAULT NULL COMMENT '如果发布就取发布时间，如果定时发布则为可见时间，最终效果为必须时间在发布时间之后才可见',
    `release_dept`      bigint NULL DEFAULT NULL COMMENT '发布人发布时所在部门id',
    `release_dept_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布人发布时所在部门名称【冗余字段】',
    `urgency`           int                                                            NOT NULL DEFAULT 1 COMMENT '紧急程度，默认为一般，网页前端默认为全部展示\n1为一般，2为不急，3为紧急',
    `version`           int                                                            NOT NULL DEFAULT 0 COMMENT '版本号，第一次为0每次修改成功加1，启用乐观锁',
    `update_user_list`  varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '默认为空，首次创建为创建人的id，后面逗号隔开，记录所有的编辑人',
    `visibility`        int                                                            NOT NULL DEFAULT 1 COMMENT '1：全体可见，2：选择的部门可见（去查通知部门关系表）',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------

-- ----------------------------
-- Table structure for notice_annex
-- ----------------------------
DROP TABLE IF EXISTS `notice_annex`;
CREATE TABLE `notice_annex`
(
    `id`             bigint                                                        NOT NULL,
    `notice_id`      bigint                                                        NOT NULL COMMENT '通知id',
    `sort_num`       int                                                           NOT NULL DEFAULT 1 COMMENT '附件的排序，可能一个通知多个附件',
    `file_url`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件地址',
    `download_count` int                                                           NOT NULL COMMENT '下载次数，每次下载递增',
    `is_deleted`     int                                                           NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`    datetime                                                      NOT NULL,
    `create_user`    bigint                                                        NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice_annex
-- ----------------------------

-- ----------------------------
-- Table structure for notice_dept
-- ----------------------------
DROP TABLE IF EXISTS `notice_dept`;
CREATE TABLE `notice_dept`
(
    `id`          bigint   NOT NULL COMMENT 'id',
    `notice_id`   bigint   NOT NULL COMMENT '通知id',
    `dept_id`     bigint   NOT NULL COMMENT '部门id',
    `is_deleted`  int      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` datetime NOT NULL,
    `create_user` bigint   NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice_dept
-- ----------------------------

-- ----------------------------
-- Table structure for notice_user_read
-- ----------------------------
DROP TABLE IF EXISTS `notice_user_read`;
CREATE TABLE `notice_user_read`
(
    `id`          bigint   NOT NULL COMMENT 'id',
    `notice_id`   bigint   NOT NULL COMMENT '通知id',
    `user_id`     bigint   NOT NULL COMMENT '用户id',
    `create_time` datetime NOT NULL COMMENT '什么时候读的',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice_user_read
-- ----------------------------

-- ----------------------------
-- Table structure for printer
-- ----------------------------
DROP TABLE IF EXISTS `printer`;
CREATE TABLE `printer`
(
    `id`                          bigint                                                        NOT NULL COMMENT '打印记录id',
    `copies`                      int NULL DEFAULT NULL COMMENT '打印份数',
    `printing_direction`          int NULL DEFAULT NULL COMMENT '打印方向',
    `print_big_value`             int NULL DEFAULT NULL COMMENT '打印大小',
    `need_print_pages_end_index`  int NULL DEFAULT NULL COMMENT '需要解析那些页码',
    `single_document_paper_usage` int NULL DEFAULT NULL COMMENT '单份文件用纸数',
    `content_hash`                varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容哈希',
    `name`                        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名称',
    `create_time`                 datetime                                                      NOT NULL COMMENT '打印时间',
    `create_user`                 bigint                                                        NOT NULL COMMENT '打印人id',
    `is_duplex`                   int NULL DEFAULT NULL COMMENT '是否双面 1为是，0为否',
    `url`                         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件地址',
    `origin_file_pages`           int NULL DEFAULT NULL COMMENT '源文件总页数',
    `need_print_pages_index`      int NULL DEFAULT NULL COMMENT '需要解析那些页码起始页',
    `is_print`                    int                                                           NOT NULL DEFAULT 0 COMMENT '最终是否打印成功',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of printer
-- ----------------------------

-- ----------------------------
-- Table structure for quick_navigation_categorize
-- ----------------------------
DROP TABLE IF EXISTS `quick_navigation_categorize`;
CREATE TABLE `quick_navigation_categorize`
(
    `id`         bigint                                                        NOT NULL COMMENT '分类id',
    `name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名',
    `is_deleted` int                                                           NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of quick_navigation_categorize
-- ----------------------------

-- ----------------------------
-- Table structure for quick_navigation_item
-- ----------------------------
DROP TABLE IF EXISTS `quick_navigation_item`;
CREATE TABLE `quick_navigation_item`
(
    `id`            bigint                                                        NOT NULL,
    `name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'itemname',
    `path`          text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '路径,或md内容',
    `permission`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ',为分隔符,有哪些权限',
    `image`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
    `introduction`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '介绍',
    `categorize_id` bigint                                                        NOT NULL COMMENT '处于哪个分类',
    `is_deleted`    int                                                           NOT NULL DEFAULT 0 COMMENT '默认没被删除',
    `type`          int                                                           NOT NULL DEFAULT 0 COMMENT '0为url+md,1为md,1为全md',
    `content`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'md内容可为空',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of quick_navigation_item
-- ----------------------------

-- ----------------------------
-- Table structure for room
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `type`        int NOT NULL COMMENT '房间类型 1群聊 2单聊',
    `hot_flag`    int NULL DEFAULT 0 COMMENT '是否全员展示 0否 1是',
    `active_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '群最后消息的更新时间（热点群不需要写扩散，只更新这里）',
    `last_msg_id` bigint NULL DEFAULT NULL COMMENT '会话中的最后一条消息id',
    `ext_json`    json NULL COMMENT '额外信息（根据不同类型房间有不同存储的东西）',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX         `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX         `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of room
-- ----------------------------
INSERT INTO `room`
VALUES (1, 1, 1, '2023-11-27 17:41:23.858', NULL, NULL, '2023-09-19 12:08:57.390', '2023-11-27 09:48:50.658');
INSERT INTO `room`
VALUES (2, 1, 1, '2023-11-27 09:39:09.657', NULL, NULL, '2023-11-27 09:39:09.657', '2023-11-27 09:39:09.657');

-- ----------------------------
-- Table structure for room_friend
-- ----------------------------
DROP TABLE IF EXISTS `room_friend`;
CREATE TABLE `room_friend`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `room_id`     bigint                                                       NOT NULL COMMENT '房间id',
    `uid1`        bigint                                                       NOT NULL COMMENT 'uid1（更小的uid）',
    `uid2`        bigint                                                       NOT NULL COMMENT 'uid2（更大的uid）',
    `room_key`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间key由两个uid拼接，先做排序uid1_uid2',
    `status`      int                                                          NOT NULL COMMENT '房间状态 0正常 1禁用(删好友了禁用)',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `room_key`(`room_key` ASC) USING BTREE,
    INDEX         `idx_room_id`(`room_id` ASC) USING BTREE,
    INDEX         `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX         `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '单聊房间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of room_friend
-- ----------------------------

-- ----------------------------
-- Table structure for room_group
-- ----------------------------
DROP TABLE IF EXISTS `room_group`;
CREATE TABLE `room_group`
(
    `id`            bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `room_id`       bigint                                                        NOT NULL COMMENT '房间id',
    `name`          varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '群名称',
    `avatar`        varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '群头像',
    `ext_json`      json NULL COMMENT '额外信息（根据不同类型房间有不同存储的东西）',
    `delete_status` int                                                           NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)',
    `create_time`   datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time`   datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX           `idx_room_id`(`room_id` ASC) USING BTREE,
    INDEX           `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX           `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '群聊房间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of room_group
-- ----------------------------
INSERT INTO `room_group`
VALUES (1, 1, '全员群', '', NULL, 0, '2023-11-27 09:40:06.714', '2023-11-27 09:40:06.714');
INSERT INTO `room_group`
VALUES (2, 2, '系统通知', '', NULL, 0, '2023-11-27 09:39:55.029', '2023-11-27 09:39:55.029');

-- ----------------------------
-- Table structure for secure_invoke_record
-- ----------------------------
DROP TABLE IF EXISTS `secure_invoke_record`;
CREATE TABLE `secure_invoke_record`
(
    `id`                 bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `secure_invoke_json` json    NOT NULL COMMENT '请求快照参数json',
    `status`             tinyint NOT NULL COMMENT '状态 1待执行 2已失败',
    `next_retry_time`    datetime(3) NOT NULL COMMENT '下一次重试的时间',
    `retry_times`        int     NOT NULL COMMENT '已经重试的次数',
    `max_retry_times`    int     NOT NULL COMMENT '最大重试次数',
    `fail_reason`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '执行失败的堆栈',
    `create_time`        datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time`        datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX                `idx_next_retry_time`(`next_retry_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 168 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '本地消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of secure_invoke_record
-- ----------------------------

-- ----------------------------
-- Table structure for sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word`
(
    `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '敏感词'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sensitive_word
-- ----------------------------

-- ----------------------------
-- Table structure for study_clcok_sign_in_info
-- ----------------------------
DROP TABLE IF EXISTS `study_clcok_sign_in_info`;
CREATE TABLE `study_clcok_sign_in_info`
(
    `id`      bigint NOT NULL,
    `user_id` bigint NOT NULL COMMENT 'user_id',
    `task_id` bigint NOT NULL COMMENT '完成哪个签到任务',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX     `task_id`(`task_id` ASC) USING BTREE,
    CONSTRAINT `task_id` FOREIGN KEY (`task_id`) REFERENCES `study_clock_sign_in_task` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of study_clcok_sign_in_info
-- ----------------------------

-- ----------------------------
-- Table structure for study_clock
-- ----------------------------
DROP TABLE IF EXISTS `study_clock`;
CREATE TABLE `study_clock`
(
    `id`         bigint   NOT NULL,
    `user_id`    bigint   NOT NULL COMMENT '用户id',
    `date`       date     NOT NULL COMMENT '今日日期',
    `first_time` datetime NOT NULL COMMENT '首次签到日期，也就是创建记录的日期',
    `old_time`   double(5, 1
) NOT NULL DEFAULT 0.0 COMMENT '已经累计多少分钟，只有分钟为单位！！！',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of study_clock
-- ----------------------------

-- ----------------------------
-- Table structure for study_clock_sign_in_task
-- ----------------------------
DROP TABLE IF EXISTS `study_clock_sign_in_task`;
CREATE TABLE `study_clock_sign_in_task`
(
    `id`        bigint                                                        NOT NULL,
    `name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '什么签到',
    `start`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '从什么时候开始',
    `end`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '截止到几点 例如8:00就写8,0',
    `en_date`   datetime                                                      NOT NULL COMMENT '启用日期',
    `end_date`  datetime                                                      NOT NULL COMMENT '截止日期',
    `enable`    int                                                           NOT NULL DEFAULT 1 COMMENT '1启用0关闭',
    `only_days` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '格式为1,2,3 如果为仅周一周二周三才生效的话 空就是每天',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of study_clock_sign_in_task
-- ----------------------------
INSERT INTO `study_clock_sign_in_task`
VALUES (1, '周日早八', '6,0', '8,0', '2023-08-19 11:07:13', '2023-12-10 11:07:53', 1, '7');
INSERT INTO `study_clock_sign_in_task`
VALUES (2, '周日下午上班', '13,30', '17,0', '2023-08-19 11:07:13', '2023-12-10 11:07:53', 1, '7');
INSERT INTO `study_clock_sign_in_task`
VALUES (3, '通用晚上打卡', '17,0', '18,0', '2023-08-19 11:12:18', '2023-12-10 11:12:20', 1, '');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '部门id',
    `parent_id`   bigint NULL DEFAULT 0 COMMENT '父部门id',
    `ancestors`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '祖级列表',
    `dept_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '部门名称',
    `order_num`   int NULL DEFAULT 0 COMMENT '显示顺序',
    `leader`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
    `phone`       varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
    `email`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
    `status`      int                                                          NOT NULL DEFAULT 1 COMMENT '部门状态（1正常 0停用）',
    `is_deleted`  int                                                          NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    `create_user` bigint NULL DEFAULT NULL COMMENT '创建者',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_user` bigint NULL DEFAULT NULL COMMENT '更新者',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1729023789828730883 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept`
VALUES (1, 0, NULL, 'AI-EN-IS', 0, 'Super Admin', '13986530157', 'admin@admin.com', 1, 0, 1, '2023-08-30 14:44:01', 1,
        '2023-08-30 14:44:03');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `name`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
    `locale`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端能解析的文字',
    `parent_id`   bigint NULL DEFAULT 0 COMMENT '父菜单ID',
    `order_num`   int NULL DEFAULT 0 COMMENT '显示顺序',
    `path`        varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '路由地址',
    `is_frame`    int NULL DEFAULT 1 COMMENT '是否为外链（1是 0否）',
    `is_cache`    int NULL DEFAULT 0 COMMENT '是否缓存（1缓存 0不缓存）',
    `type`        char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `is_show`     int NULL DEFAULT 1 COMMENT '菜单是否展示在菜单栏1:展示',
    `status`      int NULL DEFAULT 1 COMMENT '菜单状态（1正常 0停用）对应到权限字段也停用',
    `perms`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
    `icon`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
    `create_user` bigint NULL DEFAULT NULL COMMENT '创建者',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_user` bigint NULL DEFAULT NULL COMMENT '更新者',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1723981157108486146 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu`
VALUES (1, 'dashboard', 'menu.server.dashboard', 0, 1, '/dashboard', 0, 0, 'M', 1, 1, NULL, 'icon-dashboard', 1, NULL,
        1, NULL);
INSERT INTO `sys_menu`
VALUES (2, 'Workplace', 'menu.dashboard.workplace', 1, 1, 'workplace', 0, 0, 'C', 1, 1, '', '', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (3, 'printer', 'menu.dashboard.printer', 1, 2, 'printer', 0, 0, 'C', 1, 1, 'sys:print:list', '', 1, NULL, 1,
        NULL);
INSERT INTO `sys_menu`
VALUES (4, 'keep', 'menu.dashboard.keep', 1, 3, 'keep', 0, 0, 'C', 1, 1, '', '', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (5, 'usercenter', 'menu.usercenter', 0, 2, '/usercenter', 0, 0, 'M', 1, 1, NULL, 'icon-user', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (6, 'profile', 'menu.usercenter.usercenter', 5, 1, 'profile', 0, 0, 'C', 1, 1, '', '', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (7, 'UserManger', 'menu.sysmanger.UserManger', 10, 1, 'user-manger', 0, 0, 'C', 1, 1, 'sys:user:list', '', 1,
        NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (9, 'fileBrowser', 'menu.fileBrowser', 0, 7,
        'http://easyoa.fun:55550/api/oauth2.0/authorize?response_type=code&client_id=3a1cab58d4b743d681a042ba20955178&redirect_uri=http://easyoa.fun:81/login&state=test&scope=get_user_info',
        1, 0, 'M', 1, 1, NULL, 'icon-folder', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (10, 'sysmanger', 'menu.sysmanger', 0, 3, '/sysmanger', 0, 0, 'M', 1, 1, '', 'icon-menu', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (11, 'MenuManger', 'menu.sysmanger.MenuManger', 10, 2, 'menu-manger', 0, 0, 'C', 1, 1, 'sys:menu:list', '', 1,
        NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (1698681230501560322, 'QuickRouter', 'menu.QuickRouter', 0, 8, 'http://10.15.247.254:55554/', 1, 0, 'C', 0, 0,
        '', 'icon-apps', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (1699399891600224258, 'RoleManger', 'menu.sysmanger.RoleManger', 10, 3, 'role-manger', 0, 0, 'C', 1, 1,
        'sys:role:list', '', 1, NULL, 1, NULL);
INSERT INTO `sys_menu`
VALUES (1699773810437967874, 'DeptManger', 'menu.sysmanger.DeptManger', 10, 4, 'dept-manger', 0, 0, 'C', 1, 1,
        'sys:study:list', '', 1, '2023-09-07 21:17:11', 1, '2023-09-07 21:17:11');
INSERT INTO `sys_menu`
VALUES (1702312893739655169, '', '查看打卡数据', 4, 1, '', 0, 0, 'F', 1, 1, 'sys:study:list', '', 1,
        '2023-09-14 21:26:36', 1, '2023-09-14 21:26:36');
INSERT INTO `sys_menu`
VALUES (1702313016154611713, '', '管理配置', 4, 1, '', 0, 0, 'F', 1, 1, 'sys:study:manger', '', 1,
        '2023-09-14 21:27:05', 1, '2023-09-14 21:27:05');
INSERT INTO `sys_menu`
VALUES (1702313111461781506, '', '添加用户', 7, 1, '', 0, 0, 'F', 1, 1, 'sys:user:add', '', 1, '2023-09-14 21:27:28', 1,
        '2023-09-14 21:27:28');
INSERT INTO `sys_menu`
VALUES (1702313163114635266, '', '删除用户', 7, 2, '', 0, 0, 'F', 1, 1, 'sys:user:delete', '', 1, '2023-09-14 21:27:40',
        1, '2023-09-14 21:27:40');
INSERT INTO `sys_menu`
VALUES (1702313296111820802, '', '更新用户', 7, 3, '', 0, 0, 'F', 1, 1, 'sys:user:update', '', 1, '2023-09-14 21:28:12',
        1, '2023-09-14 21:28:12');
INSERT INTO `sys_menu`
VALUES (1702313362839003137, '', '用户列表', 7, 1, '', 0, 0, 'F', 1, 1, 'sys:user:list', '', 1, '2023-09-14 21:28:28',
        1, '2023-09-14 21:28:28');
INSERT INTO `sys_menu`
VALUES (1702313475783221250, '', '添加菜单', 11, 1, '', 0, 0, 'F', 1, 1, 'sys:menu:add', '', 1, '2023-09-14 21:28:55',
        1, '2023-09-14 21:28:55');
INSERT INTO `sys_menu`
VALUES (1702313559610580994, '', '删除菜单', 11, 2, '', 0, 0, 'F', 1, 1, 'sys:menu:delete', '', 1,
        '2023-09-14 21:29:15', 1, '2023-09-14 21:29:15');
INSERT INTO `sys_menu`
VALUES (1702313672923897858, '', '修改菜单', 11, 3, '', 0, 0, 'F', 1, 1, 'sys:menu:update', '', 1,
        '2023-09-14 21:29:42', 1, '2023-09-14 21:29:42');
INSERT INTO `sys_menu`
VALUES (1702313775076171777, '', '菜单列表', 11, 4, '', 0, 0, 'F', 1, 1, 'sys:menu:list', '', 1, '2023-09-14 21:30:06',
        1, '2023-09-14 21:30:06');
INSERT INTO `sys_menu`
VALUES (1702313890977374209, '', '添加角色', 1699399891600224258, 1, '', 0, 0, 'F', 1, 1, 'sys:role:add', '', 1,
        '2023-09-14 21:30:34', 1, '2023-09-14 21:30:34');
INSERT INTO `sys_menu`
VALUES (1702313955213139969, '', '删除角色', 1699399891600224258, 2, '', 0, 0, 'F', 1, 1, 'sys:role:delete', '', 1,
        '2023-09-14 21:30:49', 1, '2023-09-14 21:30:49');
INSERT INTO `sys_menu`
VALUES (1702314008166227970, '', '修改角色', 1699399891600224258, 3, '', 0, 0, 'F', 1, 1, 'sys:role:update', '', 1,
        '2023-09-14 21:31:02', 1, '2023-09-14 21:31:02');
INSERT INTO `sys_menu`
VALUES (1702314062809620482, '', '角色列表', 1699399891600224258, 4, '', 0, 0, 'F', 1, 1, 'sys:role:list', '', 1,
        '2023-09-14 21:31:15', 1, '2023-09-14 21:31:15');
INSERT INTO `sys_menu`
VALUES (1702314132380540929, '', '添加部门', 1699773810437967874, 1, '', 0, 0, 'F', 1, 1, 'sys:dept:add', '', 1,
        '2023-09-14 21:31:31', 1, '2023-09-14 21:31:31');
INSERT INTO `sys_menu`
VALUES (1702314174294220801, '', '删除部门', 1699773810437967874, 2, '', 0, 0, 'F', 1, 1, 'sys:dept:delete', '', 1,
        '2023-09-14 21:31:41', 1, '2023-09-14 21:31:41');
INSERT INTO `sys_menu`
VALUES (1702314229730336769, '', '修改部门', 1699773810437967874, 3, '', 0, 0, 'F', 1, 1, 'sys:dept:update', '', 1,
        '2023-09-14 21:31:55', 1, '2023-09-14 21:31:55');
INSERT INTO `sys_menu`
VALUES (1702314309686353921, '', '部门列表', 1699773810437967874, 4, '', 0, 0, 'F', 1, 1, 'sys:dept:list', '', 1,
        '2023-09-14 21:32:14', 1, '2023-09-14 21:32:14');
INSERT INTO `sys_menu`
VALUES (1703249662781853698, '', '全部数据权限', 3, 1, '', 0, 0, 'F', 1, 1, 'sys:print:alldata', '', 1,
        '2023-09-17 11:28:59', 1, '2023-09-17 11:28:59');
INSERT INTO `sys_menu`
VALUES (1703268642263343105, 'ContentPromotion', 'menu.contentpromotion', 0, 4, '/content-promotion', 0, 0, 'M', 1, 1,
        '', 'icon-apps', 1, '2023-09-17 12:44:24', 1, '2023-09-17 12:44:24');
INSERT INTO `sys_menu`
VALUES (1703268748320514050, 'DocNotification', 'menu.docnotification.DocNotification', 1703268642263343105, 1,
        'docnotification', 0, 0, 'C', 1, 1, '', '', 1, '2023-09-17 12:44:49', 1, '2023-09-17 12:44:49');
INSERT INTO `sys_menu`
VALUES (1703328073911078914, 'IndexImage', 'menu.docnotification.IndexImage', 1703268642263343105, 2, 'indeximage', 0,
        0, 'C', 1, 1, '', '', 1, '2023-09-17 16:40:34', 1, '2023-09-17 16:40:34');
INSERT INTO `sys_menu`
VALUES (1703379296823468033, '', '新增图片', 1703328073911078914, 1, '', 0, 0, 'F', 1, 1, 'sys:indeximage:add', '', 1,
        '2023-09-17 20:04:06', 1, '2023-09-17 20:04:06');
INSERT INTO `sys_menu`
VALUES (1703379381351276546, '', '删除图片', 1703328073911078914, 2, '', 0, 0, 'F', 1, 1, 'sys:indeximage:delete', '',
        1, '2023-09-17 20:04:26', 1, '2023-09-17 20:04:26');
INSERT INTO `sys_menu`
VALUES (1703379432723111938, '', '修改图片', 1703328073911078914, 3, '', 0, 0, 'F', 1, 1, 'sys:indeximage:update', '',
        1, '2023-09-17 20:04:39', 1, '2023-09-17 20:04:39');
INSERT INTO `sys_menu`
VALUES (1703379497562857474, '', '获取图片', 1703328073911078914, 4, '', 0, 0, 'F', 1, 1, 'sys:indeximage:list', '', 1,
        '2023-09-17 20:04:54', 1, '2023-09-17 20:04:54');
INSERT INTO `sys_menu`
VALUES (1705549735037480962, 'Chat', 'menu.chat', 0, 5, '/chat', 0, 0, 'M', 1, 1, '', 'icon-info', 1,
        '2023-09-23 19:48:39', 1, '2023-09-23 19:48:39');
INSERT INTO `sys_menu`
VALUES (1705563149847699458, 'Chat-Index', '消息', 1705549735037480962, 1, 'chat', 0, 0, 'C', 1, 1, 'sys:chat:list', '',
        1, '2023-09-23 20:41:57', 1, '2023-09-23 20:41:57');
INSERT INTO `sys_menu`
VALUES (1709863803420348417, 'Entertainment', 'menu.entertainment', 0, 6, '/entertainment', 0, 0, 'M', 1, 1, '',
        'icon-clock-circle', 1, '2023-10-05 17:31:13', 1, '2023-10-05 17:31:13');
INSERT INTO `sys_menu`
VALUES (1709863993443291137, 'Center', 'menu.entertainment.center', 1709863803420348417, 1, 'center', 0, 0, 'C', 1, 1,
        'sys.entertainment.list', '', 1, '2023-10-05 17:31:58', 1, '2023-10-05 17:31:58');
INSERT INTO `sys_menu`
VALUES (1709914681489690626, 'Game2048', '2048', 1709863803420348417, 1, 'game2048', 0, 0, 'C', 0, 1, '', '', 1,
        '2023-10-05 20:53:23', 1, '2023-10-05 20:53:23');
INSERT INTO `sys_menu`
VALUES (1719696557347688449, 'Version', 'menu.version', 0, 20, '/version', 0, 0, 'M', 0, 1, '', 'icon-info', 1,
        '2023-11-01 20:43:04', 1, '2023-11-01 20:43:04');
INSERT INTO `sys_menu`
VALUES (1719696699043860482, 'VersionIndex', 'menu.version.index', 1719696557347688449, 1, 'index', 0, 0, 'C', 0, 1, '',
        '', 1, '2023-11-01 20:43:38', 1, '2023-11-01 20:43:38');
INSERT INTO `sys_menu`
VALUES (1723980017025355778, 'OauthManger', 'menu.sysmanger.OauthManger', 10, 5, '/oauth-manger', 0, 0, 'C', 1, 1,
        'sys:oauth:list', '', 1, '2023-11-13 16:24:01', 1, '2023-11-13 16:24:01');
INSERT INTO `sys_menu`
VALUES (1723980908860215298, '', '添加Oauth', 1723980017025355778, 1, '', 0, 0, 'F', 1, 1, 'sys.oauth:add', '', 1,
        '2023-11-13 16:27:33', 1, '2023-11-13 16:27:33');
INSERT INTO `sys_menu`
VALUES (1723981087562731521, '', '更新Oauth', 1723980017025355778, 2, '', 0, 0, 'F', 1, 1, 'sys.oauth.update', '', 1,
        '2023-11-13 16:28:16', 1, '2023-11-13 16:28:16');
INSERT INTO `sys_menu`
VALUES (1723981157108486145, '', '删除oauth', 1723980017025355778, 3, '', 0, 0, 'F', 1, 1, 'sys.oauth.delete', '', 1,
        '2023-11-13 16:28:33', 1, '2023-11-13 16:28:33');

-- ----------------------------
-- Table structure for sys_oauth
-- ----------------------------
DROP TABLE IF EXISTS `sys_oauth`;
CREATE TABLE `sys_oauth`
(
    `id`               bigint                                                        NOT NULL,
    `client_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端id',
    `client_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端name',
    `client_secret`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端秘钥',
    `redirect_uri`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认回调地址，成功回调会自动加上返回参数code',
    `client_image`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网站头像',
    `domain_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务域名',
    `no_sert_redirect` int                                                           NOT NULL DEFAULT 0 COMMENT '是否允许业务域名或者ip不匹配的回调，默认为1不允许，0为允许',
    `grant_type`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'code' COMMENT '授权类型：grant_type ，code为authorization_code（授权码模式）',
    `create_time`      datetime                                                      NOT NULL,
    `update_time`      datetime                                                      NOT NULL,
    `is_deleted`       int                                                           NOT NULL DEFAULT 0,
    `status`           int                                                           NOT NULL DEFAULT 1 COMMENT '默认启用，0为停用',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name`(`client_name` ASC) USING BTREE,
    UNIQUE INDEX `client_id`(`client_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oauth
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oauth_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_oauth_user`;
CREATE TABLE `sys_oauth_user`
(
    `id`       bigint NOT NULL,
    `oauth_id` bigint NOT NULL,
    `user_id`  bigint NOT NULL,
    `scope`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可选，授权如果包含新增的或者以前没授权过额外权限就前端再次确认，否则直接进入，无需确认',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX      `oauthidsda`(`oauth_id` ASC) USING BTREE,
    INDEX      `userid`(`user_id` ASC) USING BTREE,
    CONSTRAINT `oauthidsda` FOREIGN KEY (`oauth_id`) REFERENCES `sys_oauth` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `userid` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oauth_user
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
    `role_key`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色权限字符串,可以为空，权限由菜单权限里查得',
    `role_sort`   int                                                          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`      int                                                          NOT NULL DEFAULT 1 COMMENT '角色状态（1正常 0停用）',
    `is_deleted`  int                                                          NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
    `create_user` bigint NULL DEFAULT NULL COMMENT '创建者',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_user` bigint NULL DEFAULT NULL COMMENT '更新者',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1729023963212869634 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES (1, '系统管理员', 'superadmin', 1, 1, 0, 1, '2023-08-30 15:11:42', 1, '2023-08-30 15:11:44');
INSERT INTO `sys_role`
VALUES (2, '超级管理员', 'lsadmin', 2, 1, 0, 1, '2023-09-16 20:38:04', 1, '2023-09-16 20:38:07');
INSERT INTO `sys_role`
VALUES (1703241330814615554, '内部管理员', 'roomadmin', 4, 1, 0, 1, '2023-09-17 10:55:53', 1, '2023-09-17 10:55:53');
INSERT INTO `sys_role`
VALUES (1703241493385838593, '内部用户', 'roomuser', 5, 1, 0, 1, '2023-09-17 10:56:31', 1, '2023-09-17 10:56:31');
INSERT INTO `sys_role`
VALUES (1703241621119172610, '开放用户', 'other', 10, 1, 0, 1, '2023-09-17 10:57:02', 1, '2023-09-17 10:57:02');
INSERT INTO `sys_role`
VALUES (1719711889743335425, '娱乐中心游玩', 'yulecentergame', 6, 1, 0, 1, '2023-11-01 21:44:00', 1,
        '2023-11-01 21:44:00');
INSERT INTO `sys_role`
VALUES (1719711959280701441, '版本中心', 'versionRead', 14, 1, 0, 1, '2023-11-01 21:44:16', 1, '2023-11-01 21:44:16');
INSERT INTO `sys_role`
VALUES (1720081630404603906, 'chat', 'chat', 18, 1, 0, 1, '2023-11-02 14:13:13', 1, '2023-11-02 14:13:13');
INSERT INTO `sys_role`
VALUES (1720379282501464065, '大三管理组', 'threemanger', 3, 1, 0, 1, '2023-11-03 09:55:59', 1, '2023-11-03 09:55:59');
INSERT INTO `sys_role`
VALUES (1729023963212869633, '打印', 'printer', 7, 1, 0, 1, '2023-11-27 14:26:51', 1, '2023-11-27 14:26:51');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`
(
    `id`      bigint NOT NULL COMMENT '唯一',
    `role_id` bigint NOT NULL,
    `dept_id` bigint NOT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX     `role_id`(`role_id` ASC) USING BTREE,
    INDEX     `dept_id`(`dept_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept`
VALUES (1720377011659141122, 1719711889743335425, 1);
INSERT INTO `sys_role_dept`
VALUES (1720377011663335425, 1703241621119172610, 1);
INSERT INTO `sys_role_dept`
VALUES (1720377011667529729, 1719711959280701441, 1);
INSERT INTO `sys_role_dept`
VALUES (1720377011671724034, 1720081630404603906, 1);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`
(
    `id`      bigint NOT NULL,
    `role_id` bigint NOT NULL,
    `menu_id` bigint NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu`
VALUES (1719711889743335426, 1719711889743335425, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1719711889743335427, 1719711889743335425, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1719711889743335428, 1719711889743335425, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1719711959280701442, 1719711959280701441, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1719711959289090049, 1719711959280701441, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1720081630421381122, 1720081630404603906, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1720081630425575425, 1720081630404603906, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1720382188462075906, 1703241621119172610, 1);
INSERT INTO `sys_role_menu`
VALUES (1720382188462075907, 1703241621119172610, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1720382188466270210, 1703241621119172610, 2);
INSERT INTO `sys_role_menu`
VALUES (1720382188466270211, 1703241621119172610, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1720382188470464514, 1703241621119172610, 5);
INSERT INTO `sys_role_menu`
VALUES (1720382188470464515, 1703241621119172610, 6);
INSERT INTO `sys_role_menu`
VALUES (1720382188470464516, 1703241621119172610, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1720382188474658817, 1703241621119172610, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1720382188474658818, 1703241621119172610, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1720382188478853121, 1703241621119172610, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1720385772595507201, 1703241330814615554, 1);
INSERT INTO `sys_role_menu`
VALUES (1720385772595507202, 1703241330814615554, 2);
INSERT INTO `sys_role_menu`
VALUES (1720385772599701505, 1703241330814615554, 3);
INSERT INTO `sys_role_menu`
VALUES (1720385772603895810, 1703241330814615554, 4);
INSERT INTO `sys_role_menu`
VALUES (1720385772608090113, 1703241330814615554, 5);
INSERT INTO `sys_role_menu`
VALUES (1720385772612284418, 1703241330814615554, 6);
INSERT INTO `sys_role_menu`
VALUES (1720385772616478722, 1703241330814615554, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1720385772620673025, 1703241330814615554, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1720385772624867329, 1703241330814615554, 9);
INSERT INTO `sys_role_menu`
VALUES (1720385772629061633, 1703241330814615554, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1720385772629061634, 1703241330814615554, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1720385772633255938, 1703241330814615554, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1720385772637450241, 1703241330814615554, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1720385772641644545, 1703241330814615554, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1720385772641644546, 1703241330814615554, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1720385772645838850, 1703241330814615554, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1720385772650033153, 1703241330814615554, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1720385772654227457, 1703241330814615554, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1720385772654227458, 1703241330814615554, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1720386239576731649, 1720379282501464065, 1703249662781853698);
INSERT INTO `sys_role_menu`
VALUES (1720386239576731650, 1720379282501464065, 1);
INSERT INTO `sys_role_menu`
VALUES (1720386239576731651, 1720379282501464065, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1720386239576731652, 1720379282501464065, 2);
INSERT INTO `sys_role_menu`
VALUES (1720386239576731653, 1720379282501464065, 3);
INSERT INTO `sys_role_menu`
VALUES (1720386239580925954, 1720379282501464065, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1720386239580925955, 1720379282501464065, 4);
INSERT INTO `sys_role_menu`
VALUES (1720386239580925956, 1720379282501464065, 5);
INSERT INTO `sys_role_menu`
VALUES (1720386239585120258, 1720379282501464065, 6);
INSERT INTO `sys_role_menu`
VALUES (1720386239585120259, 1720379282501464065, 7);
INSERT INTO `sys_role_menu`
VALUES (1720386239589314562, 1720379282501464065, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1720386239589314563, 1720379282501464065, 9);
INSERT INTO `sys_role_menu`
VALUES (1720386239589314564, 1720379282501464065, 10);
INSERT INTO `sys_role_menu`
VALUES (1720386239593508866, 1720379282501464065, 1702313362839003137);
INSERT INTO `sys_role_menu`
VALUES (1720386239593508867, 1720379282501464065, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1720386239593508868, 1720379282501464065, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1720386239597703170, 1720379282501464065, 1702313296111820802);
INSERT INTO `sys_role_menu`
VALUES (1720386239597703171, 1720379282501464065, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1720386239601897473, 1720379282501464065, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1720386239601897474, 1720379282501464065, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1720386239601897475, 1720379282501464065, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1720386239606091777, 1720379282501464065, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1720386239606091778, 1720379282501464065, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1720386239606091779, 1720379282501464065, 1702314309686353921);
INSERT INTO `sys_role_menu`
VALUES (1720386239610286081, 1720379282501464065, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1720386239610286082, 1720379282501464065, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1720386239614480385, 1720379282501464065, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1720386239614480386, 1720379282501464065, 1702313111461781506);
INSERT INTO `sys_role_menu`
VALUES (1720386239614480387, 1720379282501464065, 1699773810437967874);
INSERT INTO `sys_role_menu`
VALUES (1720386239618674689, 1720379282501464065, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1720386239622868993, 1720379282501464065, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1720386239622868994, 1720379282501464065, 1702313163114635266);
INSERT INTO `sys_role_menu`
VALUES (1720386239627063298, 1720379282501464065, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1725875961824604162, 1, 1);
INSERT INTO `sys_role_menu`
VALUES (1725875961828798465, 1, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1725875961832992770, 1, 2);
INSERT INTO `sys_role_menu`
VALUES (1725875961832992771, 1, 3);
INSERT INTO `sys_role_menu`
VALUES (1725875961837187073, 1, 1702313890977374209);
INSERT INTO `sys_role_menu`
VALUES (1725875961841381378, 1, 4);
INSERT INTO `sys_role_menu`
VALUES (1725875961841381379, 1, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1725875961845575681, 1, 5);
INSERT INTO `sys_role_menu`
VALUES (1725875961845575682, 1, 6);
INSERT INTO `sys_role_menu`
VALUES (1725875961849769985, 1, 7);
INSERT INTO `sys_role_menu`
VALUES (1725875961849769986, 1, 9);
INSERT INTO `sys_role_menu`
VALUES (1725875961853964289, 1, 10);
INSERT INTO `sys_role_menu`
VALUES (1725875961853964290, 1, 11);
INSERT INTO `sys_role_menu`
VALUES (1725875961858158594, 1, 1702313362839003137);
INSERT INTO `sys_role_menu`
VALUES (1725875961858158595, 1, 1699399891600224258);
INSERT INTO `sys_role_menu`
VALUES (1725875961862352897, 1, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1725875961862352898, 1, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1725875961866547201, 1, 1702314132380540929);
INSERT INTO `sys_role_menu`
VALUES (1725875961870741505, 1, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1725875961870741506, 1, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1725875961874935809, 1, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1725875961879130113, 1, 1702314309686353921);
INSERT INTO `sys_role_menu`
VALUES (1725875961879130114, 1, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1725875961883324417, 1, 1702313955213139969);
INSERT INTO `sys_role_menu`
VALUES (1725875961883324418, 1, 1702313672923897858);
INSERT INTO `sys_role_menu`
VALUES (1725875961887518721, 1, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1725875961887518722, 1, 1702313163114635266);
INSERT INTO `sys_role_menu`
VALUES (1725875961891713026, 1, 1723981157108486145);
INSERT INTO `sys_role_menu`
VALUES (1725875961895907329, 1, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1725875961895907330, 1, 1703249662781853698);
INSERT INTO `sys_role_menu`
VALUES (1725875961900101633, 1, 1723980908860215298);
INSERT INTO `sys_role_menu`
VALUES (1725875961900101634, 1, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1725875961904295938, 1, 1702314174294220801);
INSERT INTO `sys_role_menu`
VALUES (1725875961904295939, 1, 1702313296111820802);
INSERT INTO `sys_role_menu`
VALUES (1725875961908490241, 1, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1725875961908490242, 1, 1702313559610580994);
INSERT INTO `sys_role_menu`
VALUES (1725875961912684545, 1, 1702313475783221250);
INSERT INTO `sys_role_menu`
VALUES (1725875961916878850, 1, 1702314008166227970);
INSERT INTO `sys_role_menu`
VALUES (1725875961921073153, 1, 1702314062809620482);
INSERT INTO `sys_role_menu`
VALUES (1725875961921073154, 1, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1725875961925267458, 1, 1723981087562731521);
INSERT INTO `sys_role_menu`
VALUES (1725875961925267459, 1, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1725875961929461762, 1, 1702313775076171777);
INSERT INTO `sys_role_menu`
VALUES (1725875961929461763, 1, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1725875961933656065, 1, 1702314229730336769);
INSERT INTO `sys_role_menu`
VALUES (1725875961933656066, 1, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1725875961937850370, 1, 1723980017025355778);
INSERT INTO `sys_role_menu`
VALUES (1725875961937850371, 1, 1702313111461781506);
INSERT INTO `sys_role_menu`
VALUES (1725875961942044673, 1, 1699773810437967874);
INSERT INTO `sys_role_menu`
VALUES (1725875961942044674, 1, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1725876028962828290, 2, 1703249662781853698);
INSERT INTO `sys_role_menu`
VALUES (1725876028967022594, 2, 1);
INSERT INTO `sys_role_menu`
VALUES (1725876028971216898, 2, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1725876028971216899, 2, 2);
INSERT INTO `sys_role_menu`
VALUES (1725876028975411202, 2, 3);
INSERT INTO `sys_role_menu`
VALUES (1725876028975411203, 2, 4);
INSERT INTO `sys_role_menu`
VALUES (1725876028979605506, 2, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1725876028979605507, 2, 5);
INSERT INTO `sys_role_menu`
VALUES (1725876028983799810, 2, 6);
INSERT INTO `sys_role_menu`
VALUES (1725876028983799811, 2, 7);
INSERT INTO `sys_role_menu`
VALUES (1725876028987994114, 2, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1725876028987994115, 2, 9);
INSERT INTO `sys_role_menu`
VALUES (1725876028992188417, 2, 10);
INSERT INTO `sys_role_menu`
VALUES (1725876028992188418, 2, 11);
INSERT INTO `sys_role_menu`
VALUES (1725876028996382721, 2, 1702314174294220801);
INSERT INTO `sys_role_menu`
VALUES (1725876028996382722, 2, 1702313362839003137);
INSERT INTO `sys_role_menu`
VALUES (1725876028996382723, 2, 1699399891600224258);
INSERT INTO `sys_role_menu`
VALUES (1725876029000577025, 2, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1725876029000577026, 2, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1725876029004771330, 2, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1725876029004771331, 2, 1702313296111820802);
INSERT INTO `sys_role_menu`
VALUES (1725876029008965633, 2, 1702314132380540929);
INSERT INTO `sys_role_menu`
VALUES (1725876029008965634, 2, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1725876029008965635, 2, 1702313559610580994);
INSERT INTO `sys_role_menu`
VALUES (1725876029013159937, 2, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1725876029013159938, 2, 1702313475783221250);
INSERT INTO `sys_role_menu`
VALUES (1725876029017354241, 2, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1725876029017354242, 2, 1702314062809620482);
INSERT INTO `sys_role_menu`
VALUES (1725876029017354243, 2, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1725876029021548546, 2, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1725876029021548547, 2, 1702314309686353921);
INSERT INTO `sys_role_menu`
VALUES (1725876029025742849, 2, 1702313775076171777);
INSERT INTO `sys_role_menu`
VALUES (1725876029025742850, 2, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1725876029029937154, 2, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1725876029029937155, 2, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1725876029029937156, 2, 1702314229730336769);
INSERT INTO `sys_role_menu`
VALUES (1725876029034131457, 2, 1702313672923897858);
INSERT INTO `sys_role_menu`
VALUES (1725876029034131458, 2, 1702313111461781506);
INSERT INTO `sys_role_menu`
VALUES (1725876029038325761, 2, 1699773810437967874);
INSERT INTO `sys_role_menu`
VALUES (1725876029038325762, 2, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1725876029038325763, 2, 1702313163114635266);
INSERT INTO `sys_role_menu`
VALUES (1725876029042520066, 2, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1725876029042520067, 2, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1725876073686691841, 1703241493385838593, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1725876073686691842, 1703241493385838593, 1);
INSERT INTO `sys_role_menu`
VALUES (1725876073690886146, 1703241493385838593, 2);
INSERT INTO `sys_role_menu`
VALUES (1725876073690886147, 1703241493385838593, 3);
INSERT INTO `sys_role_menu`
VALUES (1725876073695080449, 1703241493385838593, 4);
INSERT INTO `sys_role_menu`
VALUES (1725876073695080450, 1703241493385838593, 5);
INSERT INTO `sys_role_menu`
VALUES (1725876073699274754, 1703241493385838593, 6);
INSERT INTO `sys_role_menu`
VALUES (1725876073699274755, 1703241493385838593, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1725876073703469057, 1703241493385838593, 9);
INSERT INTO `sys_role_menu`
VALUES (1725876073703469058, 1703241493385838593, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1725876073703469059, 1703241493385838593, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1729023963359670273, 1729023963212869633, 1);
INSERT INTO `sys_role_menu`
VALUES (1729023963368058881, 1729023963212869633, 3);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `id`      bigint NOT NULL,
    `user_id` bigint NOT NULL COMMENT '用户id',
    `role_id` bigint NOT NULL COMMENT '角色id',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------

-- ----------------------------
-- Table structure for tr_login
-- ----------------------------
DROP TABLE IF EXISTS `tr_login`;
CREATE TABLE `easy_oa`.`tr_login`
(
    `id`         bigint                                                        NOT NULL,
    `tr_id`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方id/openid',
    `user_id`    bigint NULL DEFAULT NULL COMMENT '本系统userId',
    `status`     int                                                           NOT NULL COMMENT '是否完成注册或者绑定已有帐号1,完成',
    `is_deleted` int                                                           NOT NULL DEFAULT 0 COMMENT '默认未删除',
    `type`       varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'qq,wx....',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tr_login
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`            bigint                                                        NOT NULL COMMENT '用户id',
    `name`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '用户昵称',
    `username`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '用户名',
    `email`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绑定邮箱',
    `password`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    `salt`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐',
    `sex`           varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL COMMENT '性别',
    `avatar`        mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '头像',
    `student_id`    varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
    `dept_id`       bigint                                                        NOT NULL DEFAULT 0 COMMENT '属于哪个部门',
    `status`        int                                                           NOT NULL COMMENT '状态，1为正常，0为封号',
    `phone`         varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
    `login_ip`      json NULL COMMENT '最后登录IP',
    `login_date`    datetime(3) NULL DEFAULT NULL COMMENT '最后登录时间',
    `create_user`   bigint NULL DEFAULT NULL,
    `active_status` int NULL DEFAULT 2 COMMENT '1在线或者离线0',
    `open_id`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'openid',
    `create_time`   datetime                                                      NOT NULL COMMENT '注册时间',
    `update_time`   datetime                                                      NOT NULL COMMENT '更新时间',
    `is_deleted`    int                                                           NOT NULL COMMENT '逻辑删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX           `dept_id`(`dept_id` ASC) USING BTREE,
    CONSTRAINT `dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user`
VALUES (1, 'admin', 'admin', 'admin@admin.com', 'b30dc295eabc8c9821b70d3830c227d7', 'BzxrDd.SuMAxIBM7', '男',
        '18e956a8440341e3945bfb405953a396.png', '202115040212', 1, 1, '13986530157', '{
    \"createIp\": \"192.168.12.254\", \"updateIp\": \"192.168.12.254\", \"createIpDetail\": null, \"updateIpDetail\": null}',
        '2023-11-27 17:42:09.680', NULL, 1, '9eb3daa8-f286-4531-94a4-d4c6f7dbc49b', '2023-05-20 23:10:46',
        '2023-11-27 17:42:10', 0);
INSERT INTO `user`
VALUES (10010, 'chatgpt', 'chatgpt', 'chatgpt@aien.com', NULL, NULL, '男', '18e956a8440341e3945bfb405953a396.png', NULL,
        1, 1, '13900000000', '{
    \"createIp\": \"0:0:0:0:0:0:0:1\", \"updateIp\": \"0:0:0:0:0:0:0:1\", \"createIpDetail\": null, \"updateIpDetail\": null}',
        '2023-09-28 21:16:01.000', NULL, 1, '063d8701-00a3-4349-b97a-b69fbb77cde4', '2023-09-28 21:10:07',
        '2023-11-06 21:06:15', 0);
INSERT INTO `user`
VALUES (10011, '系统通知', 'systemmessage', 'systemmessage@aien.com', NULL, NULL, '男',
        '18e956a8440341e3945bfb405953a396.png', NULL, 1, 1, '13900000000', '{
    \"createIp\": \"0:0:0:0:0:0:0:1\", \"updateIp\": \"0:0:0:0:0:0:0:1\", \"createIpDetail\": null, \"updateIpDetail\": null}',
        '2023-09-28 21:16:01.000', NULL, 1, '063d8701-00a3-4349-b97a-b69fbb77cde4', '2023-09-28 21:10:07',
        '2023-11-06 21:06:15', 0);

-- ----------------------------
-- Table structure for user_apply
-- ----------------------------
DROP TABLE IF EXISTS `user_apply`;
CREATE TABLE `user_apply`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `uid`         bigint                                                       NOT NULL COMMENT '申请人uid',
    `type`        int                                                          NOT NULL COMMENT '申请类型 1加好友',
    `target_id`   bigint                                                       NOT NULL COMMENT '接收人uid',
    `msg`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请信息',
    `status`      int                                                          NOT NULL COMMENT '申请状态 1待审批 2同意',
    `read_status` int                                                          NOT NULL COMMENT '阅读状态 1未读 2已读',
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX         `idx_target_id_uid_status`(`target_id` ASC, `uid` ASC, `status` ASC) USING BTREE,
    INDEX         `idx_target_id`(`target_id` ASC) USING BTREE,
    INDEX         `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX         `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_apply
-- ----------------------------

-- ----------------------------
-- Table structure for user_emoji
-- ----------------------------
DROP TABLE IF EXISTS `user_emoji`;
CREATE TABLE `user_emoji`
(
    `id`             bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `uid`            bigint                                                        NOT NULL COMMENT '用户表ID',
    `expression_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '表情地址',
    `delete_status`  int                                                           NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)',
    `create_time`    datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time`    datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX            `IDX_USER_EMOJIS_UID`(`uid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表情包' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_emoji
-- ----------------------------

-- ----------------------------
-- Table structure for user_friend
-- ----------------------------
DROP TABLE IF EXISTS `user_friend`;
CREATE TABLE `user_friend`
(
    `id`            bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `uid`           bigint NOT NULL COMMENT 'uid',
    `friend_uid`    bigint NOT NULL COMMENT '好友uid',
    `delete_status` int    NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)',
    `create_time`   datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '创建时间',
    `update_time`   datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX           `idx_uid_friend_uid`(`uid` ASC, `friend_uid` ASC) USING BTREE,
    INDEX           `idx_create_time`(`create_time` ASC) USING BTREE,
    INDEX           `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户联系人表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_friend
-- ----------------------------
DROP TABLE IF EXISTS `sys_confirm`;
CREATE TABLE `sys_confirm`
(
    `id`          bigint                                                        NOT NULL,
    `str_key`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '要用户确认的key',
    `user_id`     bigint                                                        NOT NULL COMMENT '用户确认',
    `create_time` datetime(3) NOT NULL COMMENT '确认时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `key_userid` (`str_key`,`user_id`) USING BTREE,
    KEY           `userid` (`user_id`) USING BTREE,
    KEY           `key` (`str_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ----------------------------
-- Table structure for user_front_config
-- ----------------------------
DROP TABLE IF EXISTS `user_front_config`;
CREATE TABLE `user_front_config`
(
    `id`          bigint   NOT NULL,
    `user_id`     bigint   NOT NULL,
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    `config_str`  longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'json_str',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_front_config
-- ----------------------------
SET
FOREIGN_KEY_CHECKS = 1;
