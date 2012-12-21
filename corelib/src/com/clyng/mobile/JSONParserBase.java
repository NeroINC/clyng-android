package com.clyng.mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

abstract class JSONParserBase {

	protected String getString(JSONObject json, String name) throws JSONException {
		if (json.has(name) && !json.isNull(name)) {
			return json.getString(name);
		}
		return null;
	}

	protected int getInt(JSONObject json, String name) throws JSONException {
		if (json.has(name) && !json.isNull(name)) {
			return json.getInt(name);
		}
		return 0;
	}

	protected boolean getBoolean(JSONObject json, String name) throws JSONException {
		if (json.has(name) && !json.isNull(name)) {
			Class cl = json.get(name).getClass();
			if (cl == Boolean.class) {
				return json.getBoolean(name);
			} else {
				return json.getInt(name) != 0;
			}
		}
		return false;
	}

	protected double getDouble(JSONObject json, String name) throws JSONException {
		if (json.has(name) && !json.isNull(name)) {
			return json.getDouble(name);
		}
		return 0.0;
	}

	protected long getLong(JSONObject json, String name) throws JSONException {
		if (json.has(name) && !json.isNull(name)) {
            return json.getLong(name);
		}
		return 0;
	}

	protected JSONArray getArray(JSONObject json, String name) throws JSONException {
		if (json.has(name) && !json.isNull(name)) {
			return json.getJSONArray(name);
		}
		return null;
	}

	protected JSONObject getObject(JSONObject json, String name) throws JSONException {
		if (json.has(name) && !json.isNull(name)) {
			return json.getJSONObject(name);
		}
		return null;
	}

	protected <T> List<T> parseArray(JSONObject obj, String name, ItemParser<T> itemParser) throws JSONException {
		List<T> result = new ArrayList<T>(10);
		JSONArray array = getArray(obj, name);
		if (array == null) {
			return result;
		}

		for (int i = 0; i < array.length(); ++i) {
			result.add(itemParser.parse(array.get(i)));
		}

		return result;
	}

	public interface ItemParser<T> {
		public T parse(Object value) throws JSONException;
	}

}
