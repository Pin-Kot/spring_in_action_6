package sia.tacocloud.repository;

import sia.tacocloud.entity.TacoOrder;

import java.util.Optional;

public interface OrderRepository {

    TacoOrder save(TacoOrder order);

    Optional<TacoOrder> findById(Long id);
}
