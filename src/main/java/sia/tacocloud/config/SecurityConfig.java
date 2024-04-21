package sia.tacocloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import sia.tacocloud.entity.User;
import sia.tacocloud.repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
//        List<UserDetails> userList = new ArrayList<>();
//        userList.add(new User(
//                "buzz", encoder.encode("password"),
//                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));
//        userList.add(new User(
//                "woody", encoder.encode("password"),
//                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));
//        return new InMemoryUserDetailsManager(userList);
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeRequests()
                .mvcMatchers("/design", "/orders").hasRole("USER")
                .anyRequest().permitAll()

                .and()
                .formLogin()
                .loginPage("/login")

                .and()
                .logout()
                .logoutSuccessUrl("/")

                .and()
                .csrf()
                .ignoringAntMatchers("/h2-console/**")

                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                .and()
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return userName -> {
            User user = userRepo.findByUsername(userName);
            if (user != null) return user;
            throw new UsernameNotFoundException("User '" + userName + "' not found");
        };
    }
}
