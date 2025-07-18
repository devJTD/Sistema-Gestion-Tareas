package com.freedom.tareas.Controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.freedom.tareas.Service.AlertService;
import com.freedom.tareas.dto.AlertNavDTO;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalNavAdvice {

    private final AlertService alertService;

    @ModelAttribute("navAlerts")
public List<AlertNavDTO> navAlerts() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null ||
        !auth.isAuthenticated() ||
        "anonymousUser".equals(auth.getName())) {
        return List.of();
    }
    return alertService.buildAlertsFor(auth.getName());
}
}
    