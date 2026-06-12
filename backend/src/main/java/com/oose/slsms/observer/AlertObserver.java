package com.oose.slsms.observer;

/**
 * Observer Pattern — subscriber interface.
 *
 * Concrete channels (admin push, digital signage, mobile notification, etc.)
 * implement this and register themselves with a Subject like NoiseMonitor.
 */
public interface AlertObserver {

    /** A short identifier shown in admin diagnostics — "ADMIN_PUSH" etc. */
    String channelName();

    /** Invoked synchronously by the subject when an event fires. */
    void onAlert(AlertEvent event);
}
