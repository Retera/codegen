package com.etheller.codegen.model;

import java.awt.Color;

public enum ConnectionType {
	CONTROL_FLOW(Color.WHITE), INT(Color.RED), STRING(Color.MAGENTA), BOOLEAN(Color.GREEN);

	private Color color;

	private ConnectionType(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}
}
