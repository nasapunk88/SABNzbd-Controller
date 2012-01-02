package com.gmail.at.faint545;

import java.text.DecimalFormat;

public class StringUtils {
	private static DecimalFormat df;
	
	/*
	 * Perform some unit normalization. This function is used to 
	 * convert a unit (i.e: megabytes) up, down, or neither depending
	 * on the size.
	 */
	public static String normalizeSize(double size, String unit) {
		df = new DecimalFormat("#0.00");
		
		if(size < 1)
			return stepDown(size,unit);
		else if(size > 1024)
			return stepUp(size,unit);
		else 
			return String.valueOf(size);
	}

	private static String stepUp(double convertSize, String unit) {
		convertSize /= 1024;
		StringBuilder normalizedSize = new StringBuilder();		
		if(unit.equals("bytes"))
			normalizedSize.append(df.format(convertSize)).append("KB");
		else if(unit.equals("k"))
			normalizedSize.append(df.format(convertSize)).append("MB");
		else if(unit.equals("m"))
			normalizedSize.append(df.format(convertSize)).append("GB");
		else if(unit.equals("g"))
			normalizedSize.append(df.format(convertSize)).append("TB");
		else
			normalizedSize.append(df.format(convertSize*1024));
		
		return normalizedSize.toString();
	}

	private static String stepDown(double convertSize, String unit) {
		convertSize *= 1024;
		StringBuilder normalizedSize = new StringBuilder();
		if(unit.equals("k"))
			normalizedSize.append(df.format(convertSize)).append("Bytes");
		else if(unit.equals("m"))
			normalizedSize.append(df.format(convertSize)).append("KB");
		else if(unit.equals("g"))
			normalizedSize.append(df.format(convertSize)).append("MB");
		else if(unit.equals("t"))
			normalizedSize.append(df.format(convertSize)).append("GB");
		else
			normalizedSize.append(df.format(convertSize/1024));
		
		return normalizedSize.toString();
	}
}
