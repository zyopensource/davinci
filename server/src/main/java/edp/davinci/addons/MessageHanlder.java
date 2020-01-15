package edp.davinci.addons;

import com.zyframework.core.mq.MQMessage;
import com.zyframework.core.mq.MQTopicRef;
import com.zyframework.core.mq.MessageHandler;
import edp.davinci.model.PassportAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@MQTopicRef("ucin-passport-access")
@Component
@Slf4j
public class MessageHanlder implements MessageHandler {

    @Override
    public void process(MQMessage message) {
        List<PassportAccess> passportAccessList = message.toObject(ArrayList.class);

        log.info("消息处理完成,收到的内容:{}",message.toJSON());
    }
}

