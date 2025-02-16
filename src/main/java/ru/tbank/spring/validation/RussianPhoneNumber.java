package ru.tbank.spring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.util.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;


@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RussianPhoneNumber.RussianPhoneNumberValidator.class)
public @interface RussianPhoneNumber {

    String message() default "Неверный формат телефона!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class RussianPhoneNumberValidator implements ConstraintValidator<RussianPhoneNumber, String> {

        private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?7\\d{10}$|^\\+?8\\d{10}$\n");

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (!StringUtils.hasText(value)) {
                return true;
            }

            return PHONE_NUMBER_PATTERN.matcher(value)
                    .matches();
        }

    }

}
