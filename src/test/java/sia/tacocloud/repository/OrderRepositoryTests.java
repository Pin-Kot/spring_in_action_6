package sia.tacocloud.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import sia.tacocloud.entity.Ingredient;
import sia.tacocloud.entity.Ingredient.Type;
import sia.tacocloud.entity.Taco;
import sia.tacocloud.entity.TacoOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


//@SpringBootTest
@DataJpaTest
public class OrderRepositoryTests {

    @Autowired
    OrderRepository orderRepo;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void saveOrderWithTwoTacos() {
        TacoOrder order = new TacoOrder();
        order.setDeliveryName("Mister test");
        order.setDeliveryStreet("1234 Test street");
        order.setDeliveryCity("Testville");
        order.setDeliveryState("CA");
        order.setDeliveryZip("80123");
        order.setCcNumber("374245455400126");
        order.setCcExpiration("05/26");
        order.setCcCVV("123");
        Taco taco1 = new Taco();
        taco1.setName("Taco One");
        taco1.addIngredient(new Ingredient("FLTO", "Flour Tortilla", Type.WRAP));
        taco1.addIngredient(new Ingredient("GRBF", "Ground Beef", Type.PROTEIN));
        taco1.addIngredient(new Ingredient("GRBF", "Ground Beef", Type.PROTEIN));
        taco1.addIngredient(new Ingredient("CHED", "Shredded Cheddar", Type.CHEESE));
        order.addTaco(taco1);

        Taco taco2 = new Taco();
        taco2.setName("Taco TWO");
        taco2.addIngredient(new Ingredient("COTO", "Corn Tortilla", Type.WRAP));
        taco2.addIngredient(new Ingredient("CARN", "Carnitas", Type.PROTEIN));
        taco2.addIngredient(new Ingredient("JACK", "Monterrey Jack", Type.CHEESE));
        order.addTaco(taco2);

        TacoOrder savedOrder = orderRepo.save(order);
        assertThat(savedOrder.getId()).isNotNull();

        TacoOrder fetchedOrder = orderRepo.findById(savedOrder.getId()).get();
        assertThat(fetchedOrder.getDeliveryName()).isEqualTo("Mister test");
        assertThat(fetchedOrder.getDeliveryStreet()).isEqualTo("1234 Test street");
        assertThat(fetchedOrder.getDeliveryCity()).isEqualTo("Testville");
        assertThat(fetchedOrder.getDeliveryState()).isEqualTo("CA");
        assertThat(fetchedOrder.getDeliveryZip()).isEqualTo("80123");
        assertThat(fetchedOrder.getCcNumber()).isEqualTo("374245455400126");
        assertThat(fetchedOrder.getCcExpiration()).isEqualTo("05/26");
        assertThat(fetchedOrder.getCcCVV()).isEqualTo("123");
        assertThat(fetchedOrder.getPlacedAt().getTime()).isEqualTo(savedOrder.getPlacedAt().getTime());

        List<Taco> tacos = fetchedOrder.getTacos();
        assertThat(tacos.size()).isEqualTo(2);
        assertThat(tacos).containsExactlyInAnyOrder(taco1, taco2);
    }
}
