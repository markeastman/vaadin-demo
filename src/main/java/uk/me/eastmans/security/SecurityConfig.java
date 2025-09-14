package uk.me.eastmans.security;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import uk.me.eastmans.security.ui.LoginView;

import javax.sql.DataSource;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
class SecurityConfig {

    @Autowired
    DataSource dataSource;

    @Autowired
    protected void configure(AuthenticationManagerBuilder auth, UserRepository userRepository) throws Exception {
        LoggerFactory.getLogger(SecurityConfig.class)
                .warn("Configuring the manager builder");
        auth.userDetailsService(new MyUserDetailsService(userRepository));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure Vaadin's security using VaadinSecurityConfigurer
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });

        http.authorizeHttpRequests( auth -> auth
                .requestMatchers("/h2-console/**").permitAll() );

        // Allow h2-console aspects
        http.csrf( c -> c.ignoringRequestMatchers("/h2-console/**"));
        http.headers( h -> h.frameOptions( frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public UserDetailsManager userDetailsManager() {
        LoggerFactory.getLogger(SecurityConfig.class)
                .warn("NOT FOR PRODUCTION: Using in-memory user details manager!");
        return new InMemoryUserDetailsManager();
    }
}
