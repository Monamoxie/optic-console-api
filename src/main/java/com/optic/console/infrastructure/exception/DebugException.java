package com.optic.console.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class DebugException extends RuntimeException {
    
    private final List<DebugItem> debugItems;
    private final HttpStatus status = HttpStatus.OK;
    
    public DebugException(List<DebugItem> debugItems) {
        super("Debug dump requested");
        this.debugItems = debugItems;
    }
    
    @Getter
    public static class DebugItem {
        private final String type;
        private final Object value;
        private final Object metadata;
        
        public DebugItem(String type, Object value, Object metadata) {
            this.type = type;
            this.value = value;
            this.metadata = metadata;
        }
    }
}