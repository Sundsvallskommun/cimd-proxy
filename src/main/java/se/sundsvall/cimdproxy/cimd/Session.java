package se.sundsvall.cimdproxy.cimd;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.util.AttributeKey;

public class Session {

    private final Channel channel;

    public Session(final Channel channel) {
        this.channel = channel;
    }

    public ChannelFuture send(final Object msg) {
        return channel.writeAndFlush(msg);
    }

    public <T> void setAttribute(final String name, final T value) {
        var sessionIdKey = AttributeKey.valueOf(name);

        channel.attr(sessionIdKey).set(value);
    }

    public <T> T getAttribute(final String name) {
        AttributeKey<T> sessionIdKey = AttributeKey.valueOf(name);

        return channel.attr(sessionIdKey).get();
    }

    public ChannelId getId() {
        return channel.id();
    }

    public ChannelFuture close() {
        return channel.close();
    }

    @Override
    public String toString() {
        return getId().toString();
    }
}
