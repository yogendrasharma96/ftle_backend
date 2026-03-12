package com.ftle.tracker.interceptor;

import com.ftle.tracker.dto.UserDetails;

public class UserContext {

    private static final ThreadLocal<UserDetails> context = new ThreadLocal<>();

    public static void set(UserDetails user) {
        context.set(user);
    }

    public static UserDetails get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
