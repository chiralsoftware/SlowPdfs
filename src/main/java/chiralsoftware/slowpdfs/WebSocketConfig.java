package chiralsoftware.slowpdfs;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static org.springframework.messaging.support.MessageHeaderAccessor.getAccessor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configure web sockets
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger LOG = Logger.getLogger(WebSocketConfig.class.getName());
    
    @Value("#{environment.getProperty('WEBSOCKET_BASE')}")
    private String websocketBase;

    private static final String websocketMatcher = 
            "(?<protocol>wss?)://(?<host>[\\p{Alnum}.]+)(?<port>:\\p{Digit}+)?/(?<context>\\p{Alnum}+)";
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        if (websocketBase == null)
            LOG.info("No WEBSOCKET_BASE env property");
        else
            LOG.info("WEBSOCKET_BASE: " + websocketBase);        
        
        if(websocketBase == null) {
            stompEndpointRegistry.addEndpoint("/mystatus").withSockJS();
            return;
        }
        final Pattern pattern = compile(websocketMatcher);
        final Matcher matcher = pattern.matcher(websocketBase);
        if(! matcher.matches()) {
            LOG.warning("Could not parse the websocket base url: " + websocketBase + " with matcher: " + websocketMatcher);
            stompEndpointRegistry.addEndpoint("/mystatus").withSockJS();
            return;
        }
        final StringBuilder myOrigin = new StringBuilder();
        if (matcher.group("protocol").equalsIgnoreCase("wss"))
            myOrigin.append("https");
        else
            myOrigin.append("http");
        myOrigin.append("://").append(matcher.group("host"));
        if (matcher.group("port") != null)
            myOrigin.append(matcher.group("port"));
        if(matcher.group("context") != null) 
            LOG.info("Found this context: " + matcher.group("context"));
        LOG.info("myOirgin is; "  + myOrigin);
        stompEndpointRegistry.addEndpoint("/mystatus").
                setAllowedOrigins(myOrigin.toString()).
                withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                final StompHeaderAccessor accessor =
                        getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        LOG.info("this is the auth token: " + token);
                        // in real use we would validate the token and set the authentication
                        // Validate the token and set the user
//                        Authentication auth = validateToken(token);
//                        accessor.setUser(auth);
//                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
                return message;
            }
        });
    }
}
