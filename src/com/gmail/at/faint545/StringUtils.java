package com.gmail.at.faint545;
public class StringUtils {
	public static String normalizeSize(String size, String unit) {
		double convertSize = Double.parseDouble(size);
		
		if(convertSize < 1)
			return stepDown(convertSize,unit);
		else if(convertSize > 1024)
			return stepUp(convertSize,unit);
		else 
			return size;
	}

	private static String stepUp(double convertSize, String unit) {
		convertSize /= 1024;
		StringBuilder normalizedSize = new StringBuilder();
		
		if(unit.equals("bytes"))
			normalizedSize.append(convertSize).append("KB");
		else if(unit.equals("k"))
			normalizedSize.append(convertSize).append("MB");
		else if(unit.equals("m"))
			normalizedSize.append(convertSize).append("GB");
		else if(unit.equals("g"))
			normalizedSize.append(convertSize).append("TB");
		else
			normalizedSize.append(convertSize*1024);
		
		return normalizedSize.toString();
	}

	private static String stepDown(double convertSize, String unit) {
		convertSize *= 1024;
		StringBuilder normalizedSize = new StringBuilder();
		if(unit.equals("k"))
			normalizedSize.append(convertSize).append("Bytes");
		else if(unit.equals("m"))
			normalizedSize.append(convertSize).append("KB");
		else if(unit.equals("g"))
			normalizedSize.append(convertSize).append("MB");
		else if(unit.equals("t"))
			normalizedSize.append(convertSize).append("GB");
		else
			normalizedSize.append(convertSize/1024);
		
		return normalizedSize.toString();
	}
}
