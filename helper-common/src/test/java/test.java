import com.fasterxml.jackson.core.JsonProcessingException;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageResp;
import com.qc.printers.common.common.JacksonObjectMapper;
import com.qc.printers.common.signin.domain.entity.SigninWay;
import com.qc.printers.common.websocket.domain.enums.WSBaseResp;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class test {
    public static void main(String args[]) throws JsonProcessingException {
//        HashMap<String,String> s= new HashMap<>();
//        LocalDate currentDate = LocalDate.now();
//        String k = String.valueOf(currentDate.getDayOfWeek());
//        int value = currentDate.getDayOfWeek().getValue();
//        System.out.println(value);
//        JacksonObjectMapper mapper = new JacksonObjectMapper();
//        WSBaseResp<ChatMessageResp> wsBaseResp = new WSBaseResp<>();
//        wsBaseResp.setType(4);
//        ChatMessageResp chatMessageResp = new ChatMessageResp();
//        ChatMessageResp.Message message = new ChatMessageResp.Message();
//        message.setSendTime(new Date());
//        chatMessageResp.setMessage(message);
//        wsBaseResp.setData(chatMessageResp);
//        System.out.println(mapper.writeValueAsString(wsBaseResp));
    }
}
