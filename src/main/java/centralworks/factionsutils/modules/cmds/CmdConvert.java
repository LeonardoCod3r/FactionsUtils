package centralworks.factionsutils.modules.cmds;

import centralworks.factionsutils.modules.spawnerstorage.Convert;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class CmdConvert extends BukkitCommand {

    public CmdConvert() {
        super("storageconvert", "", "", Lists.newArrayList("strcv"));
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        if (s.hasPermission("storagespawners.admin")) {
            if (Convert.run()) s.sendMessage("§aO banco de dados foi convertido com sucesso.");
            else s.sendMessage("§cOcorreu um erro ao converter o banco de dados.");
        }
        return true;
    }
}
