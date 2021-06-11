package com.cicoria.ehsyncmode;

import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.EmitResult;

@RestController
public class EventProducerController {

    public static final Logger LOGGER = LoggerFactory.getLogger(EventProducerController.class);

    @Autowired
    private Sinks.Many<Message<String>> many;

    @Autowired
    public Sinks.One<Message<String>> one;

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestParam String message) {
        LOGGER.info("Going to add message {} to sendMessage.", message);
        // EmitResult result = many.tryEmitNext(MessageBuilder.withPayload(message).build());

        /** noe at a time */
        // one.emitValue(MessageBuilder.withPayload(message).build(), retryOnNonSerializedElse(Sinks.EmitFailureHandler.FAIL_FAST));

        // return ResponseEntity.ok(message);


        /** anotger wat */
        EmitResult result = many.tryEmitNext(MessageBuilder.withPayload(message).build());
        //EmitResult result2 = many.tryEmitComplete();
        if (result.isSuccess())
            return ResponseEntity.ok(message);
        if (result.isFailure())
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        
        return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY); 
        
        /** oine way */
        // many.emitNext(MessageBuilder.withPayload(message).build(),retryOnNonSerializedElse(Sinks.EmitFailureHandler.FAIL_FAST)); // Sinks.EmitFailureHandler.FAIL_FAST);

        // return ResponseEntity.ok(message);
        
    }

        // From: https://stackoverflow.com/questions/65029619/how-to-call-sinks-manyt-tryemitnext-from-multiple-threads/65185325#65185325
    public static EmitFailureHandler retryOnNonSerializedElse(EmitFailureHandler fallback) {
        return (signalType, emitResult) -> {
            if (emitResult == EmitResult.FAIL_NON_SERIALIZED) {
                LockSupport.parkNanos(10);
                return true;
            } else
                return fallback.onEmitFailure(signalType, emitResult);
        };
    }
}