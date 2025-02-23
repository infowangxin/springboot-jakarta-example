package com.nutcracker.example.demo.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CustomInvalidSessionStrategy
 *
 * @author 胡桃夹子
 * @date 2022/12/23 08:28
 */
@Slf4j
@Component
public class CustomInvalidSessionStrategy implements InvalidSessionStrategy {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        log.debug("onInvalidSessionDetected {}", request.getRequestURI());
        request.getRequestDispatcher("/invalid_session").forward(request, response);
    }
}
