package integration.messaging.hl7.component.communicationpoint.mllp;

import org.apache.camel.builder.TemplatedRouteBuilder;

import integration.messaging.component.communicationpoint.BaseOutboundCommunicationPoint;

/**
 * Base class for all MLLP Outbound communication points.
 */
public abstract class BaseMllpOutboundCommunicationPoint extends BaseOutboundCommunicationPoint {

    public BaseMllpOutboundCommunicationPoint(String componentName) {
        super(componentName);
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getOptions() {
        return "?sync=true&encoders=#hl7encoder&decoders=#hl7decoder";
    }

    public String getTargetHost() {
        return componentProperties.get("TARGET_HOST");
    }

    public String getTargetPort() {
        return componentProperties.get("TARGET_PORT");
    }

    @Override
    public String getToUriString() {
        String target = getTargetHost() + ":" + getTargetPort();
        return "netty:tcp://" + target + getOptions();
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void configure() throws Exception {
        super.configure();

        TemplatedRouteBuilder.builder(camelContext, "readMessageFromInboundProcessingCompleteQueueTemplate")
                .parameter("isOutboundRunning", isOutboundRunning).parameter("componentPath", identifier.getComponentPath())
                .parameter("componentRouteId", identifier.getComponentRouteId()).add();

        for (String sourceComponent : sourceComponentPaths) {
            TemplatedRouteBuilder.builder(camelContext, "componentInboundFilterableTopicConsumer")
                    .parameter("isInboundRunning", isInboundRunning).parameter("componentPath", identifier.getComponentPath())
                    .parameter("sourceComponentPath", sourceComponent)
                    .parameter("componentRouteId", identifier.getComponentRouteId())
                    .bean("messageAcceptancePolicy", getMessageAcceptancePolicy()).add();
        }

        TemplatedRouteBuilder.builder(camelContext, "handleInboundProcessingCompleteEventTemplate")
                .parameter("isOutboundRunning", isOutboundRunning).parameter("componentPath", identifier.getComponentPath())
                .add();

        // Outbound message flow from an inbound communication point.
        from("direct:outboundProcessor-" + identifier.getComponentPath())
                .routeId("outboundProcessor-" + identifier.getComponentPath()).routeGroup(identifier.getComponentPath())
                .autoStartup(isOutboundRunning).setHeader("contentType", simple(getContentType()))
                .bean(messageProcessor, "storeOutboundMessageFlowStep(*," + identifier.getComponentRouteId() + ")")
                .bean(messageProcessor, "recordOutboundProcessingCompleteEvent(*)");

        // Process outbound processing complete events.
        from("direct:handleOutboundProcessCompleteEvent-" + identifier.getComponentPath())
                .routeId("handleOutboundProcessCompleteEvent-" + identifier.getComponentPath())
                .routeGroup(identifier.getComponentPath()).transacted("").bean(messageProcessor, "deleteMessageFlowEvent(*)")
                .transform().method(messageProcessor, "replaceMessageBodyIdWithMessageContent(*)")
                .bean(messageProcessor, "storeOutboundMessageFlowStep(*," + identifier.getComponentRouteId() + ")")
                .to(getToUriString());

    }
}
