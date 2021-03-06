package org.protobeans.security.model;

import java.util.Locale;

import javax.validation.constraints.NotBlank;

import org.protobeans.security.validation.EmailExists;

public class RestorePasswordForm {
    @NotBlank(message = "{form.email.empty}")
    @EmailExists
    private String email;

    public String getEmail() {
        return email == null ? null : email.toLowerCase(Locale.US);
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
