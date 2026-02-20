package com.fiap.fase4.application.gateway;

import com.fiap.fase4.application.dto.events.PaymentApprovedEvent;
import com.fiap.fase4.application.dto.events.PaymentFailedEvent;

public interface DomainEventPublisher {
    void publishPaymentApprovedEvent(PaymentApprovedEvent event);
    void publishPaymentFailedEvent(PaymentFailedEvent event);
}
