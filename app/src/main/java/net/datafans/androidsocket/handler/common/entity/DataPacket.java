package net.datafans.androidsocket.handler.common.entity;

import net.datafans.androidsocket.handler.common.constant.BizType;
import net.datafans.androidsocket.handler.common.constant.Version;

import java.io.Serializable;
import java.util.Arrays;


public class DataPacket implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final static DataPacket HEARTBEAT_PACKET = new DataPacket(Version.V1, BizType.HEARTBEAT);
	public final static DataPacket LOGIN_PACKET = new DataPacket(Version.V1, BizType.LOGIN);
	
	
	private int size;
	private byte[] version;
	private int id;
	private byte[] type;
	private byte[] common;
	private byte[] content;

	public DataPacket() {

	}

	public DataPacket(byte[] version, byte[] type) {
		super();
		this.version = version;
		this.type = type;
	}
	
	public DataPacket(byte[] version, byte[] type, int msgId) {
		super();
		this.version = version;
		this.type = type;
	}

	public DataPacket(byte[] version, byte[] type, byte[] content) {
		super();
		this.version = version;
		this.type = type;
		this.content = content;
	}

	public byte[] getVersion() {
		return version;
	}

	public void setVersion(byte[] version) {
		this.version = version;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte[] getType() {
		return type;
	}

	public void setType(byte[] type) {
		this.type = type;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public byte[] getCommon() {
		return common;
	}

	public void setCommon(byte[] common) {
		this.common = common;
	}

	@Override
	public String toString() {
		return "DataPacket [size=" + size + ", version=" + version + ", id=" + id + ", type=" + type + ", common="
				+ Arrays.toString(common) + ", content=" + Arrays.toString(content) + "]";
	}

}
