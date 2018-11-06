package com.alfresco.client.utils;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * A default type adapter for a {@link Date} object.
 * @href https://github.com/google/gson/issues/29
 * @author Joel Leitch
 */
public class DateJava8Converter implements JsonSerializer<Date>, JsonDeserializer<Date> {

	private final DateFormat format = new java.text.SimpleDateFormat(ISO8601Utils.DATE_ISO_FORMAT);//DateFormat.getInstance();

	public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
		String dateFormatAsString = format.format(src);
		return new JsonPrimitive(dateFormatAsString);
	}

	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		
		if (!(json instanceof JsonPrimitive)) {
			throw new JsonParseException("The date should be a string value");
		}

		try {
			return format.parse(json.getAsString());
		} catch (ParseException e) {
			throw new JsonParseException(e);
		}
	}
	
//	@Override
//    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//    	
//    	DateFormat dateFormat = new java.text.SimpleDateFormat(ISO8601Utils.DATE_ISO_FORMAT);
//        try {
//			return dateFormat.parse(json.getAsString());
//		} catch (ParseException e) {
//			 throw new JsonParseException(e.getMessage(),e);
//		}
//    }
}
