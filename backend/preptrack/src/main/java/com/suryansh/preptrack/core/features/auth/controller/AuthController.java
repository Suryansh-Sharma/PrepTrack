package com.suryansh.preptrack.core.features.auth.controller;

import com.suryansh.preptrack.core.features.auth.command.changePassword.ChangePasswordCommand;
import com.suryansh.preptrack.core.features.auth.command.changePassword.ChangePasswordCommandHandler;
import com.suryansh.preptrack.core.features.auth.command.forgotPassword.ForgotPasswordCommand;
import com.suryansh.preptrack.core.features.auth.command.forgotPassword.ForgotPasswordCommandHandler;
import com.suryansh.preptrack.core.features.auth.command.login.LoginCommand;
import com.suryansh.preptrack.core.features.auth.command.login.LoginCommandHandler;
import com.suryansh.preptrack.core.features.auth.command.login.LoginResponse;
import com.suryansh.preptrack.core.features.auth.command.refreshToken.RefreshTokenCommand;
import com.suryansh.preptrack.core.features.auth.command.refreshToken.RefreshTokenCommandHandler;
import com.suryansh.preptrack.core.features.auth.command.register.RegisterCommand;
import com.suryansh.preptrack.core.features.auth.command.register.RegisterCommandHandler;
import com.suryansh.preptrack.core.features.auth.command.register.RegisterResponse;
import com.suryansh.preptrack.core.features.auth.command.resendVerification.ResendVerificationCommand;
import com.suryansh.preptrack.core.features.auth.command.resendVerification.ResendVerificationCommandHandler;
import com.suryansh.preptrack.core.features.auth.command.resetPassword.ResetPasswordCommand;
import com.suryansh.preptrack.core.features.auth.command.resetPassword.ResetPasswordHandler;
import com.suryansh.preptrack.core.features.auth.command.verifyAccount.VerifyAccountCommand;
import com.suryansh.preptrack.core.features.auth.command.verifyAccount.VerifyAccountCommandHandler;
import com.suryansh.preptrack.core.features.auth.query.me.MeQueryHandler;
import com.suryansh.preptrack.core.features.auth.query.me.MeResponseDto;
import com.suryansh.preptrack.core.features.auth.query.session.SessionInfoDto;
import com.suryansh.preptrack.core.features.auth.query.session.SessionQueryHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RegisterCommandHandler registerCommandHandler;
    private final LoginCommandHandler loginCommandHandler;
    private final RefreshTokenCommandHandler refreshTokenCommandHandler;
    private final MeQueryHandler meQueryHandler;
    private final ResendVerificationCommandHandler resendVerificationCommandHandler;
    private final VerifyAccountCommandHandler verifyAccountCommandHandler;
    private final SessionQueryHandler sessionQueryHandler;
    private final ForgotPasswordCommandHandler forgotPasswordCommandHandler;
    private final ResetPasswordHandler resetPasswordHandler;
    private final ChangePasswordCommandHandler changePasswordCommandHandler;

    public AuthController(RegisterCommandHandler registerCommandHandler, LoginCommandHandler loginCommandHandler, RefreshTokenCommandHandler refreshTokenCommandHandler, MeQueryHandler meQueryHandler, ResendVerificationCommandHandler resendVerificationCommandHandler, VerifyAccountCommandHandler verifyAccountCommandHandler, SessionQueryHandler sessionQueryHandler, ForgotPasswordCommandHandler forgotPasswordCommandHandler, ResetPasswordHandler resetPasswordHandler, ChangePasswordCommandHandler changePasswordCommandHandler) {
        this.registerCommandHandler = registerCommandHandler;
        this.loginCommandHandler = loginCommandHandler;
        this.refreshTokenCommandHandler = refreshTokenCommandHandler;
        this.meQueryHandler = meQueryHandler;
        this.resendVerificationCommandHandler = resendVerificationCommandHandler;
        this.verifyAccountCommandHandler = verifyAccountCommandHandler;
        this.sessionQueryHandler = sessionQueryHandler;
        this.forgotPasswordCommandHandler = forgotPasswordCommandHandler;
        this.resetPasswordHandler = resetPasswordHandler;
        this.changePasswordCommandHandler = changePasswordCommandHandler;
    }

    @PostMapping("/register")
    public RegisterResponse RegisterNewUser(@Valid @RequestBody RegisterCommand command) {
        return registerCommandHandler.handle(command);
    }

    @PostMapping("/login")
    public LoginResponse LoginUser(@Valid @RequestBody LoginCommand command, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return loginCommandHandler.handle(command, ipAddress, userAgent);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/refresh")
    public LoginResponse.AuthenticationInfo RegenerateAccessToken(@Valid RefreshTokenCommand command) {
        return refreshTokenCommandHandler.refresh(command);
    }

    @PostMapping("/logout")
    public void Logout(@Valid RefreshTokenCommand command) {
        refreshTokenCommandHandler.logout(command);
    }

    @GetMapping("/all-session")
    public List<SessionInfoDto> GetAllSession() {
        return sessionQueryHandler.getAllSession();
    }

    @PostMapping("/logout-all")
    public void LogoutAll() {
        refreshTokenCommandHandler.logoutAll();
    }

    @GetMapping("/me")
    public MeResponseDto me() {
        return meQueryHandler.handle();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> ResendEmailVerification(@Valid @RequestBody ResendVerificationCommand command) {
        resendVerificationCommandHandler.handle(command);
        return ResponseEntity.ok("If an account exists with this email, a verification email has been sent.");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> VerifyEmail(@Valid @RequestBody VerifyAccountCommand command) {
        verifyAccountCommandHandler.handle(command);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> ForgotPassword(@Valid @RequestBody ForgotPasswordCommand command) {
        forgotPasswordCommandHandler.handle(command);
        return ResponseEntity.ok("If an account exists with this email, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> ResetPassword(@Valid @RequestBody ResetPasswordCommand command) {
        resetPasswordHandler.handle(command);
        return ResponseEntity.ok("Reset password sent successfully");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordCommand command) {
        changePasswordCommandHandler.handle(command);
        return ResponseEntity.ok("Password changed successfully.");
    }
}