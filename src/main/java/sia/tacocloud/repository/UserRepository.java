package sia.tacocloud.repository;

import org.springframework.data.repository.CrudRepository;
import sia.tacocloud.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByUsername(String userName);
}
