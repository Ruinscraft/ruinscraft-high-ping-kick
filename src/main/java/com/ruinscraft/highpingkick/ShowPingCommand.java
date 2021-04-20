package com.ruinscraft.highpingkick;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowPingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            if (!(sender instanceof Player)) {
                return false;
            }
            Player self = (Player) sender;
            showPing(sender, self);
        } else {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null || !target.isOnline()) {
                sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
            } else {
                // Check for vanished player
                if (sender instanceof Player) {
                    Player self = (Player) sender;
                    if (!self.canSee(target)) {
                        sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
                        return true;
                    }
                }

                showPing(sender, target);
            }
        }

        return true;
    }

    private static void showPing(CommandSender requester, Player target) {
        int ping;
        try {
            ping = HighPingKickPlugin.getPing(target);
        } catch (Exception e) {
            e.printStackTrace();
            requester.sendMessage(ChatColor.RED + "Error getting ping.");
            return;
        }
        requester.sendMessage(ChatColor.GOLD + "Ping for " + target.getName() + ": " + ChatColor.LIGHT_PURPLE + ping);
    }

}
