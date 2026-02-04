package com.example.lab10.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Username {
    String message() default "username must be 3-20 chars and contain only letters, numbers, underscore";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
