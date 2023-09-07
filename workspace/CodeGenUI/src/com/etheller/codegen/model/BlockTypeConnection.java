package com.etheller.codegen.model;

public class BlockTypeConnection {
	private final String name;
	private final ConnectionType connectionType;

	public BlockTypeConnection(String name, ConnectionType connectionType) {
		this.name = name;
		this.connectionType = connectionType;
	}

	public String getName() {
		return name;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}
}
