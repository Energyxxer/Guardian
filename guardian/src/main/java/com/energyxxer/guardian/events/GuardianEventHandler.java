package com.energyxxer.guardian.events;

public interface GuardianEventHandler<T extends GuardianEvent> {
    void handle(T evt);
}
