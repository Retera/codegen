package com.etheller.codegen.model;

public class Block {
	private final BlockType blockType;
	private final BlockConnection[] inwardConnections;
	private final BlockConnection[] outwardConnections;

	public Block(BlockType blockType) {
		this.blockType = blockType;
		inwardConnections = new BlockConnection[blockType.getInwardConnections().size()];
		outwardConnections = new BlockConnection[blockType.getOutwardConnections().size()];
	}

	public BlockType getBlockType() {
		return blockType;
	}

	public BlockConnection[] getOutwardConnections() {
		return outwardConnections;
	}

	public BlockConnection[] getInwardConnections() {
		return inwardConnections;
	}
}
