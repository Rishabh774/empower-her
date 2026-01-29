package com.empowerher.events;

import com.empowerher.entities.Scheme;
import org.springframework.context.ApplicationEvent;

public class SchemeCreatedEvent extends ApplicationEvent {
    private final Scheme scheme;

    public SchemeCreatedEvent(Object source, Scheme scheme) {
        super(source);
        this.scheme = scheme;
    }

    public Scheme getScheme() {
        return scheme;
    }
}