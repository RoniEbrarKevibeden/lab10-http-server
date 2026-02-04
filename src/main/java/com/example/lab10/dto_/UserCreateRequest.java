package com.example.lab10.dto_;

import com.example.lab10.validation.Username;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateRequest {

    @NotBlank(message = "username is required")
    @Username
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 64, message = "password length must be between 6 and 64")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
