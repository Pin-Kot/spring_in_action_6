package sia.tacocloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import sia.tacocloud.entity.Ingredient;
import sia.tacocloud.entity.Ingredient.Type;
import sia.tacocloud.entity.Taco;
import sia.tacocloud.entity.TacoOrder;
import sia.tacocloud.entity.User;
import sia.tacocloud.repository.IngredientRepository;
import sia.tacocloud.repository.TacoRepository;
import sia.tacocloud.repository.UserRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping("/design")
@SessionAttributes("tacoOrder")
@Slf4j
public class DesignTacoController {

    private final IngredientRepository ingredientRepo;

    private final TacoRepository tacoRepo;

    private final UserRepository userRepo;

    @Autowired
    public DesignTacoController(IngredientRepository ingredientRepo,
                                TacoRepository tacoRepo,
                                UserRepository userRepo) {
        this.ingredientRepo = ingredientRepo;
        this.tacoRepo = tacoRepo;
        this.userRepo = userRepo;
    }

    @ModelAttribute
    public void addIngredientsToModel(Model model) {
        Iterable<Ingredient> ingredients = ingredientRepo.findAll();
        Type[] types = Type.values();
        for (Type type : types) {
            model.addAttribute(type.toString().toLowerCase(),
                    filterByType(ingredients, type));
        }
    }

    @ModelAttribute(name = "tacoOrder")
    public TacoOrder order() {
        return new TacoOrder();
    }

    @ModelAttribute(name = "taco")
    public Taco taco() {
        return new Taco();
    }

    @ModelAttribute(name = "user")
    public User user(Principal principal) {
        String username = principal.getName();
        User user = userRepo.findByUsername(username);
        return user;
    }

    @GetMapping
    public String showDesignForm() {
        return "design";
    }

    @PostMapping
    public String processTaco(
            @Valid Taco taco, Errors errors,
            @ModelAttribute TacoOrder tacoOrder) {
        if (errors.hasErrors()) {
            return "design";
        }
        Taco saved = tacoRepo.save(taco);
        tacoOrder.addTaco(saved);
        log.info("Processing taco: {}", saved);
        return "redirect:/orders/current";
    }

    private Iterable<Ingredient> filterByType(
            Iterable<Ingredient> ingredients, Type type) {
        return StreamSupport
                .stream(ingredients.spliterator(), false)
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }
}
