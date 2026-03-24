package com.group4.backend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaWebConfigTest {

    @Mock
    private ViewControllerRegistry viewControllerRegistry;

    @Mock
    private ViewControllerRegistration viewControllerRegistration;

    @Mock
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @Mock
    private ResourceHandlerRegistration resourceHandlerRegistration;

    @Test
    void addViewControllersRegistersRootForwardAtHighestPrecedence() {
        when(viewControllerRegistry.addViewController("/")).thenReturn(viewControllerRegistration);

        new SpaWebConfig().addViewControllers(viewControllerRegistry);

        verify(viewControllerRegistry).addViewController("/");
        verify(viewControllerRegistration).setViewName("forward:/index.html");
        verify(viewControllerRegistry).setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void addResourceHandlersRegistersStaticLocationsAtLowestPrecedence() {
        when(resourceHandlerRegistry.addResourceHandler(any(String[].class))).thenReturn(resourceHandlerRegistration);

        new SpaWebConfig().addResourceHandlers(resourceHandlerRegistry);

        verify(resourceHandlerRegistry).setOrder(Ordered.LOWEST_PRECEDENCE);
        verify(resourceHandlerRegistry).addResourceHandler("/assets/**");
        verify(resourceHandlerRegistry).addResourceHandler(
                "/index.html", "/*.js", "/*.css", "/*.ico", "/*.png", "/logo.png");
        verify(resourceHandlerRegistration, org.mockito.Mockito.atLeastOnce()).addResourceLocations(any(String[].class));
    }
}
