package de.loosecoupling.lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosecoupling.lab.jmsconsumer.ReceiverListener;

/**
 * Hello world!
 *
 */
public class App {
	
	final static Logger logger = LoggerFactory.getLogger(App.class);

	private static ConnectionFactory conFactory;
	private static Connection connection;
	private static Session session;
	private static Topic messagingTopic;
	private static MessageProducer producer;
	
	private static BufferedReader input;
	private static String username = "Stefan";

	public static void main(String[] args) {
		try {
			// initiate everything we need
			init();
			initCommunicationChannel();
		} finally {
			try {
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	private static void initCommunicationChannel() {
		input = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
            try {
				send(input.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void init() {
		try {
			// reads the configuration from jndi.properties file
			Context jndi = new InitialContext();

			// connect to messaging system
			conFactory = (ConnectionFactory) jndi.lookup("MessagingFactory");
			connection = conFactory.createConnection();
			// create session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			// lookup queue
			messagingTopic = (Topic) jndi.lookup("MessagingTopic");
			// create sender and receiver
			producer = session.createProducer(messagingTopic);
			// Create listener for receiving
			receive();
			// start connection (!)
			connection.start();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private static void send(String message) {
		try {
			// create and send text message
			TextMessage msg = session.createTextMessage(username + ": " + message);
			msg.setStringProperty("username", username);
			producer.send(msg);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private static void receive() {
		try {
			// receive message
			String selector = "username <> '" + username + "'";
			MessageConsumer messageConsumer = session.createConsumer(messagingTopic, selector);
			ReceiverListener listener = new ReceiverListener();
			messageConsumer.setMessageListener(listener);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
