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

 Date: 24/01/2024 16:06:18
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
INSERT INTO `index_image`
VALUES (1, 'AI值日表', 'ai值日.png', 0, '');
INSERT INTO `index_image`
VALUES (2, 'EN值日表', '1大雾四起20230531214741.png', 0, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 201 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

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
    `content`           longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '富文本,当type为2时此处就是url了',
    `status`            int                                                            NOT NULL DEFAULT 0 COMMENT '0为草稿，1为预发布，2为发布/定时发布，3为禁止查看',
    `type`              int                                                            NOT NULL DEFAULT 1 COMMENT '1:content模式，2:外链模式',
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
INSERT INTO `notice`
VALUES (1739708470163247106, 'NAO相关教程',
        '<h1 style=\"text-align: left;\"><strong><span style=\"font-size: 20px;\">NAO相关教程</span></strong></h1>\n<p style=\"text-align: left;\"><strong><span style=\"font-size: 20px;\">NAO中文手册</span></strong> <a href=\"https://twoq.gitee.io/naoqi.net/\">https://twoq.gitee.io/naoqi.net/</a></p>\n<p style=\"text-align: left;\"><strong>naoqi API</strong>&nbsp;<a href=\"http://doc.aldebaran.com/2-5/naoqi/index.html\">http://doc.aldebaran.com/2-5/naoqi/index.html</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>nao机器人行走速度</strong> <a href=\"https://blog.csdn.net/weixin_39689428/article/details/111747248?utm_medium=distribute.pc_relevant.none-task-blog-baidujs_title-1&amp;spm=1001.2101.3001.4242\">https://blog.csdn.net/weixin_39689428/article/details/111747248?utm_medium=distribute.pc_relevant.none-task-blog-baidujs_title-1&amp;spm=1001.2101.3001.4242</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>NAO机器人的小记</strong> <a href=\"https://blog.csdn.net/qq_43309286/article/details/102894708\">https://blog.csdn.net/qq_43309286/article/details/102894708</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>NAO基本操作１&mdash;&mdash;Motion</strong> &nbsp;<a href=\"https://blog.csdn.net/yuanchengzhizuishuai/article/details/93843464\">https://blog.csdn.net/yuanchengzhizuishuai/article/details/93843464</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>nao机器人--移动、是否摔倒、opencv图像处理、socket通信</strong> <a href=\"https://blog.csdn.net/weixin_40490238/article/details/84998668?ops_request_misc=&amp;request_id=&amp;biz_id=102&amp;utm_term=nao%E6%9C%BA%E5%99%A8%E4%BA%BA%E5%9B%BE%E5%83%8F%E8%AF%86%E5%88%AB&amp;utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-2-84998668.first_rank_v2_pc_rank_v29\">https://blog.csdn.net/weixin_40490238/article/details/84998668?ops_request_misc=&amp;request_id=&amp;biz_id=102&amp;utm_term=nao%E6%9C%BA%E5%99%A8%E4%BA%BA%E5%9B%BE%E5%83%8F%E8%AF%86%E5%88%AB&amp;utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-2-84998668.first_rank_v2_pc_rank_v29</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>NAO机器人知行知的专栏</strong> <a href=\"https://blog.csdn.net/liuying_1001/category_836993.html\">https://blog.csdn.net/liuying_1001/category_836993.html</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>python控制nao机器人身体动作实例</strong> <a href=\"https://blog.csdn.net/u011181878/article/details/21618239?ops_request_misc=&amp;request_id=&amp;biz_id=102&amp;utm_term=nao%20python%20%E8%A7%A6%E6%91%B8%E5%A4%B4%E9%83%A8&amp;utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-2-21618239.first_rank_v2_pc_rank_v29\">https://blog.csdn.net/u011181878/article/details/21618239?ops_request_misc=&amp;request_id=&amp;biz_id=102&amp;utm_term=nao%20python%20%E8%A7%A6%E6%91%B8%E5%A4%B4%E9%83%A8&amp;utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-2-21618239.first_rank_v2_pc_rank_v29</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>基于python下Nao机器人的行走及步态参数</strong> <a href=\"https://blog.csdn.net/qazwsx0316/article/details/105164751\">https://blog.csdn.net/qazwsx0316/article/details/105164751</a>&nbsp;</p>\n<p style=\"text-align: left;\"><strong>基于python下Nao机器人的头顶传感器</strong> <a href=\"https://blog.csdn.net/qazwsx0316/article/details/105164709?ops_request_misc=&amp;request_id=&amp;biz_id=102&amp;utm_term=nao%20python%20%E8%A7%A6%E6%91%B8%E5%A4%B4%E9%83%A8&amp;utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-1-105164709.first_rank_v2_pc_rank_v29\">https://blog.csdn.net/qazwsx0316/article/details/105164709?ops_request_misc=&amp;request_id=&amp;biz_id=102&amp;utm_term=nao%20python%20%E8%A7%A6%E6%91%B8%E5%A4%B4%E9%83%A8&amp;utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-1-105164709.first_rank_v2_pc_rank_v29</a></p>\n<h1 style=\"text-align: left;\">相关的环境包前往文件服务器下载&nbsp;</h1>\n<h1 style=\"text-align: left;\"><span style=\"font-size: 20px;\"><a href=\"http://easyoa.fun:81/files/Nao%E8%B5%84%E6%96%99/\">Nao资料 - 文件 - AI工作室 (easyoa.fun)</a></span></h1>\n<p><img style=\"float: left;\" src=\"http://easyoa.fun:9090/aistudio/imag1703613993267.png\" width=\"885\" height=\"431\"></p>',
        2, 1, 0, 0, 0, '2023-12-27 02:03:16', '2023-12-27 09:41:29', 1659941852221624321, 1659941852221624321,
        'AI,假期任务,教程,NAO', 1659941852221624321, '邱成', '2023-12-27 02:07:01', 1700781688674267138, '大三组', 1,
        13, '1659941852221624321', 2);
INSERT INTO `notice`
VALUES (1739711706517573633, '测试通知-点击前往百度', 'https://baidu.com', 2, 2, 0, 0, 0, '2023-12-27 02:16:08',
        '2023-12-27 02:16:15', 1659941852221624321, 1659941852221624321, '测试', 1659941852221624321, '邱成',
        '2023-12-27 02:16:15', 1700781688674267138, '大三组', 3, 3, '1659941852221624321', 1);

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
    `file_name`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件名',
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
INSERT INTO `notice_dept`
VALUES (1739708470196801538, 1739708470163247106, 1700781419697745922, 1, '2023-12-27 02:03:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739708470209384449, 1739708470163247106, 1720383768015990786, 1, '2023-12-27 02:03:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739708470213578753, 1739708470163247106, 1700781578091442177, 1, '2023-12-27 02:03:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739708470221967361, 1739708470163247106, 1700781688674267138, 1, '2023-12-27 02:03:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739708470230355969, 1739708470163247106, 1720384001928130561, 1, '2023-12-27 02:03:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739708470234550273, 1739708470163247106, 1730628785854803970, 1, '2023-12-27 02:03:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739709413009231873, 1739708470163247106, 1700781419697745922, 1, '2023-12-27 02:07:01', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739709413013426177, 1739708470163247106, 1720383768015990786, 1, '2023-12-27 02:07:01', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739709413017620481, 1739708470163247106, 1700781578091442177, 1, '2023-12-27 02:07:01', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739709413021814786, 1739708470163247106, 1700781688674267138, 1, '2023-12-27 02:07:01', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739709413034397698, 1739708470163247106, 1720384001928130561, 1, '2023-12-27 02:07:01', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739709413038592002, 1739708470163247106, 1730628785854803970, 1, '2023-12-27 02:07:01', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710020398977025, 1739708470163247106, 1700781419697745922, 1, '2023-12-27 02:09:26', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710020403171330, 1739708470163247106, 1720383768015990786, 1, '2023-12-27 02:09:26', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710020407365634, 1739708470163247106, 1700781578091442177, 1, '2023-12-27 02:09:26', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710020411559938, 1739708470163247106, 1700781688674267138, 1, '2023-12-27 02:09:26', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710020415754242, 1739708470163247106, 1720384001928130561, 1, '2023-12-27 02:09:26', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710020419948545, 1739708470163247106, 1730628785854803970, 1, '2023-12-27 02:09:26', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710484582600706, 1739708470163247106, 1700781419697745922, 1, '2023-12-27 02:11:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710484586795009, 1739708470163247106, 1720383768015990786, 1, '2023-12-27 02:11:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710484590989314, 1739708470163247106, 1700781578091442177, 1, '2023-12-27 02:11:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710484595183618, 1739708470163247106, 1700781688674267138, 1, '2023-12-27 02:11:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710484595183619, 1739708470163247106, 1720384001928130561, 1, '2023-12-27 02:11:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739710484599377921, 1739708470163247106, 1730628785854803970, 1, '2023-12-27 02:11:16', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739823784653852673, 1739708470163247106, 1700781419697745922, 0, '2023-12-27 09:41:29', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739823784662241282, 1739708470163247106, 1720383768015990786, 0, '2023-12-27 09:41:29', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739823784666435586, 1739708470163247106, 1700781578091442177, 0, '2023-12-27 09:41:29', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739823784670629889, 1739708470163247106, 1700781688674267138, 0, '2023-12-27 09:41:29', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739823784674824193, 1739708470163247106, 1720384001928130561, 0, '2023-12-27 09:41:29', 1659941852221624321);
INSERT INTO `notice_dept`
VALUES (1739823784683212801, 1739708470163247106, 1730628785854803970, 0, '2023-12-27 09:41:29', 1659941852221624321);

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
    `name`                        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名称',
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
    `visibility` int                                                           NOT NULL DEFAULT 0 COMMENT '0默认都可见，1为部分部门可见',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of quick_navigation_categorize
-- ----------------------------

-- ----------------------------
-- Table structure for quick_navigation_dept
-- ----------------------------
DROP TABLE IF EXISTS `quick_navigation_dept`;
CREATE TABLE `quick_navigation_dept`
(
    `id`                      bigint NOT NULL COMMENT '索引id',
    `quick_nav_categorize_id` bigint NOT NULL COMMENT '导航分类id',
    `dept_id`                 bigint NOT NULL COMMENT '哪些部门可见，部门id',
    PRIMARY KEY (`id`, `quick_nav_categorize_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quick_navigation_dept
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
VALUES (1, 1, 1, '2024-01-16 16:14:11.206', 200, NULL, '2023-09-19 12:08:57.390', '2024-01-16 08:14:10.911');
INSERT INTO `room`
VALUES (2, 1, 1, '2023-11-29 20:08:21.647', 180, NULL, '2023-11-27 09:39:09.657', '2023-11-29 12:08:21.720');

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
) ENGINE = InnoDB AUTO_INCREMENT = 212 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '本地消息表' ROW_FORMAT = DYNAMIC;

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
-- Table structure for sys_confirm
-- ----------------------------
DROP TABLE IF EXISTS `sys_confirm`;
CREATE TABLE `sys_confirm`
(
    `id`          bigint                                                        NOT NULL,
    `str_key`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '要用户确认的key',
    `user_id`     bigint                                                        NOT NULL COMMENT '用户确认',
    `create_time` datetime(3) NOT NULL COMMENT '确认时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `key_userid`(`str_key` ASC, `user_id` ASC) USING BTREE,
    INDEX         `userid`(`user_id` ASC) USING BTREE,
    INDEX         `key`(`str_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_confirm
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 1730628785854803971 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept`
VALUES (1, 0, NULL, 'AI-EN-IS', 0, 'Super Admin', '13986530157', 'qc2003020403@163.com', 1, 0, 1659941852221624321,
        '2023-08-30 14:44:01', 1659941852221624321, '2023-08-30 14:44:03');
INSERT INTO `sys_dept`
VALUES (1700781578091442177, 1700781419697745922, '1,1700781419697745922,1700781419697745922', '大二组', 2, '张姝曼',
        '', '', 1, 0, 1659941852221624321, '2023-09-10 16:01:42', 1659941852221624321, '2023-09-10 16:01:42');
INSERT INTO `sys_dept`
VALUES (1700781638367784962, 1700781459237449729, '1,1700781459237449729,1700781459237449729', '大二组', 2, '林勇志',
        '', '', 1, 0, 1659941852221624321, '2023-09-10 16:01:56', 1659941852221624321, '2023-09-10 16:01:56');
INSERT INTO `sys_dept`
VALUES (1700781688674267138, 1700781419697745922, '1,1700781419697745922,1700781419697745922', '大三组', 3, '', '', '',
        1, 0, 1659941852221624321, '2023-09-10 16:02:08', 1659941852221624321, '2023-09-10 16:02:08');
INSERT INTO `sys_dept`
VALUES (1700781715454898177, 1700781459237449729, '1,1700781459237449729,1700781459237449729', '大三组', 3, '孙焕然',
        '', '', 1, 0, 1659941852221624321, '2023-09-10 16:02:15', 1659941852221624321, '2023-09-10 16:02:15');
INSERT INTO `sys_dept`
VALUES (1720377175597707265, 1, '1,1', '智能系统', 3, '洪天宝', '', '', 1, 0, 1659941852221624321,
        '2023-11-03 09:47:36', 1659941852221624321, '2023-11-03 09:47:36');
INSERT INTO `sys_dept`
VALUES (1720377316845088770, 1720377175597707265, '1,1720377175597707265,1720377175597707265', '大一组', 1, '', '', '',
        1, 0, 1659941852221624321, '2023-11-03 09:48:10', 1659941852221624321, '2023-11-03 09:48:10');
INSERT INTO `sys_dept`
VALUES (1720377394477461505, 1720377175597707265, '1,1720377175597707265,1720377175597707265', '大二组', 2, '', '', '',
        1, 0, 1659941852221624321, '2023-11-03 09:48:29', 1659941852221624321, '2023-11-03 09:48:29');
INSERT INTO `sys_dept`
VALUES (1720377462601347074, 1720377175597707265, '1,1720377175597707265,1720377175597707265', '大三组', 3, '', '', '',
        1, 0, 1659941852221624321, '2023-11-03 09:48:45', 1659941852221624321, '2023-11-03 09:48:45');
INSERT INTO `sys_dept`
VALUES (1720383768015990786, 1700781419697745922, '1,1700781419697745922,1700781419697745922', '大一组', 1, '', '', '',
        1, 0, 1659941852221624321, '2023-11-03 10:13:48', 1659941852221624321, '2023-11-03 10:13:48');
INSERT INTO `sys_dept`
VALUES (1720384001928130561, 1700781419697745922, '1,1700781419697745922,1700781419697745922', '大四', 4, '', '', '', 1,
        0, 1659941852221624321, '2023-11-03 10:14:44', 1659941852221624321, '2023-11-03 10:14:44');
INSERT INTO `sys_dept`
VALUES (1720384124842209281, 1700781459237449729, '1,1700781459237449729,1700781459237449729', '大一', 1, '', '', '', 1,
        0, 1659941852221624321, '2023-11-03 10:15:13', 1659941852221624321, '2023-11-03 10:15:13');
INSERT INTO `sys_dept`
VALUES (1720384154269446146, 1700781459237449729, '1,1700781459237449729,1700781459237449729', '大四', 4, '', '', '', 1,
        0, 1659941852221624321, '2023-11-03 10:15:20', 1659941852221624321, '2023-11-03 10:15:20');
INSERT INTO `sys_dept`
VALUES (1720384194815782913, 1720377175597707265, '1,1720377175597707265,1720377175597707265', '大四', 4, '', '', '', 1,
        0, 1659941852221624321, '2023-11-03 10:15:30', 1659941852221624321, '2023-11-03 10:15:30');
INSERT INTO `sys_dept`
VALUES (1729023789828730882, 1, '1,1', '其他信任组[允许打印]', 20, '', '', '', 1, 0, 1659941852221624321,
        '2023-11-27 14:26:10', 1659941852221624321, '2023-11-27 14:26:10');
INSERT INTO `sys_dept`
VALUES (1730628451526832129, 1, '1,1', '系统功能用户', 21, '', '', '', 1, 0, 1659941852221624321, '2023-12-02 00:42:31',
        1659941852221624321, '2023-12-02 00:42:31');
INSERT INTO `sys_dept`
VALUES (1730628504693829633, 1, '1,1', '已退出', 22, '', '', '', 1, 0, 1659941852221624321, '2023-12-02 00:42:44',
        1659941852221624321, '2023-12-02 00:42:44');
INSERT INTO `sys_dept`
VALUES (1730628785854803970, 1700781419697745922, '1,1700781419697745922,1700781419697745922', '研究生', 5, '', '', '',
        1, 0, 1659941852221624321, '2023-12-02 00:43:51', 1659941852221624321, '2023-12-02 00:43:51');

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
) ENGINE = InnoDB AUTO_INCREMENT = 1747172616846344195 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu`
VALUES (1, 'dashboard', 'menu.server.dashboard', 0, 1, '/dashboard', 0, 0, 'M', 1, 1, NULL, 'icon-dashboard', NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (2, 'Workplace', 'menu.dashboard.workplace', 1, 1, 'workplace', 0, 0, 'C', 1, 1, '', '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (3, 'printer', 'menu.dashboard.printer', 1, 2, 'printer', 0, 0, 'C', 1, 1, 'sys:print:list', '', NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_menu`
VALUES (4, 'keep', 'menu.dashboard.keep', 1, 3, 'keep', 0, 0, 'C', 1, 1, '', '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (5, 'usercenter', 'menu.usercenter', 0, 2, '/usercenter', 0, 0, 'M', 1, 1, NULL, 'icon-user', NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_menu`
VALUES (6, 'profile', 'menu.usercenter.usercenter', 5, 1, 'profile', 0, 0, 'C', 1, 1, '', '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (7, 'UserManger', 'menu.sysmanger.UserManger', 10, 1, 'user-manger', 0, 0, 'C', 1, 1, 'sys:user:list', '', NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (9, 'fileBrowser', 'menu.fileBrowser', 0, 7,
        'http://easyoa.fun:55550/api/oauth2.0/authorize?response_type=code&client_id=3a1cab58d4b743d681a042ba20955178&redirect_uri=http://easyoa.fun:81/login&state=test&scope=get_user_info',
        1, 0, 'M', 1, 1, NULL, 'icon-folder', NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (10, 'sysmanger', 'menu.sysmanger', 0, 3, '/sysmanger', 0, 0, 'M', 1, 1, '', 'icon-menu', NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_menu`
VALUES (11, 'MenuManger', 'menu.sysmanger.MenuManger', 10, 2, 'menu-manger', 0, 0, 'C', 1, 1, 'sys:menu:list', '', NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (1698681230501560322, 'QuickRouter', 'menu.QuickRouter', 0, 8, 'http://10.15.247.254:55554/', 1, 0, 'C', 0, 0,
        '', 'icon-apps', NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (1699399891600224258, 'RoleManger', 'menu.sysmanger.RoleManger', 10, 3, 'role-manger', 0, 0, 'C', 1, 1,
        'sys:role:list', '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu`
VALUES (1699773810437967874, 'DeptManger', 'menu.sysmanger.DeptManger', 10, 4, 'dept-manger', 0, 0, 'C', 1, 1,
        'sys:study:list', '', 1659941852221624321, '2023-09-07 21:17:11', 1659941852221624321, '2023-09-07 21:17:11');
INSERT INTO `sys_menu`
VALUES (1702312893739655169, '', '查看打卡数据', 4, 1, '', 0, 0, 'F', 1, 1, 'sys:study:list', '', 1659939726386827265,
        '2023-09-14 21:26:36', 1659939726386827265, '2023-09-14 21:26:36');
INSERT INTO `sys_menu`
VALUES (1702313016154611713, '', '管理配置', 4, 1, '', 0, 0, 'F', 1, 1, 'sys:study:manger', '', 1659939726386827265,
        '2023-09-14 21:27:05', 1659939726386827265, '2023-09-14 21:27:05');
INSERT INTO `sys_menu`
VALUES (1702313111461781506, '', '添加用户', 7, 1, '', 0, 0, 'F', 1, 1, 'sys:user:add', '', 1659939726386827265,
        '2023-09-14 21:27:28', 1659939726386827265, '2023-09-14 21:27:28');
INSERT INTO `sys_menu`
VALUES (1702313163114635266, '', '删除用户', 7, 2, '', 0, 0, 'F', 1, 1, 'sys:user:delete', '', 1659939726386827265,
        '2023-09-14 21:27:40', 1659939726386827265, '2023-09-14 21:27:40');
INSERT INTO `sys_menu`
VALUES (1702313296111820802, '', '更新用户', 7, 3, '', 0, 0, 'F', 1, 1, 'sys:user:update', '', 1659939726386827265,
        '2023-09-14 21:28:12', 1659939726386827265, '2023-09-14 21:28:12');
INSERT INTO `sys_menu`
VALUES (1702313362839003137, '', '用户列表', 7, 1, '', 0, 0, 'F', 1, 1, 'sys:user:list', '', 1659939726386827265,
        '2023-09-14 21:28:28', 1659939726386827265, '2023-09-14 21:28:28');
INSERT INTO `sys_menu`
VALUES (1702313475783221250, '', '添加菜单', 11, 1, '', 0, 0, 'F', 1, 1, 'sys:menu:add', '', 1659939726386827265,
        '2023-09-14 21:28:55', 1659939726386827265, '2023-09-14 21:28:55');
INSERT INTO `sys_menu`
VALUES (1702313559610580994, '', '删除菜单', 11, 2, '', 0, 0, 'F', 1, 1, 'sys:menu:delete', '', 1659939726386827265,
        '2023-09-14 21:29:15', 1659939726386827265, '2023-09-14 21:29:15');
INSERT INTO `sys_menu`
VALUES (1702313672923897858, '', '修改菜单', 11, 3, '', 0, 0, 'F', 1, 1, 'sys:menu:update', '', 1659939726386827265,
        '2023-09-14 21:29:42', 1659939726386827265, '2023-09-14 21:29:42');
INSERT INTO `sys_menu`
VALUES (1702313775076171777, '', '菜单列表', 11, 4, '', 0, 0, 'F', 1, 1, 'sys:menu:list', '', 1659939726386827265,
        '2023-09-14 21:30:06', 1659939726386827265, '2023-09-14 21:30:06');
INSERT INTO `sys_menu`
VALUES (1702313890977374209, '', '添加角色', 1699399891600224258, 1, '', 0, 0, 'F', 1, 1, 'sys:role:add', '',
        1659939726386827265, '2023-09-14 21:30:34', 1659939726386827265, '2023-09-14 21:30:34');
INSERT INTO `sys_menu`
VALUES (1702313955213139969, '', '删除角色', 1699399891600224258, 2, '', 0, 0, 'F', 1, 1, 'sys:role:delete', '',
        1659939726386827265, '2023-09-14 21:30:49', 1659939726386827265, '2023-09-14 21:30:49');
INSERT INTO `sys_menu`
VALUES (1702314008166227970, '', '修改角色', 1699399891600224258, 3, '', 0, 0, 'F', 1, 1, 'sys:role:update', '',
        1659939726386827265, '2023-09-14 21:31:02', 1659939726386827265, '2023-09-14 21:31:02');
INSERT INTO `sys_menu`
VALUES (1702314062809620482, '', '角色列表', 1699399891600224258, 4, '', 0, 0, 'F', 1, 1, 'sys:role:list', '',
        1659939726386827265, '2023-09-14 21:31:15', 1659939726386827265, '2023-09-14 21:31:15');
INSERT INTO `sys_menu`
VALUES (1702314132380540929, '', '添加部门', 1699773810437967874, 1, '', 0, 0, 'F', 1, 1, 'sys:dept:add', '',
        1659939726386827265, '2023-09-14 21:31:31', 1659939726386827265, '2023-09-14 21:31:31');
INSERT INTO `sys_menu`
VALUES (1702314174294220801, '', '删除部门', 1699773810437967874, 2, '', 0, 0, 'F', 1, 1, 'sys:dept:delete', '',
        1659939726386827265, '2023-09-14 21:31:41', 1659939726386827265, '2023-09-14 21:31:41');
INSERT INTO `sys_menu`
VALUES (1702314229730336769, '', '修改部门', 1699773810437967874, 3, '', 0, 0, 'F', 1, 1, 'sys:dept:update', '',
        1659939726386827265, '2023-09-14 21:31:55', 1659939726386827265, '2023-09-14 21:31:55');
INSERT INTO `sys_menu`
VALUES (1702314309686353921, '', '部门列表', 1699773810437967874, 4, '', 0, 0, 'F', 1, 1, 'sys:dept:list', '',
        1659939726386827265, '2023-09-14 21:32:14', 1659939726386827265, '2023-09-14 21:32:14');
INSERT INTO `sys_menu`
VALUES (1703249662781853698, '', '全部数据权限', 3, 1, '', 0, 0, 'F', 1, 1, 'sys:print:alldata', '',
        1659941852221624321, '2023-09-17 11:28:59', 1659941852221624321, '2023-09-17 11:28:59');
INSERT INTO `sys_menu`
VALUES (1703268642263343105, 'ContentPromotion', 'menu.contentpromotion', 0, 4, '/content-promotion', 0, 0, 'M', 1, 1,
        '', 'icon-apps', 1659941852221624321, '2023-09-17 12:44:24', 1659941852221624321, '2023-09-17 12:44:24');
INSERT INTO `sys_menu`
VALUES (1703268748320514050, 'DocNotification', 'menu.docnotification.DocNotification', 1703268642263343105, 1,
        'docnotification', 0, 0, 'C', 1, 1, '', '', 1659941852221624321, '2023-09-17 12:44:49', 1659941852221624321,
        '2023-09-17 12:44:49');
INSERT INTO `sys_menu`
VALUES (1703328073911078914, 'IndexImage', 'menu.docnotification.IndexImage', 1703268642263343105, 2, 'indeximage', 0,
        0, 'C', 1, 1, '', '', 1659941852221624321, '2023-09-17 16:40:34', 1659941852221624321, '2023-09-17 16:40:34');
INSERT INTO `sys_menu`
VALUES (1703379296823468033, '', '新增图片', 1703328073911078914, 1, '', 0, 0, 'F', 1, 1, 'sys:indeximage:add', '',
        1659941852221624321, '2023-09-17 20:04:06', 1659941852221624321, '2023-09-17 20:04:06');
INSERT INTO `sys_menu`
VALUES (1703379381351276546, '', '删除图片', 1703328073911078914, 2, '', 0, 0, 'F', 1, 1, 'sys:indeximage:delete', '',
        1659941852221624321, '2023-09-17 20:04:26', 1659941852221624321, '2023-09-17 20:04:26');
INSERT INTO `sys_menu`
VALUES (1703379432723111938, '', '修改图片', 1703328073911078914, 3, '', 0, 0, 'F', 1, 1, 'sys:indeximage:update', '',
        1659941852221624321, '2023-09-17 20:04:39', 1659941852221624321, '2023-09-17 20:04:39');
INSERT INTO `sys_menu`
VALUES (1703379497562857474, '', '获取图片', 1703328073911078914, 4, '', 0, 0, 'F', 1, 1, 'sys:indeximage:list', '',
        1659941852221624321, '2023-09-17 20:04:54', 1659941852221624321, '2023-09-17 20:04:54');
INSERT INTO `sys_menu`
VALUES (1705549735037480962, 'Chat', 'menu.chat', 0, 5, '/chat', 0, 0, 'M', 1, 1, '', 'icon-info', 1659941852221624321,
        '2023-09-23 19:48:39', 1659941852221624321, '2023-09-23 19:48:39');
INSERT INTO `sys_menu`
VALUES (1705563149847699458, 'Chat-Index', 'menu.Message', 1705549735037480962, 1, 'chat', 0, 0, 'C', 1, 1,
        'sys:chat:list', '', 1659941852221624321, '2023-09-23 20:41:57', 1659941852221624321, '2023-09-23 20:41:57');
INSERT INTO `sys_menu`
VALUES (1709863803420348417, 'Entertainment', 'menu.entertainment', 0, 6, '/entertainment', 0, 0, 'M', 1, 1, '',
        'icon-clock-circle', 1659941852221624321, '2023-10-05 17:31:13', 1659941852221624321, '2023-10-05 17:31:13');
INSERT INTO `sys_menu`
VALUES (1709863993443291137, 'Center', 'menu.entertainment.center', 1709863803420348417, 1, 'center', 0, 0, 'C', 1, 1,
        'sys.entertainment.list', '', 1659941852221624321, '2023-10-05 17:31:58', 1659941852221624321,
        '2023-10-05 17:31:58');
INSERT INTO `sys_menu`
VALUES (1709914681489690626, 'Game2048', '2048', 1709863803420348417, 1, 'game2048', 0, 0, 'C', 0, 1, '', '',
        1659941852221624321, '2023-10-05 20:53:23', 1659941852221624321, '2023-10-05 20:53:23');
INSERT INTO `sys_menu`
VALUES (1719696557347688449, 'Version', 'menu.version', 0, 20, '/version', 0, 0, 'M', 0, 1, '', 'icon-info',
        1659941852221624321, '2023-11-01 20:43:04', 1659941852221624321, '2023-11-01 20:43:04');
INSERT INTO `sys_menu`
VALUES (1719696699043860482, 'VersionIndex', 'menu.version.index', 1719696557347688449, 1, 'index', 0, 0, 'C', 0, 1, '',
        '', 1659941852221624321, '2023-11-01 20:43:38', 1659941852221624321, '2023-11-01 20:43:38');
INSERT INTO `sys_menu`
VALUES (1723980017025355778, 'OauthManger', 'menu.sysmanger.OauthManger', 10, 5, '/oauth-manger', 0, 0, 'C', 1, 1,
        'sys:oauth:list', '', 1659941852221624321, '2023-11-13 16:24:01', 1659941852221624321, '2023-11-13 16:24:01');
INSERT INTO `sys_menu`
VALUES (1723980908860215298, '', '添加Oauth', 1723980017025355778, 1, '', 0, 0, 'F', 1, 1, 'sys.oauth:add', '',
        1659939726386827265, '2023-11-13 16:27:33', 1659939726386827265, '2023-11-13 16:27:33');
INSERT INTO `sys_menu`
VALUES (1723981087562731521, '', '更新Oauth', 1723980017025355778, 2, '', 0, 0, 'F', 1, 1, 'sys.oauth.update', '',
        1659939726386827265, '2023-11-13 16:28:16', 1659939726386827265, '2023-11-13 16:28:16');
INSERT INTO `sys_menu`
VALUES (1723981157108486145, '', '删除oauth', 1723980017025355778, 3, '', 0, 0, 'F', 1, 1, 'sys.oauth.delete', '',
        1659939726386827265, '2023-11-13 16:28:33', 1659939726386827265, '2023-11-13 16:28:33');
INSERT INTO `sys_menu`
VALUES (1729846687137067009, '', '创建通知', 1703268748320514050, 1, '', 0, 0, 'F', 1, 1, 'sys:notice:add', '',
        1659939726386827265, '2023-11-29 20:56:04', 1659939726386827265, '2023-11-29 20:56:04');
INSERT INTO `sys_menu`
VALUES (1729846772424044546, '', '通知列表【管理端】', 1703268748320514050, 2, '', 0, 0, 'F', 1, 1, 'sys:notice:list', '',
        1659939726386827265, '2023-11-29 20:56:24', 1659939726386827265, '2023-11-29 20:56:24');
INSERT INTO `sys_menu`
VALUES (1729846867559247873, '', '通知更新【管理】', 1703268748320514050, 3, '', 0, 0, 'F', 1, 1, 'sys:notice:update', '',
        1659939726386827265, '2023-11-29 20:56:47', 1659939726386827265, '2023-11-29 20:56:47');
INSERT INTO `sys_menu`
VALUES (1729848226530144257, '', '删除通知', 1703268748320514050, 4, '', 0, 0, 'F', 1, 1, 'sys:notice:delete', '',
        1659939726386827265, '2023-11-29 21:02:11', 1659939726386827265, '2023-11-29 21:02:11');
INSERT INTO `sys_menu`
VALUES (1731189059301208065, 'x', '服务器状态监控', 0, 9, 'x', 0, 0, 'M', 1, 1, '', 'icon-info', 1659941852221624321,
        '2023-12-03 13:50:10', 1659941852221624321, '2023-12-03 13:50:10');
INSERT INTO `sys_menu`
VALUES (1731189131862667266, '', '文件服务器监控', 1731189059301208065, 1, 'http://192.168.12.12:1212/', 1, 0, 'C', 1,
        1, '', '', 1659941852221624321, '2023-12-03 13:50:28', 1659941852221624321, '2023-12-03 13:50:28');
INSERT INTO `sys_menu`
VALUES (1738844674756120578, 'Notice-List', 'menu.notice-list', 1705549735037480962, 3, '/notice-list', 0, 0, 'C', 1, 1,
        '', '', 1659939726386827265, '2023-12-24 16:50:51', 1659939726386827265, '2023-12-24 16:50:51');
INSERT INTO `sys_menu`
VALUES (1747172616846344194, 'NavIndex', 'menu.nav.index', 0, 88, '/nav/index', 0, 0, 'C', 1, 1, '', '',
        1659941852221624321, '2024-01-16 16:23:07', 1659941852221624321, '2024-01-16 16:23:07');

-- ----------------------------
-- Table structure for sys_oauth
-- ----------------------------
DROP TABLE IF EXISTS `sys_oauth`;
CREATE TABLE `sys_oauth`
(
    `id`                           bigint                                                        NOT NULL,
    `client_id`                    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端id',
    `client_name`                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端name',
    `client_secret`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端秘钥',
    `redirect_uri`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认回调地址，成功回调会自动加上返回参数code',
    `client_image`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网站头像',
    `domain_name`                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务域名',
    `no_sert_redirect`             int                                                           NOT NULL DEFAULT 0 COMMENT '是否允许业务域名或者ip不匹配的回调，默认为1不允许，0为允许',
    `force_configuration_redirect` int                                                           NOT NULL DEFAULT 0 COMMENT '是否强制配置回调，默认为0，不强制，1为强制',
    `grant_type`                   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'code' COMMENT '授权类型：grant_type ，code为authorization_code（授权码模式）',
    `create_time`                  datetime                                                      NOT NULL,
    `update_time`                  datetime                                                      NOT NULL,
    `is_deleted`                   int                                                           NOT NULL DEFAULT 0,
    `status`                       int                                                           NOT NULL DEFAULT 1 COMMENT '默认启用，0为停用',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name`(`client_name` ASC) USING BTREE,
    UNIQUE INDEX `client_id`(`client_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oauth
-- ----------------------------
INSERT INTO `sys_oauth`
VALUES (1, '3a1cab58d4b743d681a042ba20955178', '文件服务器', 'e21a3efafc7641a1a93542d14ffe8359',
        'http://easyoa.fun:81/login', NULL, 'http://easyoa.fun:81', 0, 0, 'code', '2023-11-11 21:34:55',
        '2023-11-11 21:34:57', 0, 1);
INSERT INTO `sys_oauth`
VALUES (2, '8k1DiDP6TW1IeXbcJKZvqjfSFgEhjw1ljbiUAFWt', 'en_ai_auth', 'k7CxjNo014poU6mw9rWZRK7C92xI2JmZibeS303M',
        'http://192.168.12.12:3000/', NULL, 'http://192.168.12.12:3000/', 1, 0, 'code', '2023-11-12 18:53:23',
        '2024-01-18 19:29:05', 0, 1);

-- ----------------------------
-- Table structure for sys_oauth_openid
-- ----------------------------
DROP TABLE IF EXISTS `sys_oauth_openid`;
CREATE TABLE `sys_oauth_openid`
(
    `id`           bigint NOT NULL,
    `sys_oauth_id` bigint NOT NULL,
    `user_id`      bigint NOT NULL,
    `openid`       int    NOT NULL COMMENT '从1递增，在对应的oauth_id下是唯一的',
    PRIMARY KEY (`id`, `sys_oauth_id`, `user_id`, `openid`) USING BTREE,
    UNIQUE INDEX `openid_oauth`(`sys_oauth_id` ASC, `openid` ASC) USING BTREE,
    INDEX          `user_oauth`(`sys_oauth_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oauth_openid
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
) ENGINE = InnoDB AUTO_INCREMENT = 1739706419127619586 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES (1, '系统管理员', 'superadmin', 1, 1, 0, 1659939726386827265, '2023-08-30 15:11:42', 1659939726386827265,
        '2023-08-30 15:11:44');
INSERT INTO `sys_role`
VALUES (2, '超级管理员', 'lsadmin', 2, 1, 0, 1659941852221624321, '2023-09-16 20:38:04', 1659941852221624321,
        '2023-09-16 20:38:07');
INSERT INTO `sys_role`
VALUES (1703241330814615554, '内部管理员', 'roomadmin', 4, 1, 0, 1659941852221624321, '2023-09-17 10:55:53',
        1659941852221624321, '2023-09-17 10:55:53');
INSERT INTO `sys_role`
VALUES (1703241493385838593, '内部用户', 'roomuser', 5, 1, 0, 1659941852221624321, '2023-09-17 10:56:31',
        1659941852221624321, '2023-09-17 10:56:31');
INSERT INTO `sys_role`
VALUES (1703241621119172610, '开放用户', 'other', 10, 1, 0, 1659941852221624321, '2023-09-17 10:57:02',
        1659941852221624321, '2023-09-17 10:57:02');
INSERT INTO `sys_role`
VALUES (1719711889743335425, '娱乐中心游玩', 'yulecentergame', 6, 1, 0, 1659941852221624321, '2023-11-01 21:44:00',
        1659941852221624321, '2023-11-01 21:44:00');
INSERT INTO `sys_role`
VALUES (1719711959280701441, '版本中心', 'versionRead', 14, 1, 0, 1659941852221624321, '2023-11-01 21:44:16',
        1659941852221624321, '2023-11-01 21:44:16');
INSERT INTO `sys_role`
VALUES (1720081630404603906, 'chat', 'chat', 18, 1, 0, 1659939726386827265, '2023-11-02 14:13:13', 1659939726386827265,
        '2023-11-02 14:13:13');
INSERT INTO `sys_role`
VALUES (1720379282501464065, '大三管理组', 'threemanger', 3, 1, 0, 1659941852221624321, '2023-11-03 09:55:59',
        1659941852221624321, '2023-11-03 09:55:59');
INSERT INTO `sys_role`
VALUES (1729023963212869633, '打印', 'printer', 7, 1, 0, 1659939726386827265, '2023-11-27 14:26:51',
        1659939726386827265, '2023-11-27 14:26:51');
INSERT INTO `sys_role`
VALUES (1729845346511679489, '演示模式', 'show', 20, 1, 0, 1659939726386827265, '2023-11-29 20:50:44',
        1659939726386827265, '2023-11-29 20:50:44');
INSERT INTO `sys_role`
VALUES (1739706419127619585, '通知中心查看', 'noticeCenterRead', 15, 1, 0, 1659939726386827265, '2023-12-27 01:55:07',
        1659939726386827265, '2023-12-27 01:55:07');

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
VALUES (1703241838639972353, 1703241493385838593, 1700781419697745922);
INSERT INTO `sys_role_dept`
VALUES (1720377394481655810, 1703241330814615554, 1720377394477461505);
INSERT INTO `sys_role_dept`
VALUES (1720379410389987329, 1720379282501464065, 1720377462601347074);
INSERT INTO `sys_role_dept`
VALUES (1720383795119583234, 1703241330814615554, 1700781578091442177);
INSERT INTO `sys_role_dept`
VALUES (1720383805244633089, 1720379282501464065, 1700781688674267138);
INSERT INTO `sys_role_dept`
VALUES (1720384077605957633, 1703241330814615554, 1700781638367784962);
INSERT INTO `sys_role_dept`
VALUES (1720384093594644481, 1720379282501464065, 1700781715454898177);
INSERT INTO `sys_role_dept`
VALUES (1729024198618181634, 1719711889743335425, 1729023789828730882);
INSERT INTO `sys_role_dept`
VALUES (1729024198622375938, 1703241621119172610, 1729023789828730882);
INSERT INTO `sys_role_dept`
VALUES (1729024198626570241, 1729023963212869633, 1729023789828730882);
INSERT INTO `sys_role_dept`
VALUES (1729024198630764545, 1719711959280701441, 1729023789828730882);
INSERT INTO `sys_role_dept`
VALUES (1729024198634958849, 1720081630404603906, 1729023789828730882);
INSERT INTO `sys_role_dept`
VALUES (1729024214762057730, 1703241493385838593, 1720377175597707265);
INSERT INTO `sys_role_dept`
VALUES (1729024225285566465, 1703241493385838593, 1700781459237449729);
INSERT INTO `sys_role_dept`
VALUES (1730628849192988673, 1703241493385838593, 1730628785854803970);
INSERT INTO `sys_role_dept`
VALUES (1739706456846995457, 1703241621119172610, 1);
INSERT INTO `sys_role_dept`
VALUES (1739706456855384066, 1719711959280701441, 1);
INSERT INTO `sys_role_dept`
VALUES (1739706456859578370, 1720081630404603906, 1);
INSERT INTO `sys_role_dept`
VALUES (1739706456863772674, 1719711889743335425, 1);
INSERT INTO `sys_role_dept`
VALUES (1739706456867966977, 1739706419127619585, 1);

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
INSERT INTO `sys_role_menu`
VALUES (1729845346536845314, 1729845346511679489, 1);
INSERT INTO `sys_role_menu`
VALUES (1729845346541039617, 1729845346511679489, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1729845346545233922, 1729845346511679489, 2);
INSERT INTO `sys_role_menu`
VALUES (1729845346549428225, 1729845346511679489, 3);
INSERT INTO `sys_role_menu`
VALUES (1729845346553622530, 1729845346511679489, 4);
INSERT INTO `sys_role_menu`
VALUES (1729845346557816834, 1729845346511679489, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1729845346562011138, 1729845346511679489, 5);
INSERT INTO `sys_role_menu`
VALUES (1729845346562011139, 1729845346511679489, 6);
INSERT INTO `sys_role_menu`
VALUES (1729845346566205441, 1729845346511679489, 7);
INSERT INTO `sys_role_menu`
VALUES (1729845346570399746, 1729845346511679489, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1729845346574594049, 1729845346511679489, 9);
INSERT INTO `sys_role_menu`
VALUES (1729845346578788354, 1729845346511679489, 10);
INSERT INTO `sys_role_menu`
VALUES (1729845346582982658, 1729845346511679489, 11);
INSERT INTO `sys_role_menu`
VALUES (1729845346587176962, 1729845346511679489, 1702313362839003137);
INSERT INTO `sys_role_menu`
VALUES (1729845346591371265, 1729845346511679489, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1729845346595565570, 1729845346511679489, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1729845346599759874, 1729845346511679489, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1729845346603954178, 1729845346511679489, 1702314062809620482);
INSERT INTO `sys_role_menu`
VALUES (1729845346603954179, 1729845346511679489, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1729845346608148482, 1729845346511679489, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1729845346616537089, 1729845346511679489, 1702314309686353921);
INSERT INTO `sys_role_menu`
VALUES (1729845346620731394, 1729845346511679489, 1702313775076171777);
INSERT INTO `sys_role_menu`
VALUES (1729845346624925697, 1729845346511679489, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1729845346629120002, 1729845346511679489, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1729845346629120003, 1729845346511679489, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1729845346633314306, 1729845346511679489, 1699773810437967874);
INSERT INTO `sys_role_menu`
VALUES (1729845346637508610, 1729845346511679489, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1739705992067780610, 1720379282501464065, 1703249662781853698);
INSERT INTO `sys_role_menu`
VALUES (1739705992071974914, 1720379282501464065, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1739705992076169218, 1720379282501464065, 1);
INSERT INTO `sys_role_menu`
VALUES (1739705992080363521, 1720379282501464065, 2);
INSERT INTO `sys_role_menu`
VALUES (1739705992084557826, 1720379282501464065, 3);
INSERT INTO `sys_role_menu`
VALUES (1739705992088752130, 1720379282501464065, 4);
INSERT INTO `sys_role_menu`
VALUES (1739705992092946434, 1720379282501464065, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1739705992097140738, 1720379282501464065, 5);
INSERT INTO `sys_role_menu`
VALUES (1739705992101335041, 1720379282501464065, 6);
INSERT INTO `sys_role_menu`
VALUES (1739705992105529345, 1720379282501464065, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1739705992109723650, 1720379282501464065, 7);
INSERT INTO `sys_role_menu`
VALUES (1739705992113917953, 1720379282501464065, 9);
INSERT INTO `sys_role_menu`
VALUES (1739705992118112257, 1720379282501464065, 10);
INSERT INTO `sys_role_menu`
VALUES (1739705992118112258, 1720379282501464065, 1729846772424044546);
INSERT INTO `sys_role_menu`
VALUES (1739705992122306562, 1720379282501464065, 1738844674756120578);
INSERT INTO `sys_role_menu`
VALUES (1739705992126500865, 1720379282501464065, 1729848065544368129);
INSERT INTO `sys_role_menu`
VALUES (1739705992130695169, 1720379282501464065, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1739705992134889474, 1720379282501464065, 1702313362839003137);
INSERT INTO `sys_role_menu`
VALUES (1739705992139083777, 1720379282501464065, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1739705992143278082, 1720379282501464065, 1729853369334050817);
INSERT INTO `sys_role_menu`
VALUES (1739705992147472386, 1720379282501464065, 1729846687137067009);
INSERT INTO `sys_role_menu`
VALUES (1739705992147472387, 1720379282501464065, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1739705992151666689, 1720379282501464065, 1702313296111820802);
INSERT INTO `sys_role_menu`
VALUES (1739705992155860993, 1720379282501464065, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1739705992160055297, 1720379282501464065, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1739705992164249602, 1720379282501464065, 1731189131862667266);
INSERT INTO `sys_role_menu`
VALUES (1739705992168443905, 1720379282501464065, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1739705992172638210, 1720379282501464065, 1729848226530144257);
INSERT INTO `sys_role_menu`
VALUES (1739705992176832514, 1720379282501464065, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1739705992181026817, 1720379282501464065, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1739705992185221121, 1720379282501464065, 1702314309686353921);
INSERT INTO `sys_role_menu`
VALUES (1739705992189415426, 1720379282501464065, 1729846928607342593);
INSERT INTO `sys_role_menu`
VALUES (1739705992193609729, 1720379282501464065, 1729853426531774465);
INSERT INTO `sys_role_menu`
VALUES (1739705992197804034, 1720379282501464065, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1739705992201998338, 1720379282501464065, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1739705992206192642, 1720379282501464065, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1739705992210386945, 1720379282501464065, 1731189059301208065);
INSERT INTO `sys_role_menu`
VALUES (1739705992214581249, 1720379282501464065, 1702313111461781506);
INSERT INTO `sys_role_menu`
VALUES (1739705992218775554, 1720379282501464065, 1699773810437967874);
INSERT INTO `sys_role_menu`
VALUES (1739705992218775555, 1720379282501464065, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1739705992222969858, 1720379282501464065, 1702313163114635266);
INSERT INTO `sys_role_menu`
VALUES (1739705992227164161, 1720379282501464065, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1739705992231358465, 1720379282501464065, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1739705992235552769, 1720379282501464065, 1729846867559247873);
INSERT INTO `sys_role_menu`
VALUES (1739706419136008193, 1739706419127619585, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1739706419140202498, 1739706419127619585, 1738844674756120578);
INSERT INTO `sys_role_menu`
VALUES (1739706509917523970, 2, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1739706509921718273, 2, 1);
INSERT INTO `sys_role_menu`
VALUES (1739706509921718274, 2, 2);
INSERT INTO `sys_role_menu`
VALUES (1739706509921718275, 2, 1702313890977374209);
INSERT INTO `sys_role_menu`
VALUES (1739706509925912578, 2, 3);
INSERT INTO `sys_role_menu`
VALUES (1739706509925912579, 2, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1739706509930106881, 2, 4);
INSERT INTO `sys_role_menu`
VALUES (1739706509930106882, 2, 5);
INSERT INTO `sys_role_menu`
VALUES (1739706509930106883, 2, 6);
INSERT INTO `sys_role_menu`
VALUES (1739706509930106884, 2, 7);
INSERT INTO `sys_role_menu`
VALUES (1739706509934301185, 2, 9);
INSERT INTO `sys_role_menu`
VALUES (1739706509934301186, 2, 10);
INSERT INTO `sys_role_menu`
VALUES (1739706509938495489, 2, 11);
INSERT INTO `sys_role_menu`
VALUES (1739706509938495490, 2, 1729848065544368129);
INSERT INTO `sys_role_menu`
VALUES (1739706509938495491, 2, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1739706509942689794, 2, 1699399891600224258);
INSERT INTO `sys_role_menu`
VALUES (1739706509942689795, 2, 1702313362839003137);
INSERT INTO `sys_role_menu`
VALUES (1739706509942689796, 2, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1739706509946884097, 2, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1739706509946884098, 2, 1702314132380540929);
INSERT INTO `sys_role_menu`
VALUES (1739706509951078401, 2, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1739706509951078402, 2, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1739706509951078403, 2, 1702314309686353921);
INSERT INTO `sys_role_menu`
VALUES (1739706509955272706, 2, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1739706509955272707, 2, 1702313955213139969);
INSERT INTO `sys_role_menu`
VALUES (1739706509955272708, 2, 1702313672923897858);
INSERT INTO `sys_role_menu`
VALUES (1739706509959467009, 2, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1739706509963661313, 2, 1702313163114635266);
INSERT INTO `sys_role_menu`
VALUES (1739706509963661314, 2, 1723981157108486145);
INSERT INTO `sys_role_menu`
VALUES (1739706509963661315, 2, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1739706509967855617, 2, 1729846867559247873);
INSERT INTO `sys_role_menu`
VALUES (1739706509967855618, 2, 1703249662781853698);
INSERT INTO `sys_role_menu`
VALUES (1739706509967855619, 2, 1723980908860215298);
INSERT INTO `sys_role_menu`
VALUES (1739706509972049922, 2, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1739706509972049923, 2, 1729846772424044546);
INSERT INTO `sys_role_menu`
VALUES (1739706509972049924, 2, 1738844674756120578);
INSERT INTO `sys_role_menu`
VALUES (1739706509976244225, 2, 1702314174294220801);
INSERT INTO `sys_role_menu`
VALUES (1739706509976244226, 2, 1729853369334050817);
INSERT INTO `sys_role_menu`
VALUES (1739706509976244227, 2, 1729846687137067009);
INSERT INTO `sys_role_menu`
VALUES (1739706509980438530, 2, 1702313296111820802);
INSERT INTO `sys_role_menu`
VALUES (1739706509980438531, 2, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1739706509980438532, 2, 1702313559610580994);
INSERT INTO `sys_role_menu`
VALUES (1739706509984632833, 2, 1702313475783221250);
INSERT INTO `sys_role_menu`
VALUES (1739706509984632834, 2, 1731189131862667266);
INSERT INTO `sys_role_menu`
VALUES (1739706509984632835, 2, 1702314008166227970);
INSERT INTO `sys_role_menu`
VALUES (1739706509988827137, 2, 1729848226530144257);
INSERT INTO `sys_role_menu`
VALUES (1739706509988827138, 2, 1702314062809620482);
INSERT INTO `sys_role_menu`
VALUES (1739706509993021442, 2, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1739706509993021443, 2, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1739706509993021444, 2, 1723981087562731521);
INSERT INTO `sys_role_menu`
VALUES (1739706509997215745, 2, 1729853426531774465);
INSERT INTO `sys_role_menu`
VALUES (1739706509997215746, 2, 1729846928607342593);
INSERT INTO `sys_role_menu`
VALUES (1739706509997215747, 2, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1739706510001410049, 2, 1702313775076171777);
INSERT INTO `sys_role_menu`
VALUES (1739706510001410050, 2, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1739706510001410051, 2, 1702314229730336769);
INSERT INTO `sys_role_menu`
VALUES (1739706510005604354, 2, 1731189059301208065);
INSERT INTO `sys_role_menu`
VALUES (1739706510005604355, 2, 1723980017025355778);
INSERT INTO `sys_role_menu`
VALUES (1739706510005604356, 2, 1702313111461781506);
INSERT INTO `sys_role_menu`
VALUES (1739706510009798658, 2, 1699773810437967874);
INSERT INTO `sys_role_menu`
VALUES (1739706510009798659, 2, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1739706565211033602, 1703241330814615554, 1);
INSERT INTO `sys_role_menu`
VALUES (1739706565215227905, 1703241330814615554, 2);
INSERT INTO `sys_role_menu`
VALUES (1739706565219422209, 1703241330814615554, 3);
INSERT INTO `sys_role_menu`
VALUES (1739706565223616514, 1703241330814615554, 4);
INSERT INTO `sys_role_menu`
VALUES (1739706565223616515, 1703241330814615554, 5);
INSERT INTO `sys_role_menu`
VALUES (1739706565227810818, 1703241330814615554, 6);
INSERT INTO `sys_role_menu`
VALUES (1739706565232005121, 1703241330814615554, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1739706565236199426, 1703241330814615554, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1739706565240393729, 1703241330814615554, 1729848226530144257);
INSERT INTO `sys_role_menu`
VALUES (1739706565240393730, 1703241330814615554, 9);
INSERT INTO `sys_role_menu`
VALUES (1739706565244588033, 1703241330814615554, 1729846772424044546);
INSERT INTO `sys_role_menu`
VALUES (1739706565248782338, 1703241330814615554, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1739706565252976642, 1703241330814615554, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1739706565252976643, 1703241330814615554, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1739706565269753858, 1703241330814615554, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1739706565273948161, 1703241330814615554, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1739706565273948162, 1703241330814615554, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1739706565278142465, 1703241330814615554, 1729846687137067009);
INSERT INTO `sys_role_menu`
VALUES (1739706565282336770, 1703241330814615554, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1739706565286531074, 1703241330814615554, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1739706565290725377, 1703241330814615554, 1702312893739655169);
INSERT INTO `sys_role_menu`
VALUES (1739706565294919681, 1703241330814615554, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1739706565294919682, 1703241330814615554, 1729846867559247873);
INSERT INTO `sys_role_menu`
VALUES (1747173782145630209, 1, 1);
INSERT INTO `sys_role_menu`
VALUES (1747173782145630210, 1, 1709863803420348417);
INSERT INTO `sys_role_menu`
VALUES (1747173782154018818, 1, 2);
INSERT INTO `sys_role_menu`
VALUES (1747173782154018819, 1, 3);
INSERT INTO `sys_role_menu`
VALUES (1747173782162407425, 1, 1702313890977374209);
INSERT INTO `sys_role_menu`
VALUES (1747173782162407426, 1, 4);
INSERT INTO `sys_role_menu`
VALUES (1747173782162407427, 1, 1709914681489690626);
INSERT INTO `sys_role_menu`
VALUES (1747173782170796033, 1, 5);
INSERT INTO `sys_role_menu`
VALUES (1747173782170796034, 1, 6);
INSERT INTO `sys_role_menu`
VALUES (1747173782170796035, 1, 7);
INSERT INTO `sys_role_menu`
VALUES (1747173782179184641, 1, 9);
INSERT INTO `sys_role_menu`
VALUES (1747173782179184642, 1, 10);
INSERT INTO `sys_role_menu`
VALUES (1747173782179184643, 1, 11);
INSERT INTO `sys_role_menu`
VALUES (1747173782187573250, 1, 1702313362839003137);
INSERT INTO `sys_role_menu`
VALUES (1747173782187573251, 1, 1699399891600224258);
INSERT INTO `sys_role_menu`
VALUES (1747173782187573252, 1, 1703379381351276546);
INSERT INTO `sys_role_menu`
VALUES (1747173782187573253, 1, 1703328073911078914);
INSERT INTO `sys_role_menu`
VALUES (1747173782195961857, 1, 1702314132380540929);
INSERT INTO `sys_role_menu`
VALUES (1747173782195961858, 1, 1703379432723111938);
INSERT INTO `sys_role_menu`
VALUES (1747173782195961859, 1, 1719696557347688449);
INSERT INTO `sys_role_menu`
VALUES (1747173782204350466, 1, 1705563149847699458);
INSERT INTO `sys_role_menu`
VALUES (1747173782204350467, 1, 1702314309686353921);
INSERT INTO `sys_role_menu`
VALUES (1747173782204350468, 1, 1698681230501560322);
INSERT INTO `sys_role_menu`
VALUES (1747173782204350469, 1, 1702313955213139969);
INSERT INTO `sys_role_menu`
VALUES (1747173782212739073, 1, 1702313672923897858);
INSERT INTO `sys_role_menu`
VALUES (1747173782212739074, 1, 1703379296823468033);
INSERT INTO `sys_role_menu`
VALUES (1747173782212739075, 1, 1702313163114635266);
INSERT INTO `sys_role_menu`
VALUES (1747173782221127682, 1, 1723981157108486145);
INSERT INTO `sys_role_menu`
VALUES (1747173782221127683, 1, 1719696699043860482);
INSERT INTO `sys_role_menu`
VALUES (1747173782221127684, 1, 1729846867559247873);
INSERT INTO `sys_role_menu`
VALUES (1747173782229516290, 1, 1703249662781853698);
INSERT INTO `sys_role_menu`
VALUES (1747173782229516291, 1, 1723980908860215298);
INSERT INTO `sys_role_menu`
VALUES (1747173782229516292, 1, 1703268642263343105);
INSERT INTO `sys_role_menu`
VALUES (1747173782229516293, 1, 1729846772424044546);
INSERT INTO `sys_role_menu`
VALUES (1747173782233710594, 1, 1702314174294220801);
INSERT INTO `sys_role_menu`
VALUES (1747173782233710595, 1, 1729846687137067009);
INSERT INTO `sys_role_menu`
VALUES (1747173782233710596, 1, 1702313296111820802);
INSERT INTO `sys_role_menu`
VALUES (1747173782242099201, 1, 1702313016154611713);
INSERT INTO `sys_role_menu`
VALUES (1747173782242099202, 1, 1702313559610580994);
INSERT INTO `sys_role_menu`
VALUES (1747173782242099203, 1, 1702313475783221250);
INSERT INTO `sys_role_menu`
VALUES (1747173782250487809, 1, 1731189131862667266);
INSERT INTO `sys_role_menu`
VALUES (1747173782250487810, 1, 1702314008166227970);
INSERT INTO `sys_role_menu`
VALUES (1747173782258876417, 1, 1702314062809620482);
INSERT INTO `sys_role_menu`
VALUES (1747173782258876418, 1, 1703379497562857474);
INSERT INTO `sys_role_menu`
VALUES (1747173782258876419, 1, 1747172616846344194);
INSERT INTO `sys_role_menu`
VALUES (1747173782267265025, 1, 1723981087562731521);
INSERT INTO `sys_role_menu`
VALUES (1747173782267265026, 1, 1703268748320514050);
INSERT INTO `sys_role_menu`
VALUES (1747173782267265027, 1, 1729846928607342593);
INSERT INTO `sys_role_menu`
VALUES (1747173782275653634, 1, 1702313775076171777);
INSERT INTO `sys_role_menu`
VALUES (1747173782275653635, 1, 1709863993443291137);
INSERT INTO `sys_role_menu`
VALUES (1747173782275653636, 1, 1702314229730336769);
INSERT INTO `sys_role_menu`
VALUES (1747173782284042241, 1, 1705549735037480962);
INSERT INTO `sys_role_menu`
VALUES (1747173782284042242, 1, 1731189059301208065);
INSERT INTO `sys_role_menu`
VALUES (1747173782284042243, 1, 1723980017025355778);
INSERT INTO `sys_role_menu`
VALUES (1747173782292430850, 1, 1702313111461781506);
INSERT INTO `sys_role_menu`
VALUES (1747173782292430851, 1, 1699773810437967874);
INSERT INTO `sys_role_menu`
VALUES (1747173782292430852, 1, 1702312893739655169);

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
CREATE TABLE `tr_login`
(
    `id`         bigint                                                        NOT NULL,
    `tr_id`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方id/openid',
    `user_id`    bigint NULL DEFAULT NULL COMMENT '本系统userId',
    `status`     int                                                           NOT NULL COMMENT '是否完成注册或者绑定已有帐号1,完成',
    `is_deleted` int                                                           NOT NULL DEFAULT 0 COMMENT '默认未删除',
    `type`       varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'qq,wx,......',
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
    `id`            bigint                                                       NOT NULL COMMENT '用户id',
    `name`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
    `username`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
    `email`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绑定邮箱',
    `password`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    `salt`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐',
    `sex`           varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '性别',
    `avatar`        mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '头像',
    `student_id`    varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
    `dept_id`       bigint                                                       NOT NULL DEFAULT 0 COMMENT '属于哪个部门',
    `status`        int                                                          NOT NULL COMMENT '状态，1为正常，0为封号',
    `phone`         varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
    `login_ip`      json NULL COMMENT '最后登录IP',
    `login_date`    datetime(3) NULL DEFAULT NULL COMMENT '最后登录时间',
    `create_user`   bigint NULL DEFAULT NULL,
    `active_status` int NULL DEFAULT 2 COMMENT '1在线或者离线0',
    `create_time`   datetime                                                     NOT NULL COMMENT '注册时间',
    `update_time`   datetime                                                     NOT NULL COMMENT '更新时间',
    `is_deleted`    int                                                          NOT NULL COMMENT '逻辑删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX           `dept_id`(`dept_id` ASC) USING BTREE,
    CONSTRAINT `dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user`
VALUES (10010, 'chatgpt', 'chatgpt', 'chatgpt@aien.com', NULL, NULL, '男',
        'http://easyoa.fun:9090/aistudio/18e956a8440341e3945bfb405953a396.png', 'null', 1730628451526832129, 1,
        '13900000000', '{
    \"createIp\": \"0:0:0:0:0:0:0:1\", \"updateIp\": \"0:0:0:0:0:0:0:1\", \"createIpDetail\": null, \"updateIpDetail\": null}',
        '2023-09-28 21:16:01.000', NULL, 1, '2023-09-28 21:10:07', '2023-11-06 21:06:15', 0);
INSERT INTO `user`
VALUES (10011, '系统通知', 'systemmessage', 'systemmessage@aien.com', 'b30dc295eabc8c9821b70d3830c227d7',
        'BzxrDd.SuMAxIBM7', '男', 'http://easyoa.fun:9090/aistudio/18e956a8440341e3945bfb405953a396.png',
        '202115040212', 1730628451526832129, 1, '13986530157', '{
    \"createIp\": \"192.168.12.254\", \"updateIp\": \"192.168.12.254\", \"createIpDetail\": null, \"updateIpDetail\": null}',
        '2023-11-28 15:28:58.612', NULL, 1, '2023-09-28 21:10:07', '2023-11-28 15:28:59', 0);
INSERT INTO `user`
VALUES (1659939726386827265, 'admin', 'admin', 'qc2003020402@163.com', 'b30dc295eabc8c9821b70d3830c227d7',
        'BzxrDd.SuMAxIBM7', '男', 'http://easyoa.fun:9090/aistudio/18e956a8440341e3945bfb405953a396.png',
        '202115040212', 1, 1, '13986530157', '{
    \"createIp\": \"0:0:0:0:0:0:0:1\", \"updateIp\": \"0:0:0:0:0:0:0:1\", \"createIpDetail\": null, \"updateIpDetail\": null}',
        '2024-01-19 14:54:49.653', NULL, 2, '2023-05-20 23:10:46', '2024-01-19 14:54:50', 0);

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
INSERT INTO `user_emoji`
VALUES (1, 1659941852221624321,
        'http://easyoa.fun:9090/aistudio//emoji/2023-11/1659941852221624321/0a54767e-590c-43e2-aed6-7b6884b74178.png',
        0, '2023-11-24 08:44:36.101', '2023-11-24 08:44:36.101');

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
