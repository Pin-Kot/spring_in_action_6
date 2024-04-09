package sia.tacocloud.repository;

import aj.org.objectweb.asm.Type;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sia.tacocloud.entity.IngredientRef;
import sia.tacocloud.entity.Taco;
import sia.tacocloud.entity.TacoOrder;

import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcOrderRepository implements OrderRepository {

    private JdbcOperations jdbcOperations;

    public JdbcOrderRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    @Transactional
    public TacoOrder save(TacoOrder order) {
        PreparedStatementCreatorFactory pscf =
                new PreparedStatementCreatorFactory(
                        "insert into Taco_order "
                                + "(delivery_name, delivery_street, delivery_city, "
                                + "delivery_state, delivery_zip, cc_number, "
                                + "cc_expiration, cc_cvv, placed_at) "
                                + "values (?,?,?,?,?,?,?,?,?)",
                        Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP
                );
        pscf.setReturnGeneratedKeys(true);

        order.setPlacedAt(new Date());
        PreparedStatementCreator psc =
                pscf.newPreparedStatementCreator(
                        Arrays.asList(
                                order.getDeliveryName(),
                                order.getDeliveryStreet(),
                                order.getDeliveryCity(),
                                order.getDeliveryState(),
                                order.getDeliveryZip(),
                                order.getCcNumber(),
                                order.getCcExpiration(),
                                order.getCcCVV(),
                                order.getPlacedAt()));

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(psc, keyHolder);
        Long orderId = keyHolder.getKey().longValue();
        order.setId(orderId);

        List<Taco> tacos = order.getTacos();
        int i = 0;
        for (Taco taco : tacos) {
            saveTaco(orderId, i++, taco);
        }
        return order;
    }

    private long saveTaco(Long orderId, int orderKey, Taco taco) {
        taco.setCreatedAt(new Date());
        PreparedStatementCreatorFactory pscf =
                new PreparedStatementCreatorFactory(
                        "insert into Taco "
                                + "(name, created_at, taco_order, taco_order_key) "
                                + "values (?, ?, ?, ?)",
                        Types.VARCHAR, Types.TIMESTAMP, Type.LONG, Type.LONG
                );
        pscf.setReturnGeneratedKeys(true);

        PreparedStatementCreator psc =
                pscf.newPreparedStatementCreator(
                        Arrays.asList(
                                taco.getName(),
                                taco.getCreatedAt(),
                                orderId,
                                orderKey));
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(psc, keyHolder);
        Long tacoId = keyHolder.getKey().longValue();
        taco.setId(tacoId);

        saveIngredientRefs(tacoId, taco.getIngredients());
        return tacoId;
    }

    private void saveIngredientRefs(
            Long tacoId, List<IngredientRef> ingredientRefs) {
        int key = 0;
        for (IngredientRef ingredientRef : ingredientRefs) {
            jdbcOperations.update(
                    "insert into Ingredient_Ref (ingredient, taco, taco_key) "
                            + "values (?, ?, ?)",
                    ingredientRef.getIngredient(), tacoId, key++);
        }
    }

    @Override
    public Optional<TacoOrder> findById(Long id) {
        try {
            TacoOrder order = jdbcOperations.queryForObject(
                    "select id, delivery_name, delivery_street, delivery_city, "
                            + "delivery_state, delivery_zip, cc_number, cc_expiration, "
                            + "cc_cvv, placed_at from Taco_Order where id=?",
                    (rs, rowNum) -> {
                        TacoOrder tacoOrder = new TacoOrder();
                        tacoOrder.setId(rs.getLong("id"));
                        tacoOrder.setDeliveryName(rs.getString("delivery_name"));
                        tacoOrder.setDeliveryStreet(rs.getString("delivery_street"));
                        tacoOrder.setDeliveryCity(rs.getString("delivery_city"));
                        tacoOrder.setDeliveryState(rs.getString("delivery_state"));
                        tacoOrder.setDeliveryZip(rs.getString("delivery_zip"));
                        tacoOrder.setCcNumber(rs.getString("cc_number"));
                        tacoOrder.setCcExpiration(rs.getString("cc_expiration"));
                        tacoOrder.setCcCVV(rs.getString("cc_cvv"));
                        tacoOrder.setPlacedAt(new Date(rs.getTimestamp("placed_at").getTime()));
                        tacoOrder.setTacos(findTacosByOrderId(rs.getLong("id")));
                        return tacoOrder;
                    }, id);
            return Optional.of(order);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    private List<Taco> findTacosByOrderId(Long orderId) {
        return jdbcOperations.query(
                "select id, name, created_at from Taco "
                        + "where taco_order=? order by taco_order_key",
                (rs, rowNum) -> {
                    Taco taco = new Taco();
                    taco.setId(rs.getLong("id"));
                    taco.setName(rs.getString("name"));
                    taco.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    taco.setIngredients(findIngredientsByTacoId(rs.getLong("id")));
                    return taco;
                }, orderId);
    }

    private List<IngredientRef> findIngredientsByTacoId(Long tacoId) {
        return jdbcOperations.query(
                "select ingredient from Ingredient_Ref "
                        + "where taco = ? order by taco_key",
                (rs, rowNum) -> {
                    return new IngredientRef(rs.getString("ingredient"));
                }, tacoId);
    }
}
