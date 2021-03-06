package org.protobeans.security.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.protobeans.security.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailNotExistsValidator implements ConstraintValidator<EmailNotExists, String> {
    @Autowired
    private ProfileService profileService;
    
    @Override
    public void initialize(EmailNotExists constraintAnnotation) {
        //empty
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) return true;
        
        return profileService.getById(email) == null;
    }
}
