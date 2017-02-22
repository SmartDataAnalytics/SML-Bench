package org.aksw.mlbenchmark.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Created by Simon Bin on 16-4-14.
 */
public class INIConfigurationWriteLists extends INIConfiguration {

	public INIConfigurationWriteLists() {
		super();
	}

	public INIConfigurationWriteLists(HierarchicalConfiguration<ImmutableNode> c) {
		super(c);
	}

	// Copy of function of INIConfiguration due to access restrictions
	protected String escapeValue(String value) {
		return String.valueOf(getListDelimiterHandler().escape(
				escapeComments(value), ListDelimiterHandler.NOOP_TRANSFORMER));
	}

	// Copy of function of INIConfiguration due to access restrictions
	protected static String escapeComments(String value){
		boolean quoted = false;

		for (int i = 0; i < COMMENT_CHARS.length() && !quoted; i++) {
			char c = COMMENT_CHARS.charAt(i);
			if (value.indexOf(c) != -1) {
				quoted = true;
			}
		}

		if (quoted) {
			return '"' + value.replaceAll("\"", "\\\\\\\"") + '"';
		
		} else {
			return value;
		}
	}

	@Override
	public void write(Writer writer) throws ConfigurationException, IOException {
		if (this.getListDelimiterHandler() instanceof DisabledListDelimiterHandler) {
			super.write(writer);
		} else {
			// Mostly copy of function of INIConfiguration due to missing abstraction
			PrintWriter out = new PrintWriter(writer);
			for (String section : getSections()) {
				if (section != null) {
					out.print("[");
					out.print(section);
					out.print("]");
					out.println();
				}
				Configuration subset = getSection(section);

				Iterator<String> keys = subset.getKeys();
				while (keys.hasNext()) {
					String key = keys.next();
					Object value = subset.getProperty(key);
					if (value instanceof Collection) {
						Iterator<?> values = ((Collection<?>) value).iterator();
						out.print(key);
						out.print(" = ");

						out.print(getListDelimiterHandler().escapeList(value instanceof List
								? (List) value
								: new ArrayList((Collection) value),
								ListDelimiterHandler.NOOP_TRANSFORMER));
						out.println();
					}
					else
					{
						out.print(key);
						out.print(" = ");
						out.print(escapeValue(value.toString()));
						out.println();
					}
				}

				out.println();
			}

			out.flush();
		}
	}
}
