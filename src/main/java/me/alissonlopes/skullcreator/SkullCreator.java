package me.alissonlopes.skullcreator;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;

/**
 * A library for the Bukkit API to create player skulls
 * from names, base64 strings, and texture URLs.
 * <p>
 * Does not use any NMS code, and should work across all versions.
 *
 * @author Dean B on 12/28/2016 and modified by Alisson Lopes on 20/02/2022.
 * @implNote The current implementation uses a lazy variable to
 * 			 distinguish the material instance used to create the
 * 			 head ItemStack, alongside it, there is a boolean variable
 * 			 that replaces the functionality of the old <code>checkLegacy()</code> method,
 * 			 which verified the version of the used Bukkit's Material API
 */
public final class SkullCreator {

	// some reflection stuff to be used when setting a skull's profile
	private static Field blockProfileField;
	private static Method metaSetProfileMethod;
	private static Field metaProfileField;

	// Newly introduced
	private static Material headMaterial;
	private static Material blockMaterial;
	private static boolean legacy;

	/*
	 lazy initializing the variable only one time
	 instead of searching for the Material every
	 time taking multiple O(n) costs. In a normal
	 small enum this usually doesn't matter but on
	 the Bukkit's Material enum class which contains
	 dozens of entries we should avoid it, specially
	 in a environment when we create a lot of different
	 skulls
	 Reference: https://stackoverflow.com/questions/24254250/what-is-the-fastest-for-map-keys-enum-valueof-vs-string-hashcode
	 */
	/**
	 * Return the material used to create item or block heads
	 *
	 * Initializes the block and item material fields if they
	 * haven't been initialized yet
	 *
	 * @param asBlock true, if the method should return the block material
	 *                or false, the head material
	 * @return the head or block material
	 */
	private static Material requireSkullMaterial(boolean asBlock) {
		if (headMaterial != null) {
			return asBlock ? blockMaterial : headMaterial;
		}

		try {
			headMaterial = Material.valueOf("PLAYER_HEAD");
			blockMaterial = headMaterial;
		} catch (IllegalArgumentException e) {
			headMaterial = Material.valueOf("SKULL_ITEM");
			blockMaterial = Material.valueOf("SKULL");
			legacy = true;
		}

		return asBlock ? blockMaterial : headMaterial;
	}

	public static ItemStack createSkull() {
		final Material skullMaterial = requireSkullMaterial(false);
		if (legacy) {
			return new ItemStack(skullMaterial, 1, (byte) 3);
		}
		return new ItemStack(skullMaterial);
	}

	/**
	 * Creates a player skull item with the skin based on a player's name.
	 *
	 * @param name The Player's name.
	 * @return The head of the Player.
	 * @deprecated names don't make for good identifiers.
	 */
	public static ItemStack itemFromName(String name) {
		return itemWithName(createSkull(), name);
	}

	/**
	 * Creates a player skull item with the skin based on a player's UUID.
	 *
	 * @param uuid The Player's UUID.
	 * @return The head of the Player.
	 */
	public static ItemStack itemFromUuid(UUID uuid) {
		return itemWithUuid(createSkull(), uuid);
	}

	/**
	 * Creates a player skull item with the skin at a Mojang URL.
	 *
	 * @param url The Mojang URL.
	 * @return The head of the Player.
	 */
	public static ItemStack itemFromUrl(String url) {
		return itemWithUrl(createSkull(), url);
	}

	/**
	 * Creates a player skull item with the skin based on a base64 string.
	 *
	 * @param base64 The Mojang URL.
	 * @return The head of the Player.
	 */
	public static ItemStack itemFromBase64(String base64) {
		return itemWithBase64(createSkull(), base64);
	}

