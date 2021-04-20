package org.epilogtool.gui.dialog;

import org.epilogtool.common.Txt;

public enum EnumStatePercent {
	
	YES(Txt.get("s_ENUM_STATEPERCENT_Y")), 
	NO(Txt.get("s_ENUM_STATEPERCENT_N"));
	private String desc;

	private EnumStatePercent(String desc) {
		this.desc = desc;
	}

	public static String title() {
		return Txt.get("s_ENUM_STATEPERCENT_TITLE");
	}

	public String toString() {
		return this.desc;
	}

	public static EnumStatePercent fromString(String str) {
		if (str.equals(YES.toString()))
			return YES;
		else if (str.equals(NO.toString()))
			return NO;
		return null;
	}
}
