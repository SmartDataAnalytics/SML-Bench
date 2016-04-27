package org.aksw.mlbenchmark.process;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * This class will run a shell script
 */
public class ProcessRunner {
	static final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);
	/*public ProcessRunner(Configuration config) {
		List<Object> systems = config.getList("learningsystems");



	}*/

	public ProcessRunner(String learningSystemDir, String command, List<String> args) throws IOException {
		DefaultExecutor e = new DefaultExecutor();
		try {
			e.setStreamHandler(new PumpStreamHandler(new FileOutputStream("lsoutput.log")));
		} catch (FileNotFoundException e1) {
			logger.warn("could not set log file output");
		}
		e.setWorkingDirectory(new File(learningSystemDir));

		CommandLine cmd = new CommandLine("./run");
		if (args != null) {
			cmd.addArguments(args.toArray(new String[0]));
		}
		int rc = e.execute(cmd);
	}

	public ProcessRunner(String learningSystemDir, String s) throws IOException {
		this(learningSystemDir, s, null);
	}
}
