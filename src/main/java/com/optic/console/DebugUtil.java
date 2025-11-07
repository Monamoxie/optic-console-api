package com.optic.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.ResponseEntity;

public class DebugUtil {
    public static <T> ResponseEntity<String> dd(T object) throws Exception {
        String prettyJson = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .writeValueAsString(object);

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(prettyJson);
    }
}