package keepcraft.services;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeService {

	public void modifyRecipes(Server server) {

		// Remove these recipes
//		Iterator<Recipe> it = server.recipeIterator();
//		Recipe recipe;
//		while (it.hasNext()) {
//			recipe = it.next();
//			if (recipe != null && recipe.getResult().getType() == Material.REMOVE_ME) {
//				it.remove();
//			}
//		}

		ShapedRecipe nameTag = new ShapedRecipe(new ItemStack(Material.NAME_TAG))
				.shape("A", "B")
				.setIngredient('A', Material.STRING)
				.setIngredient('B', Material.IRON_INGOT);
		server.addRecipe(nameTag);
	}

}
