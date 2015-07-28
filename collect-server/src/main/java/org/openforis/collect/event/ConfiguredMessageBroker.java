package org.openforis.collect.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.openforis.rmb.MessageBroker;
import org.openforis.rmb.MessageQueue.Builder;
import org.openforis.rmb.metrics.MetricsMonitor;
import org.openforis.rmb.monitor.Event;
import org.openforis.rmb.monitor.Monitor;
import org.openforis.rmb.slf4j.Slf4jLoggingMonitor;
import org.openforis.rmb.spring.SpringJdbcMessageBroker;
import org.openforis.rmb.xstream.XStreamMessageSerializer;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

public class ConfiguredMessageBroker implements MessageBroker {

	private static final String TABLE_PREFIX = "ofc_";
	
	private final SpringJdbcMessageBroker messageBroker;

	public ConfiguredMessageBroker(DataSource dataSource) throws Exception {
		messageBroker = new SpringJdbcMessageBroker(
				dataSource);
		messageBroker.setMessageSerializer(new XStreamMessageSerializer());
		messageBroker.setTablePrefix(TABLE_PREFIX);
		List<Monitor<Event>> monitors = new ArrayList<Monitor<Event>>();
		
		monitors.add(new Slf4jLoggingMonitor());
		monitors.add(createMetricsMonitor());
		
		messageBroker.setMonitors(monitors);
		
		messageBroker.afterPropertiesSet();
	}

	private MetricsMonitor createMetricsMonitor() {
		MetricRegistry metrics = new MetricRegistry();
//		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
//				.convertRatesTo(TimeUnit.SECONDS)
//				.convertDurationsTo(TimeUnit.MILLISECONDS).build();
		Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
                .outputTo(LoggerFactory.getLogger(ConfiguredMessageBroker.class.getName()))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
		reporter.start(1, TimeUnit.SECONDS);
		MetricsMonitor metricsMonitor = new MetricsMonitor(metrics);
		return metricsMonitor;
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
	}

}
