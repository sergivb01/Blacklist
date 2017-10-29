package net.veilmc.blacklist;
import java.util.logging.Level;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import net.veilmc.blacklist.Utils.MySQL;

public class Core extends JavaPlugin implements Listener {

    private MySQL mysql;

    public void onEnable() {
        //Config
        saveDefaultConfig();
        //MySQL Stuff
        if(getConfig().getBoolean("MySQL.Enabled") == true) {
            mysql = new MySQL(getConfig().getString("MySQL.IP"), getConfig().getString("MySQL.Username"), getConfig().getString("MySQL.Password"), getConfig().getString("MySQL.Name"));
            mysql.createUsers();
        } else {
            getLogger().log(Level.INFO, "MySQL disabled. Blackling will not work!");
        }

        //Check
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for(Player online : Bukkit.getOnlinePlayers()) {
                    if(mysql.isPlayerBanned(online.getName())) {
                        String reason = mysql.getBannedReason(online.getName());
                        String staff = mysql.getStaff(online.getName());
                        String kickreason = "&7\n&cYou have been blacklisted from the VeilMC Network\n&7Connect to ts.veilmc.net to appeal this blacklist.\n&7".replaceAll("\\n", "\n");
                        if (reason != null) {
                            online.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickreason.replaceAll("%reason%", reason).replaceAll("%staff%", staff)));
                        } else {
                            online.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickreason.replaceAll("%reaso%", "No Reason Specified").replaceAll("%staff%", staff)));
                        }
                    }
                }
            }
        }, 50L, 50L);

        //Listeners
        Bukkit.getPluginManager().registerEvents(this, this);

        //Register Bungeecord Channel
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if(mysql.isPlayerBanned(e.getPlayer().getName()) == true) {
            String reason = mysql.getBannedReason(e.getPlayer().getName());
            String staff = mysql.getStaff(e.getPlayer().getName());
            String kickreason = "&7\n&cYou have been blacklisted from the VeilMC Network\n&7Connect to ts.veilmc.net to appeal this blacklist.\n&7".replaceAll("\\n", "\n");
            if (reason != null) {
                e.disallow(Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', kickreason.replaceAll("%reason%", reason).replaceAll("%staff%", staff)));
            } else {
                e.disallow(Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', kickreason.replaceAll("%reaso%", "No Reason Specified").replaceAll("%staff%", staff)));
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("blacklist")) {
            if(sender.hasPermission("riots.manager")) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /blacklist <player> <reason>");
                    return true;
                }
                Player player = Bukkit.getServer().getPlayer(args[0]);

                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }

                String reason = reasonBuilder.toString();

                if(mysql.isPlayerBanned(args[0])) {
                    sender.sendMessage(ChatColor.RED + "Error: That player is already blacklisted!");
                    return true;
                }

                String staff = sender.getName();
                String kickreason = "&7\n&cYou have been blacklisted from the VeilMC Network\n&7Connect to ts.veilmc.net to appeal this blacklist.\n&7".replaceAll("\\n", git init"\n");
                sender.sendMessage(ChatColor.GREEN + "Grabbing profile and IP...");
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        if(sender instanceof Player) {
                            Player staff1 = (Player) sender;
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + args[0] + " &ahas been successfully blacklisted"));
                            if(!reason.contains("-s")) {
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&l" + args[0] + " &cwas permanently blacklisted by &l" + staff + "&c!"));
                            } else {
                                reason.replaceAll("-s", "");
                                Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', "&c&l" + args[0] + " &cwas permanently blacklisted by &l" + staff + "&c!"), "rank.staff");
                            }
                            mysql.banPlayer(args[0], reason, staff1.getName());
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + args[0] + " &ahas been successfully blacklisted!"));
                            if(!reason.contains("-s")) {
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&l" + args[0] + " &cwas permanently blacklisted by &lConsole&c!"));
                            } else {
                                reason.replaceAll("-s", "");
                                Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', "&c&l" + args[0] + " &cwas permanently blacklisted by &lConsole&c!"), "rank.staff");
                            }
                            mysql.banPlayer(args[0], reason, "Console");
                        }
                        if(player != null) {
                            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickreason.replaceAll("\\n", "\n")));
                        }
                    }
                }, 15L);
            } else {
                sender.sendMessage(ChatColor.RED + "No permission.");
            }
        }
        if(cmd.getName().equalsIgnoreCase("unblacklist")) {
            if(sender.hasPermission("riots.manager")) {
                if(args.length != 1) {
                    sender.sendMessage(ChatColor.RED + "Usage: /unblacklist <player>");
                    return true;
                }

                Player player = Bukkit.getPlayer(args[0]);

                if(!mysql.isPlayerBanned(args[0])) {
                    sender.sendMessage(ChatColor.RED + "Error: That player is not blacklisted!");
                    return true;
                }

                mysql.unbanPlayer(args[0]);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + args[0] + " &ahas been successfully unblacklisted!"));
                Bukkit.broadcastMessage(ChatColor.RED.toString() + ChatColor.BOLD + sender.getName() + ChatColor.RED + " unblacklisted " + ChatColor.BOLD + args[0] + ChatColor.RED + "!");
            } else {
                sender.sendMessage(ChatColor.RED + "No permission.");
            }
        }

        if(cmd.getName().equalsIgnoreCase("checkblacklist")) {
            if(sender.hasPermission("rank.staff")) {
                if (args.length != 1) {
                    sender.sendMessage(ChatColor.RED + "Usage: /checkblacklist <player>");
                    return true;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m------------------------------------------"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l * &a" + args[0] + (mysql.isPlayerBanned(args[0]) ? "&e is blacklisted" : "&e is &anot blacklisted")));
                if (mysql.isPlayerBanned(args[0])) {
                    if (mysql.getBannedReason(args[0]) != null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l * &eReason: &a" + mysql.getBannedReason(args[0])));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l * &eReason: &aNone"));
                    }
                    if (mysql.getStaff(args[0]) != null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l * &eStaff: &a" + mysql.getStaff(args[0])));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l * &eStaff: &aNot available"));
                    }
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m------------------------------------------"));
            }
        }
        return false;
    }

}