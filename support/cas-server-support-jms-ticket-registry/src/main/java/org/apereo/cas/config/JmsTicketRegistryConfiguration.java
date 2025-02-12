package org.apereo.cas.config;

import org.apereo.cas.StringBean;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.JmsTicketRegistry;
import org.apereo.cas.ticket.registry.JmsTicketRegistryReceiver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

/**
 * This is {@link JmsTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("jmsTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JmsTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ObjectProvider<JmsTemplate> jmsTemplate;

    @Bean
    public StringBean messageQueueTicketRegistryIdentifier() {
        return new StringBean();
    }

    @Bean
    public JmsTicketRegistryReceiver messageQueueTicketRegistryReceiver() {
        return new JmsTicketRegistryReceiver(ticketRegistry(), messageQueueTicketRegistryIdentifier());
    }

    @Lazy
    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        val jms = casProperties.getTicket().getRegistry().getJms();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(jms.getCrypto(), "jms");
        return new JmsTicketRegistry(this.jmsTemplate.getIfAvailable(), messageQueueTicketRegistryIdentifier(), cipher);
    }

    @Autowired
    @Bean
    public JmsListenerContainerFactory<?> messageQueueTicketRegistryFactory(final ConnectionFactory connectionFactory,
                                                                            final DefaultJmsListenerContainerFactoryConfigurer configurer) {
        val factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
