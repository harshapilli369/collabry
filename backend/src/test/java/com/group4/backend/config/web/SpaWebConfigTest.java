package com.group4.backend.config.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(viewControllerRegistry).addViewController(pathCaptor.capture());
        assertThat(pathCaptor.getValue()).isEqualTo("/");
        verify(viewControllerRegistration).setViewName("forward:/index.html");
        verify(viewControllerRegistry).setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void addResourceHandlersRegistersStaticLocationsAtLowestPrecedence() {
        when(resourceHandlerRegistry.addResourceHandler(any(String[].class))).thenReturn(resourceHandlerRegistration);

        new SpaWebConfig().addResourceHandlers(resourceHandlerRegistry);

        verify(resourceHandlerRegistry).setOrder(Ordered.LOWEST_PRECEDENCE);
        ArgumentCaptor<String[]> pathsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(resourceHandlerRegistry, atLeastOnce()).addResourceHandler(pathsCaptor.capture());
        assertThat(pathsCaptor.getAllValues()).as("resource handler paths").anySatisfy(args -> assertThat(args).contains("/assets/**"));
        verify(resourceHandlerRegistration, atLeastOnce()).addResourceLocations(any(String[].class));
    }
}
