package me.alissonlopes.skullcreator;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin implements CommandExecutor {

    private static final String skin_base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZmMWQ5ZmNjNTZmZDI1NWNkMWZiYjEyYzA4ZWU3ZWFiYzk0YjhkYmQwMWE5ZTAyNjJlZGI3NTM5NzU2ZjE4MCJ9fX0=";
    private static final String skin_url = "http://textures.minecraft.net/texture/c095f48e4c02a6b39d639c74448c57cfbc5805568753bb255310cd5bc8126ade";
    private static final String skin_name = "Notch";

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("SkullCreator testing unit enabled.");
        getCommand("skulltest").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        long initTime = System.currentTimeMillis();
        player.sendMessage("Giving items...");
        giveItems(player);
        player.sendMessage("Setting blocks...");
        setBlocks(player);
        long endTime = System.currentTimeMillis() - initTime;
        player.sendMessage(String.format("Giving items and settings blocks took: %s ms to complete.", endTime));
        return true;
    }

    private void giveItems(Player player) {
         player.getInventory().addItem(
                SkullCreator.itemFromBase64(skin_base64),
                SkullCreator.itemFromUrl(skin_url),
                SkullCreator.itemFromName(skin_name),
                SkullCreator.itemFromUuid(player.getUniqueId())
         );
         player.updateInventory();
    }

    private void setBlocks(Player player) {
        SkullCreator.blockWithBase64(player.getLocation().getBlock(), skin_base64);
        SkullCreator.blockWithUrl(player.getLocation().add(0, 1, 0).getBlock(), skin_url);
        SkullCreator.blockWithName(player.getLocation().add(0, 2, 0).getBlock(), skin_name);
        SkullCreator.blockWithUuid(player.getLocation().add(0, 3, 0).getBlock(), player.getUniqueId());
    }
}