package main.java.fr.farmeurimmo.reapersanction.cmd;

import main.java.fr.farmeurimmo.reapersanction.ReaperSanction;
import main.java.fr.farmeurimmo.reapersanction.storage.DatabaseManager;
import main.java.fr.farmeurimmo.reapersanction.storage.FilesManager;
import main.java.fr.farmeurimmo.reapersanction.storage.LocalStorageManager;
import main.java.fr.farmeurimmo.reapersanction.storage.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RsAdminCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(MessageManager.prefix +
                    MessageManager.instance.getMessage("ErrorArgAdminCommands"));
            sender.sendMessage("Subs commands available: infos, reload, rl");
            return true;
        }
        if (args[0].equalsIgnoreCase("infos")) {
            sender.sendMessage("Plugin developper : Farmeurimmo");
            sender.sendMessage("Email : contact@farmeurimmo.fr");
            sender.sendMessage("Website : https://reaper.farmeurimmo.fr/reapersanction");
            sender.sendMessage("Version : " + ReaperSanction.instance.getDescription().getVersion());
            return true;
        }
        if (!sender.hasPermission("reapersanction.rsadmin")) {
            sender.sendMessage(MessageManager.prefix +
                    MessageManager.instance.getMessage("NoPermission"));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            ReaperSanction.instance.reload();
            sender.sendMessage(MessageManager.prefix +
                    MessageManager.instance.getMessage("ReloadMessage"));
            return true;
        }
        if (args[0].equalsIgnoreCase("migratedb")) {
            if (!ReaperSanction.instance.matchRequirementsToMigrateToMYSQL()) {
                sender.sendMessage("§l§cIf you want to upgrade to MYSQL and you have already sanctions on players, you can, just follow those steps !\n" +
                        "§cPlease migrate your db when the server is empty, and make a backup of your old messages.yml to prevent data loss if the server crash !\n" +
                        "§l§c1. §7Stop your server\n" +
                        "§l§c2. §7Reset your config.yml or just add the missing part\n" +
                        "§7(you can check the default one here (https://github.com/Reaper-Solutions/Minecraft-ReaperSanction/blob/main/src/main/resources/config.yml)\n" +
                        "§l§c3. §7Place your credentials in the config.yml an change YAML to MYSQL\n" +
                        "§l§c4. §7Start your server\n" +
                        "§l§c5. §7Type this command again");
                return false;
            }
            long start = System.currentTimeMillis();
            sender.sendMessage("§cStarting migration, can take a while depending on how much users you have.");
            CompletableFuture.runAsync(() -> {
                sender.sendMessage("§7Migrating users... (ASYNC)");
                FilesManager.instance.setup_YAML_Storage();
                if (LocalStorageManager.instance.isAnOldYAMLFile())
                    LocalStorageManager.instance.convertFromOldStorageMethod();
                else LocalStorageManager.instance.loadUsers();
                DatabaseManager.instance.updateAllUsersFromMigratation();
                FilesManager.instance.deleteAndRecreateDataFile();
                sender.sendMessage("§aMigration done ! (took " + (System.currentTimeMillis() - start) + "ms)");
            });
            return false;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ArrayList<String> subcmd = new ArrayList<>();
        if (cmd.getName().equalsIgnoreCase("rsadmin")) {
            if (args.length == 1) {
                subcmd.add("reload");
                subcmd.add("rl");
                subcmd.add("infos");
                subcmd.add("migratedb");
            } else if (args.length >= 2) {
                subcmd.add("");
            }
            Collections.sort(subcmd);
        }
        return subcmd;
    }
}
