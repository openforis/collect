package org.openforis.collect.event;

import static org.openforis.collect.utils.Files.eraseFileContent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.jooq.Configuration;
import org.openforis.collect.persistence.DbUtils;
import org.openforis.rmb.MessageBroker;
import org.openforis.rmb.MessageQueue.Builder;
import org.openforis.rmb.metrics.MetricsMonitor;
import org.openforis.rmb.monitor.Event;
import org.openforis.rmb.monitor.Monitor;
import org.openforis.rmb.slf4j.Slf4jLoggingMonitor;
import org.openforis.rmb.spring.SpringJdbcMessageBroker;
import org.openforis.rmb.xstream.XStreamMessageSerializer;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class ConfiguredMessageBroker implements MessageBroker {

	private static final Logger LOG = Logger.getLogger(ConfiguredMessageBroker.class);
	private static final String TABLE_PREFIX = "ofc_";
	
	private final SpringJdbcMessageBroker messageBroker;
	private FileReporter fileReporter;

	public ConfiguredMessageBroker(DataSource dataSource, Configuration jooqConf) throws Exception {
		messageBroker = new SpringJdbcMessageBroker(dataSource);
		messageBroker.setMessageSerializer(new XStreamMessageSerializer());
		messageBroker.setTablePrefix(determineFullTablePrefix(jooqConf));
		initMonitors();
		messageBroker.afterPropertiesSet();
	}

	private String determineFullTablePrefix(Configuration jooqConf) {
		String fullPrefix = (jooqConf.settings().isRenderSchema() ? DbUtils.SCHEMA_NAME + ".": "") + TABLE_PREFIX;
		return fullPrefix;
	}

	private void initMonitors() {
		List<Monitor<Event>> monitors = new ArrayList<Monitor<Event>>();
		
		monitors.add(new Slf4jLoggingMonitor());
		MetricsMonitor metricsMonitor = createMetricsMonitor();
		if (metricsMonitor != null) {
			monitors.add(metricsMonitor);
		}
		messageBroker.setMonitors(monitors);
	}

	private MetricsMonitor createMetricsMonitor() {
		MetricRegistry metrics = new MetricRegistry();
		try {
			fileReporter = new FileReporter(metrics);
			fileReporter.start(1, TimeUnit.MINUTES);
			MetricsMonitor metricsMonitor = new MetricsMonitor(metrics);
			return metricsMonitor;
		} catch (IOException e) {
			LOG.warn("Metrics output file not found");
			return null;
		}
	}
	
	@Override
	public <M> Builder<M> queueBuilder(String queueId) {
		return messageBroker.queueBuilder(queueId);
	}

	@Override
	public <M> Builder<M> queueBuilder(String queueId, Class<M> messageType) {
		return messageBroker.queueBuilder(queueId, messageType);
	}

	@Override
	public void start() {
		messageBroker.start();		
	}

	@Override
	public void stop() {
		messageBroker.stop();
		fileReporter.stop();
	}
	
	private static class FileReporter extends ScheduledReporter {

		private static final String METRICS_LOG_FILE_NAME = "metrics.log";
		private File outputFile;
		private MetricRegistry registry;

		protected FileReporter(MetricRegistry registry) throws IOException {
			super(registry, "file-reporter", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
			this.registry = registry;
			outputFile = getMetricsOutputFile();
		}

		@Override
		public void report(@SuppressWarnings("rawtypes") SortedMap<String, Gauge> gauges,
				SortedMap<String, Counter> counters,
				SortedMap<String, Histogram> histograms,
				SortedMap<String, Meter> meters,
				SortedMap<String, Timer> timers) {
			ScheduledReporter reporter = null;
			try {
				reporter = createInternalReporter();
				reporter.report(gauges, counters, histograms, meters, timers);
			} catch (IOException e) {
				LOG.warn("Failed to write to metrics log file: " + outputFile.getAbsolutePath(), e);
			} finally {
				IOUtils.closeQuietly(reporter);
			}
		}

		private File getMetricsOutputFile() throws IOException {
			@SuppressWarnings("unchecked")
			Enumeration<Appender> e = Logger.getRootLogger().getAllAppenders();
			while (e.hasMoreElements()) {
				Appender app = e.nextElement();
				if (app instanceof FileAppender) {
					FileAppender fileAppender = (FileAppender) app;
					File openforisAppenderFile = new File(fileAppender.getFile());
					File logDir = openforisAppenderFile.getParentFile();
					File file = new File(logDir, METRICS_LOG_FILE_NAME);
					if (file.exists()) {
						eraseFileContent(file);
					} else {
						file.createNewFile();
					}
					return file;
				}
			}
			throw new IOException("Error writing metrics log file");
		}
		
		private ScheduledReporter createInternalReporter() throws FileNotFoundException {
			FileOutputStream fileOutputStream = new FileOutputStream(this.outputFile);
			ScheduledReporter reporter = ConsoleReporter.forRegistry(this.registry)
							.convertRatesTo(TimeUnit.SECONDS)
							.convertDurationsTo(TimeUnit.MILLISECONDS)
							.outputTo(new PrintStream(fileOutputStream))
							.build();
			return reporter;	
		}

	}

}
