package com.energyxxer.guardian.events;

import com.energyxxer.util.logger.Debug;

import java.util.ArrayList;
import java.util.HashMap;

public class EventManager {
    private HashMap<Class, ArrayList<GuardianEventHandler<?>>> eventHandlers = new HashMap<>();

    public <T extends GuardianEvent> boolean invoke(T evt) {
        ArrayList<GuardianEventHandler<?>> handlers = eventHandlers.get(evt.getClass());
        if(handlers == null || handlers.isEmpty()) return false;
        for(GuardianEventHandler<?> handler : handlers) {
            try {
                ((GuardianEventHandler<T>)handler).handle(evt);
            } catch(Throwable err) {
                Debug.log("Error invoking event " + evt.getClass().getName(), Debug.MessageType.ERROR);
                err.printStackTrace();
            }
        }
        return true;
    }

    public <T extends GuardianEvent> void addEventHandler(Class<T> evtClass, GuardianEventHandler<T> handler) {
        if(!eventHandlers.containsKey(evtClass)) eventHandlers.put(evtClass, new ArrayList<>());
        eventHandlers.get(evtClass).add(handler);
    }

    public <T extends GuardianEvent> void removeEventHandler(Class<T> evtClass, GuardianEventHandler<T> handler) {
        if(!eventHandlers.containsKey(evtClass)) return;
        eventHandlers.get(evtClass).remove(handler);
    }
}
