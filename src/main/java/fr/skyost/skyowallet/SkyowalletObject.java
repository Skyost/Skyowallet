package fr.skyost.skyowallet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public abstract class SkyowalletObject {
	
	private long lastModificationTime;
	
	/**
	 * Creates a new instance of this object.
	 */
	
	SkyowalletObject() {
		this(System.currentTimeMillis());
	}
	
	/**
	 * Creates a new instance of this object.
	 * 
	 * @param lastModificationTime The last modification time of the specified object.
	 */
	
	SkyowalletObject(final long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}
	
	/**
	 * Creates a new instance of this object via JSON.
	 * 
	 * @param json The JSON string.
	 * 
	 * @throws ParseException If an exception occurs while parsing JSON.
	 * @throws IllegalAccessException  If an exception occurs while accessing fields.
	 * @throws IllegalArgumentException If an exception occurs while reading JSON.
	 */
	
	SkyowalletObject(final String json) throws ParseException, IllegalArgumentException, IllegalAccessException {
		final JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(json);
		Class<?> clazz = getClass();
		while(clazz != SkyowalletObject.class.getSuperclass()) {
			for(final Field field : clazz.getDeclaredFields()) {
				field.setAccessible(true);
				final Class<?> fieldClazz = field.getType();
				final Object value = jsonObject.get(field.getName());
				if(value != null) {
					field.set(this, value);
					continue;
				}
				if(field.getAnnotation(MustBePresent.class) != null) {
					throw new IllegalArgumentException("Invalid JSON : " + field.getName() + " is null.");
				}
				if(long.class.isAssignableFrom(fieldClazz)) {
					field.set(this, 0l);
					continue;
				}
				if(double.class.isAssignableFrom(fieldClazz)) {
					field.set(this, 0d);
					continue;
				}
				if(boolean.class.isAssignableFrom(fieldClazz)) {
					field.set(this, false);
					continue;
				}
				if(String.class.isAssignableFrom(fieldClazz)) {
					field.set(this, null);
					continue;
				}
				throw new IllegalArgumentException("Invalid JSON : " + field.getName() + " is null and no default value found.");
			}
			clazz = clazz.getSuperclass();
		}
	}
	
	/**
	 * Gets the identifier of this class.
	 * 
	 * @return The identifier of this class.
	 */
	
	public abstract String getIdentifier();
	
	/**
	 * Updates the last modification time.
	 */
	
	void updateLastModificationTime() {
		this.lastModificationTime = System.currentTimeMillis();
	}
	
	/**
	 * Gets the last modification time in millis of this object.
	 * 
	 * @return The last modification time.
	 */
	
	public final long getLastModificationTime() {
		return lastModificationTime;
	}
	
	@Override
	public final String toString() {
		try {
			final JSONObject json = new JSONObject();
			Class<?> clazz = getClass();
			while(clazz != SkyowalletObject.class.getSuperclass()) {
				for(final Field field : clazz.getDeclaredFields()) {
					field.setAccessible(true);
					json.put(field.getName(), field.get(this));
				}
				clazz = clazz.getSuperclass();
			}
			return json.toJSONString();
		}
		catch(final Exception ex) {
			return super.toString();
		}
	}
	
	/**
	 * Indicates that a field must be present in the JSON file.
	 */
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	protected @interface MustBePresent {}
	
}