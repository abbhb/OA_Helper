package com.qc.printers.common.chatai.handler;

import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.msg.MessageExtra;
import com.qc.printers.common.chatai.domain.ChatGPTContext;
import com.qc.printers.common.chatai.domain.ChatGPTMsg;
import com.qc.printers.common.chatai.domain.builder.ChatGPTContextBuilder;
import com.qc.printers.common.chatai.domain.builder.ChatGPTMsgBuilder;
import com.qc.printers.common.chatai.properties.ChatGPTProperties;
import com.qc.printers.common.chatai.utils.ChatGPTUtils;
import com.qc.printers.common.common.constant.RedisKey;
import com.qc.printers.common.common.domain.dto.FrequencyControlDTO;
import com.qc.printers.common.common.exception.FrequencyControlException;
import com.qc.printers.common.common.service.frequencycontrol.FrequencyControlUtil;
import com.qc.printers.common.common.utils.DateUtils;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.user.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qc.printers.common.common.constant.RedisKey.USER_CHAT_CONTEXT;
import static com.qc.printers.common.common.service.frequencycontrol.FrequencyControlStrategyFactory.TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;

@Slf4j
@Component
public class GPTChatAIHandler extends AbstractChatAIHandler {
    /**
     * GPTChatAIHandler限流前缀
     */
    private static final String CHAT_FREQUENCY_PREFIX = "GPTChatAIHandler";
    private static String AI_NAME;
    @Autowired
    private ChatGPTProperties chatGPTProperties;

    @Override
    protected void init() {
        super.init();
        if (isUse()) {
            User userInfo = userDao.getById(chatGPTProperties.getAIUserId());
            if (userInfo == null) {
                log.error("根据AIUserId:{} 找不到用户信息", chatGPTProperties.getAIUserId());
                throw new RuntimeException("根据AIUserId: " + chatGPTProperties.getAIUserId() + " 找不到用户信息");
            }
            if (StringUtils.isBlank(userInfo.getName())) {
                log.warn("根据AIUserId:{} 找到的用户信息没有name", chatGPTProperties.getAIUserId());
                throw new RuntimeException("根据AIUserId: " + chatGPTProperties.getAIUserId() + " 找到的用户没有名字");
            }
            AI_NAME = userInfo.getName();
        }
    }

    @Override
    protected boolean isUse() {
        return chatGPTProperties.isUse();
    }

    @Override
    public Long getChatAIUserId() {
        return chatGPTProperties.getAIUserId();
    }

    @Override
    protected String doChat(Message message) {
        Long uid = message.getFromUid();
        try {
            FrequencyControlDTO frequencyControlDTO = new FrequencyControlDTO();
            frequencyControlDTO.setKey(RedisKey.getKey(CHAT_FREQUENCY_PREFIX) + ":" + uid);
            frequencyControlDTO.setUnit(TimeUnit.HOURS);
            frequencyControlDTO.setCount(chatGPTProperties.getLimit());
            frequencyControlDTO.setTime(1);
            return FrequencyControlUtil.executeWithFrequencyControl(TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER,
                    frequencyControlDTO, // 限流参数
                    () -> sendRequestToGPT(message));
        } catch (FrequencyControlException e) {
            return "亲爱的,你今天找我聊了" + chatGPTProperties.getLimit() + "次了~人家累了~明天见";
        } catch (Throwable e) {
            return "系统开小差啦~~";
        }
    }


    private String sendRequestToGPT(Message message) {
        ChatGPTContext context = buildContext(message);// 构建上下文
        context = tailorContext(context);// 裁剪上下文
        log.info("context = {}", context);
        String text;
        try {
            Response response = ChatGPTUtils.create(chatGPTProperties.getKey())
                    .proxyUrl(chatGPTProperties.getProxyUrl())
                    .model(chatGPTProperties.getModelName())
                    .timeout(chatGPTProperties.getTimeout())
                    .maxTokens(chatGPTProperties.getMaxTokens())
                    .message(context.getMsg())
                    .send();
            log.info("gpt-{}",response);
            text = ChatGPTUtils.parseText(response);
            ChatGPTMsg chatGPTMsg = ChatGPTMsgBuilder.assistantMsg(text);
            context.addMsg(chatGPTMsg);
            saveContext(context);
        } catch (Exception e) {
            log.warn("gpt doChat warn:", e);
            text = "我累了，明天再聊吧";
        }
        return text;
    }

