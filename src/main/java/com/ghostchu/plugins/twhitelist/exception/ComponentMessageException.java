package com.ghostchu.plugins.twhitelist.exception;

import net.kyori.adventure.text.Component;

public class ComponentMessageException extends Exception{

    private final Component message;

    public ComponentMessageException(Component message) {
        this.message = message;
    }

    public Component getComponentMessage() {
        return message;
    }
}
