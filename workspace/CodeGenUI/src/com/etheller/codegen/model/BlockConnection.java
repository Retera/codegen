package com.etheller.codegen.model;

public class BlockConnection {
	private final Block target;
	private final int connectionIndex;

	public BlockConnection(Block target, int connectionIndex) {
		this.target = target;
		this.connectionIndex = connectionIndex;
	}

	public Block getTarget() {
		return target;
	}

	public int getConnectionIndex() {
		return connectionIndex;
	}
}