    private ChatGPTContext tailorContext(ChatGPTContext context) {
        List<ChatGPTMsg> msg = context.getMsg();
        Integer integer = ChatGPTUtils.countTokens(msg);
        if (integer < (chatGPTProperties.getMaxTokens() - 500)) { // 用户的输入+ChatGPT的回答内容都会计算token 留500个token给ChatGPT回答
            return context;
        }
        msg.remove(1);
        return tailorContext(context);
    }

    private ChatGPTContext buildContext(Message message) {
        String prompt = message.getContent().replace("@" + AI_NAME, "").trim();
        Long uid = message.getFromUid();
        Long roomId = message.getRoomId();
        ChatGPTContext chatGPTContext = RedisUtils.get(RedisKey.getKey(USER_CHAT_CONTEXT, uid, roomId), ChatGPTContext.class);
        if (chatGPTContext == null) {
            chatGPTContext = ChatGPTContextBuilder.initContext(uid, roomId);
        }
        saveContext(chatGPTContext);
        chatGPTContext.addMsg(ChatGPTMsgBuilder.userMsg(prompt));
        return chatGPTContext;
    }

    private void saveContext(ChatGPTContext chatGPTContext) {
        // 12个小时的上下文
        RedisUtils.set(RedisKey.getKey(USER_CHAT_CONTEXT, chatGPTContext.getUid(), chatGPTContext.getRoomId()), chatGPTContext, 12L, TimeUnit.HOURS);
    }


    private Long userChatNumInrc(Long uid) {
        return RedisUtils.inc(RedisKey.getKey(RedisKey.USER_CHAT_NUM, uid), DateUtils.getEndTimeByToday().intValue(), TimeUnit.MILLISECONDS);
    }

    private Long getUserChatNum(Long uid) {
        Long num = RedisUtils.get(RedisKey.getKey(RedisKey.USER_CHAT_NUM, uid), Long.class);
        return num == null ? 0 : num;

    }


    @Override
    protected boolean supports(Message message) {
        if (!chatGPTProperties.isUse()) {
            return false;
        }
        /* 前端传@信息后取消注释 */

        MessageExtra extra = message.getExtra();
        if (extra == null) {
            return false;
        }

        if (StringUtils.isBlank(message.getContent())) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean menu(Message message) {
        if (message.getContent().equals("清除上下文")){
            // 清除redis里的上下文
            RedisUtils.del(RedisKey.getKey(USER_CHAT_CONTEXT, message.getFromUid(), message.getRoomId()));
            answerMsg("清除上下文成功~", message);
            return true;
        }
        if (message.getContent().equals("查看当前上下文条数")){
            // 返回redis当前上下文条数
            ChatGPTContext chatGPTContext = RedisUtils.get(RedisKey.getKey(USER_CHAT_CONTEXT,  message.getFromUid(), message.getRoomId()), ChatGPTContext.class);
            answerMsg("上下文从最后一条（不所有的命令和命令回复）开始有"+chatGPTContext.getMsg().size()+"条~", message);
            return true;
        }
        Integer integer = checkAndExtractContext(message.getContent());
        if (integer!=null){
            // 仅保留后n条上下文 integer
            ChatGPTContext chatGPTContext = RedisUtils.get(RedisKey.getKey(USER_CHAT_CONTEXT,  message.getFromUid(), message.getRoomId()), ChatGPTContext.class);
            List<ChatGPTMsg> msg = chatGPTContext.getMsg();
            if (msg.size()<=integer){
                RedisUtils.del(RedisKey.getKey(USER_CHAT_CONTEXT, message.getFromUid(), message.getRoomId()));
                answerMsg("清除全部上下文成功【保留的比总数都少】", message);
                return true;
            }
            // 方法三：使用流操作保留后n条数据
            List<ChatGPTMsg> lastNMsg = msg.stream()
                    .skip(Math.max(0, msg.size() - integer))
                    .collect(Collectors.toList());
            chatGPTContext.setMsg(lastNMsg);
            saveContext(chatGPTContext);
            // 减一条
            answerMsg("仅保留部分上下文成功", message);

            return true;
        }
        return false;
    }
    private Integer checkAndExtractContext(String input) {
        // 定义正则表达式，匹配格式 "仅保留后x条上下文"
        String pattern = "仅保留后(\\d+)条上下文";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);

        // 检查是否匹配
        if (m.matches()) {
            // 提取匹配的数字
            String numberStr = m.group(1);
            int x = Integer.parseInt(numberStr);
            return x;
        } else {
            return null;
        }
    }
}
