package com.etheller.codegen.model;

import java.util.List;

public class BlockType {
	private final String name;
	private final List<BlockTypeConnection> inwardConnections;
	private final List<BlockTypeConnection> outwardConnections;

	public BlockType(String name, List<BlockTypeConnection> inwardConnections, List<BlockTypeConnection> outwardConnections) {
		this.name = name;
		this.inwardConnections = inwardConnections;
		this.outwardConnections = outwardConnections;
	}

	public String getName() {
		return name;
	}

	public List<BlockTypeConnection> getInwardConnections() {
		return inwardConnections;
	}

	public List<BlockTypeConnection> getOutwardConnections() {
		return outwardConnections;
	}
}
