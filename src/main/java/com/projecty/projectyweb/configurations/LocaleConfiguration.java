package com.projecty.projectyweb.configurations;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class LocaleConfiguration {
    
    @Value("${il8n.supported-languages}")
    private List<String> supportedLanguages;
    
    @Bean
    public LocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        if(supportedLanguages != null) {
           List<Locale> supportedLocales =  supportedLanguages.stream().map(l->new Locale(l)).collect(Collectors.toList());
           resolver.setSupportedLocales(supportedLocales);
        }
        resolver.setDefaultLocale(Locale.getDefault());
        return resolver;
    }

}
