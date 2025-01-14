package main.java.fr.farmeurimmo.reapersanction.gui;

import main.java.fr.farmeurimmo.reapersanction.storage.FilesManager;
import main.java.fr.farmeurimmo.reapersanction.storage.MessageManager;
import main.java.fr.farmeurimmo.reapersanction.users.Sanction;
import main.java.fr.farmeurimmo.reapersanction.users.User;
import main.java.fr.farmeurimmo.reapersanction.utils.TimeConverter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class HistoryGui {

    public static HistoryGui instance;
    public static String GUI_NAME = "§c§lHistory of %player% #%page%";

    public HistoryGui() {
        instance = this;
    }

    public String getPlayerFromGuiName(String guiName) {
        return guiName.split(" ")[2];
    }

    public void openHistoryGui(Player player, User targetUser, int page) {
        if (!player.hasPermission("mod")) {
            player.sendMessage(MessageManager.prefix + MessageManager.instance.getMessage("NoPermission"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, GUI_NAME.replace("%player%", targetUser.getName()).replace("%page%", String.valueOf(page + 1)));

        if (targetUser.getHistory().size() == 0) {
            player.sendMessage(MessageManager.prefix + MessageManager.instance.getMessage("PlayerNoHistoryAvailable"));
            return;
        }

        LinkedList<Sanction> historyForPage = getContentForPage(targetUser.getHistory(), page);

        for (int i = 0; i <= 45; i++) {
            if (historyForPage.size() == 0) break;
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            Sanction sanction = historyForPage.getLast();
            historyForPage.removeLast();
            meta.setDisplayName(TimeConverter.replaceArgs(FilesManager.instance.getFromConfigFormatted("History.displayname"),
                            sanction.getDuration(), targetUser.getName(), sanction.getBy(), sanction.getReason(), sanction.getAt(), sanction.getUntil())
                    .replace("%sanctiontype%", sanction.getSanctionTypeStr()));
            String lore = TimeConverter.replaceArgs(FilesManager.instance.getFromConfigFormatted("History.lore"),
                            sanction.getDuration(), targetUser.getName(), sanction.getBy(), sanction.getReason(), sanction.getAt(), sanction.getUntil())
                    .replace("%sanctiontype%", sanction.getSanctionTypeStr());
            ArrayList<String> loreList = new ArrayList<>(Arrays.asList(lore.split("%%")));
            meta.setLore(loreList);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        /*ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cPrevious page");
        back.setItemMeta(backMeta);
        inv.setItem(48, back);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName("§aNext page");
        next.setItemMeta(nextMeta);
        inv.setItem(50, next);*/

        GuiManager.instance.applyDoorsFromInvSize(inv);
        GuiManager.instance.applyGlass(inv);

        player.openInventory(inv);
    }

    public LinkedList<Sanction> getContentForPage(LinkedList<Sanction> history, int page) {
        LinkedList<Sanction> content = new LinkedList<>();
        int start = history.size() - 45;
        if (start < 0) start = 0;
        int end = history.size();
        for (int i = start; i < end; i++) {
            content.add(history.get(i));
        }
        return content;
    }
}
