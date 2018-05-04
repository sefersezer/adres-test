package com.paritus.addresstest;
 
public class InputToken {

	private String tokenKeyword;
	private String tokenValue;
	private int group;
	
	public InputToken() {
 
	}

	public String getTokenValue() {
//		if (tokenValue == null)
//			return "";
		return tokenValue;
	}

	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}

	public String getTokenKeyword() {
		return tokenKeyword;
	}

	public void setTokenKeyword(String tokenKeyword) {
		this.tokenKeyword = tokenKeyword;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public String[] getTokenKeywordParts() {
		return tokenKeyword.split("\\.");
	}
}
