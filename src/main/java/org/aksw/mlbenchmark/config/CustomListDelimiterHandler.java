package org.aksw.mlbenchmark.config;

import java.util.List;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ValueTransformer;
import org.apache.commons.lang3.StringUtils;

public class CustomListDelimiterHandler extends DefaultListDelimiterHandler {

	public CustomListDelimiterHandler(char listDelimiter) {
		super(listDelimiter);
	}
	
	@Override
	public Object escapeList(List<?> values, ValueTransformer transformer) {
		if (values.size() > 0 && values.get(0) instanceof Byte) {
			/*
			 * This distinction was added since in case of byte values the
			 * original assignment of
			 * 
			 *   escapedValues[idx++] = escape(v, transformer);
			 * 
			 * to the String array
			 * 
			 *   Object[] escapedValues = new String[values.size()];
			 * 
			 * will cause an error,
			 */
			Object[] escapedValues = new Object[values.size()];
			int idx = 0;
			for (Object v : values) {
				escapedValues[idx++] = escape(v, transformer);
			}
			return StringUtils.join(escapedValues, getDelimiter());
			
		} else {
			return super.escapeList(values, transformer);
		}
	}
}
