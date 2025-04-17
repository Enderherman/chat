package top.enderherman.easychat.webSocket;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

@Component
public class ChannelContextUtils {

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();

        AttributeKey attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }

        channel.attr(attributeKey).set(userId);
    }
}
