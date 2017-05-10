package org.aksw.mlbenchmark.config;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * A custom INIConfiguration where the write method removes the "double escaping" dot
 */
public class INIConfigurationWriteDotkeys extends INIConfigurationWriteLists {
	static final DefaultExpressionEngine dotkeysWritingEngine = new DefaultExpressionEngine(
				new DefaultExpressionEngineSymbols.Builder()
				.setPropertyDelimiter(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER)
				.setEscapedDelimiter(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER)
				.setIndexStart(DefaultExpressionEngineSymbols.DEFAULT_INDEX_START)
				.setIndexEnd(DefaultExpressionEngineSymbols.DEFAULT_INDEX_END)
				.setAttributeStart(DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START)
				.setAttributeEnd(DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END)
				.create());

	INIConfigurationWriteDotkeys() {
		super();
	}

	public INIConfigurationWriteDotkeys(HierarchicalConfiguration<ImmutableNode> c) {
		super(c);
	}

	@Override
	public void write(Writer writer) throws ConfigurationException, IOException {
		ExpressionEngine ee = this.getExpressionEngine();
		this.setExpressionEngine(dotkeysWritingEngine);
		super.write(writer);
		this.setExpressionEngine(ee);
	}
}
