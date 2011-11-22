/**
 * 
 */
package org.openforis.collect.jms;

import java.io.Serializable;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.services.MessageService;
import flex.messaging.util.UUIDUtils;

/**
 * @author Mino Togna
 * 
 */
public class JmsTest {

	/**
	 * @uml.property  name="jmsTemplate"
	 * @uml.associationEnd  readOnly="true"
	 */
	@Autowired
	JmsTemplate jmsTemplate;
//	@Autowired(required = false)
//	Queue editRecordQueue;
	/**
	 * @uml.property  name="updateResult"
	 * @uml.associationEnd  readOnly="true"
	 */
	@Autowired
	Topic updateResult;

	// @Override
	// public void onMessage(Message message) {
	// try {
	// Enumeration<?> names = message.getPropertyNames();
	// for (Object name = names.nextElement(); names.hasMoreElements(); name = names.nextElement()) {
	// System.err.println(name);
	// }
	// } catch (JMSException e) {
	// e.printStackTrace();
	// }
	// }
	public void topicTest(Object object) {
		System.out.println("Topic "+object.toString());
	}

	public void update(Object object) {
		System.out.println("Queue Received: " + object.toString());
		sendMessage(object.toString());

		Destination destination = new ActiveMQTopic("updateResult");

		sendMessage(destination);
		
		// String id = FlexContext.getFlexClient().getId();
		// @SuppressWarnings("unchecked")
		// Map<Object,Object> map = (Map<Object, Object>) object;
		// MessageBroker broker = MessageBroker.getMessageBroker(null);
		// AsyncMessage msg = new AsyncMessage();
		//
		// msg.setDestination("edit-record-jms");
		//
		// Object clientID = map.get("clientId");
		// MessageService service = (MessageService) broker.getServiceByType(MessageService.class.getName());
		// MessageDestination dest = (MessageDestination) service.createDestination((String) clientID);
		// dest.setAdapter(dest.createAdapter(service.getDefaultAdapter()));
		// dest.start();
		//
		//
		//
		// msg.setClientId(clientID );
		// msg.setMessageId(UUIDUtils.createUUID());
		// msg.setTimestamp(System.currentTimeMillis());
		// msg.setBody(map.get("msg"));
		//
		// broker.routeMessageToService(msg, null);

	}

	public void sendMessage(final String msg) {
		this.jmsTemplate.send("updateResult", new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				ObjectMessage objectMessage = session.createObjectMessage();
				// Map<String, String> map = new HashMap<String, String>();
				Serializable a = new Serializable() {
					private static final long serialVersionUID = 1L;

					public String name = msg;
					private String sessionId = FlexContext.getHttpRequest().getSession().getId();

					public String getName() {
						return name;
					}

					public void setName(String name) {
						this.name = name;
					}

					public String getSessionId() {
						return sessionId;
					}

					public void setSessionId(String sessionId) {
						this.sessionId = sessionId;
					}
				};
				objectMessage.setObject(a);
				// MapMessage message = session.createMapMessage();
				// message.setString("sessionId", FlexContext.getHttpRequest().getSession().getId());
				// message.setString("msg", msg);
				return objectMessage;
			}
		});
	}

	public static void main(String[] args) {
//		Destination destination = new ActiveMQQueue("editRecordQueue");
//		 Destination destination = new ActiveMQQueue("updateResult");
		 Destination destination = new ActiveMQTopic("updateResult");

		sendMessage(destination);

	}

	private static void sendMessage(Destination destination) {
		ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
		Connection conn = null;
		Session session = null;
		try {
			conn = cf.createConnection();
			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(destination);
			TextMessage message = session.createTextMessage();
			for (int i = 0; i < 5; i++) {
				String msgBody = "b" + i;
				System.err.println("sending " + msgBody);
				message.setText(msgBody);
				producer.send(message);
			}

		} catch (JMSException e) {
			e.printStackTrace(System.err);
		} finally {
			try {
				if (session != null) {
					session.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (JMSException ex) {
			}
		}
	}

//	public JmsTemplate getJmsTemplate() {
//		return jmsTemplate;
//	}
//
//	public void setJmsTemplate(JmsTemplate jmsTemplate) {
//		this.jmsTemplate = jmsTemplate;
//	}
//
//	public Queue getQueue() {
//		return queue;
//	}
//
//	public void setQueue(Queue queue) {
//		this.queue = queue;
//	}
//
//	public Topic getProducer() {
//		return producer;
//	}
//
//	public void setProducer(Topic producerQueue) {
//		this.producer = producerQueue;
//	}

}
