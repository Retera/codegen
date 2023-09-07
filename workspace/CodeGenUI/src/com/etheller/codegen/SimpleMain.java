package com.etheller.codegen;

public class SimpleMain {
	public static String notUseful(String x) {
		return x + "";
	}

	public static void main(String[] args) {
		var x = "Hello";
		var y = notUseful("Hello");
		System.out.println(x.equals(y));
	}
}
