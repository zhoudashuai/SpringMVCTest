package cn.smbms.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class StringToDateConverter implements Converter<String, Date> {
	private String datePattern;

	public StringToDateConverter(String datePattern) {
		System.out.println("dataPattern=====================");
		this.datePattern = datePattern;
	}

	// 将字符串转换成指定格式的时间对象Date的自定义转换器
	@Override
	public Date convert(String s) {
		Date date = null;
		try {
			date = new SimpleDateFormat(datePattern).parse(s);
			System.out.println("StringToDateConverter convert date============"
					+ date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

}
