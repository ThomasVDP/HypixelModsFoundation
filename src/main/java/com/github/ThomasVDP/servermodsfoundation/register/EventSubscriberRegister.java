package com.github.ThomasVDP.servermodsfoundation.register;

import com.github.ThomasVDP.servermodsfoundation.library.events.IEventSubscribeRegister;
import com.google.common.reflect.TypeToken;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class EventSubscriberRegister implements IEventSubscribeRegister
{
    private Map<String, Map<Object, List<IEventListener>>> listeners = new HashMap<>();
    private String currentAddonId = "";

    @Override
    public void registerSubscriber(Object subscriber)
    {
        if (listeners.containsKey(this.currentAddonId)) {
            if (listeners.get(this.currentAddonId).containsKey(subscriber)) return;
        } else {
            listeners.put(this.currentAddonId, new HashMap<>());
        }

        Set<? extends Class<?>> supers = TypeToken.of(subscriber.getClass()).getTypes().rawTypes();
        for (Method method : subscriber.getClass().getMethods())
        {
            for (Class<?> cls : supers)
            {
                try
                {
                    Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (real.isAnnotationPresent(SubscribeEvent.class))
                    {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1) {
                            throw new IllegalArgumentException(
                                    "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                                            " arguments.  Event handler methods must require a single argument."
                            );
                        }

                        Class<?> eventType = parameterTypes[0];
                        if (!Event.class.isAssignableFrom(eventType)) {
                            throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                        }

                        register(eventType, subscriber, real, this.currentAddonId);
                    }

                } catch (NoSuchMethodException ignored) {
                }
            }
        }
    }

    private void register(Class<?> eventType, Object target, Method method, String addonName)
    {
        try
        {
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            Event event = (Event)eventType.newInstance();
            // IMPORTANT since there is no apparent other way, we first set the currentAddonId manually
            // in order to get picked up by the CreateWrapper methodd
            ASMEventHandlerWrapper.currentAddonId = addonName;
            ASMEventHandlerWrapper listener = new ASMEventHandlerWrapper(target, method);
            event.getListenerList().register(0, listener.getPriority(), listener);

            listeners.get(addonName).computeIfAbsent(target, k -> new ArrayList<>()).add(listener);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setAddon(String addonName)
    {
        this.currentAddonId = addonName;
    }
}
