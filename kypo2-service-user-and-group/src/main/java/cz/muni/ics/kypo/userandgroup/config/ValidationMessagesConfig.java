package cz.muni.ics.kypo.userandgroup.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class ValidationMessagesConfig {

    @Bean
    public MessageSource messageSourceValidation() {
        final ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:locale/ValidationMessages");
        source.setUseCodeAsDefaultMessage(true);
        source.setDefaultEncoding("UTF-8");

        source.setCacheSeconds(0);
        return source;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSourceValidation());
        return bean;
    }

    /**
     * Prints available locales. It is useful to set up appropriate ValidationMessages.properties file name,
     * e.g. messages_en_US.properties
     * <p>
     * en_US
     * <p>
     * en - language; US - country
     */
    public static void main(String[] args) {
        Locale[] locales = Locale.getAvailableLocales();
        Arrays.sort(locales, (l1, l2) -> l1.toString().compareTo(l2.toString()));
        for (Locale l : locales) {
            System.out.println(l.toString());
        }
    }

}
