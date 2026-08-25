package be.ucll.backend.eindproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OrganizerRequest {

    private String name;

    private String email;

    private String password;

    private String validationUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }
}
