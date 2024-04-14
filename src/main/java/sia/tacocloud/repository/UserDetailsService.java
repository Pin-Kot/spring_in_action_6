package sia.tacocloud.repository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService {

    UserDetails loadUserByUserName(String userName) throws
            UsernameNotFoundException;
}
