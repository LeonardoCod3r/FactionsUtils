package centralworks.factionsutils.modules.cmds;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.lib.Configuration;
import centralworks.factionsutils.lib.NPCManager;
import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class CmdSetNpc extends BukkitCommand {

    public CmdSetNpc() {
        super("setnpctop", "", "§c/setnpctop (first/second/third)", Lists.newArrayList("setntop"));
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        if (s instanceof Player && s.hasPermission("faction.admin")){
            final Player p = ((Player) s);
            try {
                final Configuration cfg = Main.getConfiguration();
                final NPCManager npc = NPCManager.get();
                final Location l = p.getLocation();
                final String arg = args[0];
                switch (arg){
                    case "first":
                        cfg.saveLocationInConfig("npc1", l);
                        npc.setFirst(l);
                        p.sendMessage("§aLocalização definida com sucesso, aguarde o ranking atualizar.");
                        break;
                    case "second":
                        cfg.saveLocationInConfig("npc2", l);
                        npc.setSecond(l);
                        p.sendMessage("§aLocalização definida com sucesso, aguarde o ranking atualizar.");
                        break;
                    case "third":
                        cfg.saveLocationInConfig("npc3", l);
                        npc.setThird(l);
                        p.sendMessage("§aLocalização definida com sucesso, aguarde o ranking atualizar.");
                        break;
                    default:
                        break;
                }
            }catch (Exception e){
                p.sendMessage(getUsage());
            }
        }
        return true;
    }
}




