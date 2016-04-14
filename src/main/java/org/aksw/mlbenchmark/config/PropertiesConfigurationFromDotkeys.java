package org.aksw.mlbenchmark.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;

import java.util.regex.Pattern;

/**
 * A custom PropertiesConfiguration that removes the "double escaping" from INIConfiguration files
 */
public class PropertiesConfigurationFromDotkeys extends PropertiesConfiguration {
	private boolean copying;

	private String keyTransform(String key) {
		return key.replaceAll(Pattern.quote(DefaultExpressionEngineSymbols.DEFAULT_ESCAPED_DELIMITER), DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER);
	}

	protected void setPropertyInternal(String key, Object value) {
		if (copying) {
			super.addPropertyInternal(keyTransform(key), value);
		} else {
			super.setPropertyInternal(keyTransform(key), value);
		}
	}

	protected <T extends ConfigurationEvent>
	void fireEvent(EventType<T> type, String propName, Object propValue, boolean before) {
		EventType<? extends ConfigurationEvent> ntype = copying && ConfigurationEvent.SET_PROPERTY.equals(type)
				? ConfigurationEvent.ADD_PROPERTY : type;
		super.fireEvent(ntype, keyTransform(propName), propValue, before);
	}

	public void copy(Configuration c) {
		copying = true;
		super.copy(c);
		copying = false;
	}

}
