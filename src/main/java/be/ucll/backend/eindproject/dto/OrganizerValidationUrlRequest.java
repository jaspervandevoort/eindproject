package be.ucll.backend.eindproject.dto;

import jakarta.validation.constraints.NotBlank;

public class OrganizerValidationUrlRequest {

    @NotBlank
    private String validationUrl;

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }
}
