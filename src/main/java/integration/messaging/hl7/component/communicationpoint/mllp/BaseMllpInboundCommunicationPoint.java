package integration.messaging.hl7.component.communicationpoint.mllp;

import static org.apache.camel.component.hl7.HL7.ack;

import org.apache.camel.builder.TemplatedRouteBuilder;
import org.springframework.context.annotation.DependsOn;

import integration.messaging.component.communicationpoint.BaseInboundCommunicationPoint;

/**
 * Base class for all MLLP input communication points. This components reads the
 * HL7 message, stores it, writes an event and returns an ack.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMllpInboundCommunicationPoint extends BaseInboundCommunicationPoint {

	public BaseMllpInboundCommunicationPoint(String componentName) throws Exception {
		super(componentName);
	}

	private static final String CONTENT_TYPE = "HL7";

	public String getHost() {
		return componentProperties.get("HOST");
	}

	public String getPort() {
		return componentProperties.get("PORT");
	}

	@Override
	public String getFromUriString() {
		String target = getHost() + ":" + getPort();
		return "netty:tcp://" + target + getOptions();
	}

	@Override
	public String getOptions() {
		return "?sync=true&encoders=#hl7encoder&decoders=#hl7decoder";
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	@DependsOn("routeTemplates")
	public void configure() throws Exception {
		super.configure();

		// Inbound message flow for an MLLP receiver.
		from(getFromUriString()).routeId("messageReceiver-" + identifier.getComponentPath())
		        .routeGroup(identifier.getComponentPath()).autoStartup(isInboundRunning).transacted()
		        .bean(messageProcessor, "storeInboundMessageFlowStep(*," + identifier.getComponentRouteId() + ")")
		        .transform(ack()).bean(messageProcessor, "recordAck(*," + identifier.getComponentRouteId() + ")")
		        .bean(messageProcessor, "recordInboundProcessingCompleteEvent(*)");

		TemplatedRouteBuilder.builder(camelContext, "handleInboundProcessingCompleteEventTemplate")
		        .parameter("isOutboundRunning", isOutboundRunning).parameter("componentPath", identifier.getComponentPath())
		        .add();

		TemplatedRouteBuilder.builder(camelContext, "readMessageFromInboundProcessingCompleteQueueTemplate")
		        .parameter("isOutboundRunning", isOutboundRunning).parameter("componentPath", identifier.getComponentPath())
		        .parameter("componentRouteId", identifier.getComponentRouteId()).add();

		TemplatedRouteBuilder.builder(camelContext, "inboundCommunicationPointOutboundProcessorTemplate")
		        .parameter("isOutboundRunning", isOutboundRunning).parameter("componentPath", identifier.getComponentPath())
		        .parameter("componentRouteId", identifier.getComponentRouteId())
		        .bean("messageForwardingPolicy", getMessageForwardingPolicy()).add();

		TemplatedRouteBuilder.builder(camelContext, "outboundProcessingCompleteTopicConsumer")
		        .parameter("componentPath", identifier.getComponentPath()).add();
	}
}
