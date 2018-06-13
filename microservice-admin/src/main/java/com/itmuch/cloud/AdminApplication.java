package com.itmuch.cloud;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.LoggingNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.RemindingNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@EnableDiscoveryClient
@SpringBootApplication
@EnableAdminServer
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

    //    @Configuration
//    public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter {
//        @Override
//        protected void configure(HttpSecurity http) throws Exception {
//            http.authorizeRequests().anyRequest().permitAll()//
//                    .and().csrf().disable();
//        }
//    }
    @Configuration
    public static class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

        private final String adminContextPath;

        public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
            this.adminContextPath = adminServerProperties.getContextPath();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
            successHandler.setTargetUrlParameter("redirectTo");

            http.authorizeRequests()
                    .antMatchers(adminContextPath + "/assets/**").permitAll()
                    .antMatchers(adminContextPath + "/login").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .formLogin().loginPage(adminContextPath + "/login").successHandler(successHandler).and()
                    .logout().logoutUrl(adminContextPath + "/logout").and()
                    .httpBasic().and()
                    .csrf().disable();
            // @formatter:on
        }
    }

    @Configuration
    @EnableScheduling
    public static class NotifierConfig {
        private final InstanceRepository repository;
        private final ObjectProvider<List<Notifier>> otherNotifiers;
        private static final Logger log = LoggerFactory.getLogger(NotifierConfig.class);
        public NotifierConfig(InstanceRepository repository, ObjectProvider<List<Notifier>> otherNotifiers) {
            this.repository = repository;
            this.otherNotifiers = otherNotifiers;
        }

        @Primary
        @Bean(initMethod = "start", destroyMethod = "stop")
        public RemindingNotifier remindingNotifier(InstanceRepository repository) {
            RemindingNotifier remindingNotifier = new RemindingNotifier(filteringNotifier(), repository);
            remindingNotifier.setReminderPeriod(Duration.of(1, ChronoUnit.MINUTES));
            return remindingNotifier;
        }

        @Bean
        public LoggingNotifier loggerNotifier(InstanceRepository repository) {
            return new LoggingNotifier(repository);
        }

        @Bean
        public FilteringNotifier filteringNotifier() {
            return new FilteringNotifier(new CompositeNotifier(otherNotifiers.getObject()), repository);
        }

        @Bean
        public InstanceExchangeFilterFunction auditLog() {
            return (instance, request, next) -> {
                if (HttpMethod.DELETE.equals(request.method()) || HttpMethod.POST.equals(request.method())) {
                    log.info("{} for {} on {}", request.method(), instance.getId(), request.url());
                }
                return next.exchange(request);
            };
        }

    }
}