	/**
	 * Modifies a skull to use the skin of the player with a given name.
	 *
	 * @param item The item to apply the name to. Must be a player skull.
	 * @param name The Player's name.
	 * @return The head of the Player.
	 * @deprecated names don't make for good identifiers.
	 */
	@Deprecated
	public static ItemStack itemWithName(ItemStack item, String name) {
		notNull(item, "item");
		notNull(name, "name");

		SkullMeta meta = (SkullMeta) item.getItemMeta();
		SkullOwner.setOwner(meta, name);
		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Modifies a skull to use the skin of the player with a given UUID.
	 *
	 * @param item The item to apply the name to. Must be a player skull.
	 * @param uuid   The Player's UUID.
	 * @return The head of the Player.
	 */
	public static ItemStack itemWithUuid(ItemStack item, UUID uuid) {
		notNull(item, "item");
		notNull(uuid, "uuid");

		SkullMeta meta = (SkullMeta) item.getItemMeta();
		SkullOwner.setOwner(meta, uuid);
		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Modifies a skull to use the skin at the given Mojang URL.
	 *
	 * @param item The item to apply the skin to. Must be a player skull.
	 * @param url  The URL of the Mojang skin.
	 * @return The head associated with the URL.
	 */
	public static ItemStack itemWithUrl(ItemStack item, String url) {
		notNull(item, "item");
		notNull(url, "url");

		return itemWithBase64(item, urlToBase64(url));
	}

	/**
	 * Modifies a skull to use the skin based on the given base64 string.
	 *
	 * @param item   The ItemStack to put the base64 onto. Must be a player skull.
	 * @param base64 The base64 string containing the texture.
	 * @return The head with a custom texture.
	 */
	public static ItemStack itemWithBase64(ItemStack item, String base64) {
		notNull(item, "item");
		notNull(base64, "base64");

		if (!(item.getItemMeta() instanceof SkullMeta)) {
			return null;
		}
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		mutateItemMeta(meta, base64);
		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Sets the block to a skull with the given name.
	 *
	 * @param block The block to set.
	 * @param name  The player to set it to.
	 * @deprecated names don't make for good identifiers.
	 */
	@Deprecated
	public static void blockWithName(Block block, String name) {
		notNull(block, "block");
		notNull(name, "name");

		setToSkull(block);
		Skull state = (Skull) block.getState();
		SkullOwner.setOwner(state, name);
		state.update(false, false);
	}

	/**
	 * Sets the block to a skull with the given UUID.
	 *
	 * @param block The block to set.
	 * @param uuid    The player to set it to.
	 */
	public static void blockWithUuid(Block block, UUID uuid) {
		notNull(block, "block");
		notNull(uuid, "uuid");

		setToSkull(block);
		Skull state = (Skull) block.getState();
		SkullOwner.setOwner(state, uuid);
		state.update(false, false);
	}

	/**
	 * Sets the block to a skull with the skin found at the provided mojang URL.
	 *
	 * @param block The block to set.
	 * @param url   The mojang URL to set it to use.
	 */
	public static void blockWithUrl(Block block, String url) {
		notNull(block, "block");
		notNull(url, "url");

		blockWithBase64(block, urlToBase64(url));
	}

	/**
	 * Sets the block to a skull with the skin for the base64 string.
	 *
	 * @param block  The block to set.
	 * @param base64 The base64 to set it to use.
	 */
	public static void blockWithBase64(Block block, String base64) {
		notNull(block, "block");
		notNull(base64, "base64");

		setToSkull(block);
		Skull state = (Skull) block.getState();
		mutateBlockState(state, base64);
		state.update(false, false);
	}

	private static void setToSkull(Block block) {
		final Material blockMaterial = requireSkullMaterial(true);

		if (legacy) {
			block.setType(blockMaterial, false);
			Skull state = (Skull) block.getState();
			state.setSkullType(SkullType.PLAYER); // implement this method with reflection if removed
			state.update(false, false);
		} else {
			block.setType(blockMaterial, false);
		}
	}

	private static void notNull(Object o, String name) {
		if (o == null) {
			throw new NullPointerException(name + " should not be null!");
		}
	}

	private static String urlToBase64(String url) {
		URI actualUrl;

		try {
			actualUrl = new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl.toString() + "\"}}}";
		return Base64.getEncoder().encodeToString(toEncode.getBytes());
	}

	private static GameProfile makeProfile(String b64) {
		// random uuid based on the b64 string
		UUID id = new UUID(
				b64.substring(b64.length() - 20).hashCode(),
				b64.substring(b64.length() - 10).hashCode()
		);

		GameProfile profile = new GameProfile(id, "Player");
		profile.getProperties().put("textures", new Property("textures", b64));
		return profile;
	}

	private static void mutateBlockState(Skull block, String b64) {
		try {
			if (blockProfileField == null) {
				blockProfileField = block.getClass().getDeclaredField("profile");
				blockProfileField.setAccessible(true);
			}

			blockProfileField.set(block, makeProfile(b64));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static void mutateItemMeta(SkullMeta meta, String b64) {
		try {
			if (metaSetProfileMethod == null) {
				metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
				metaSetProfileMethod.setAccessible(true);
			}

			metaSetProfileMethod.invoke(meta, makeProfile(b64));
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
			// if in an older API where there is no setProfile method,
			// we set the profile field directly.
			try {
				if (metaProfileField == null) {
					metaProfileField = meta.getClass().getDeclaredField("profile");
					metaProfileField.setAccessible(true);
				}

				metaProfileField.set(meta, makeProfile(b64));
			} catch (NoSuchFieldException | IllegalAccessException ex2) {
				ex2.printStackTrace();
			}
		}
	}
}