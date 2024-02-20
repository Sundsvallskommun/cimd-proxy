package se.sundsvall.cimdproxy.cimd;

import java.util.List;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.PacketSerializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * CIMD packet decoder.
 */
public class CIMDPacketDecoder extends ByteToMessageDecoder {

    private enum Token {
        STX((byte) 2),
        ETX((byte) 3);

        private final byte val;

        Token(final byte b) {
            val = b;
        }
    }

    private final boolean useChecksum;

    public CIMDPacketDecoder(final boolean useChecksum) {
        this.useChecksum = useChecksum;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        var packet = doDecode(in);

        if (packet != null) {
            out.add(packet);
        }
    }

    protected Packet doDecode(final ByteBuf in) throws Exception {
        var start = in.readerIndex();

        while (in.readableBytes() > 0) {
            in.markReaderIndex();
            var c = in.readByte();
            if (c == Token.ETX.val) {
                var pos = in.readerIndex();

                try {
                    return PacketSerializer.deserializePacket(new ByteBufInputStream(in.slice(start, pos)), useChecksum);
                } finally {
                    in.readerIndex(pos);
                }
            }
        }

        in.readerIndex(start);

        return null;
    }
}
