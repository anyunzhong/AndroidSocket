package net.datafans.androidsocket.handler.common.handler;

import android.util.Log;

import net.datafans.androidsocket.handler.common.constant.Protocal;
import net.datafans.androidsocket.handler.common.entity.DataPackage;
import net.datafans.netty.common.constant.Tag;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.logging.Logger;

public class DataPackageDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {

		if (buf.readableBytes() < Protocal.HEADER_LENGTH) {
			Log.d(Tag.NETTY_CLIENT, "READABLE_BYTES_ERROR " + buf);
			return;
		}
		
		DataPackage pkg = new DataPackage();
		fillPackage(pkg, buf);
		out.add(pkg);

	}

	private void fillPackage(final DataPackage pkg, final ByteBuf buf) {

		// 头部开始
		// 数据总长度
		pkg.setSize(buf.readInt());
		// 版本
		byte[] version = new byte[Protocal.FIELD_VERSION_LENGTH];
		buf.readBytes(version);
		pkg.setVersion(version);
		
		// 消息id
		pkg.setId(buf.readInt());

		// 消息类型
		byte[] type = new byte[Protocal.FIELD_TYPE_LENGTH];
		buf.readBytes(type);
		pkg.setType(type);

		// 通用字段
		byte[] common = new byte[Protocal.FIELD_COMMON_LENGTH];
		buf.readBytes(common);
		pkg.setCommon(common);
		// 头部结束

		// body内容
		byte[] content = new byte[pkg.getSize() - Protocal.HEADER_LENGTH];
		buf.readBytes(content);
		pkg.setContent(content);

	}

	
}
