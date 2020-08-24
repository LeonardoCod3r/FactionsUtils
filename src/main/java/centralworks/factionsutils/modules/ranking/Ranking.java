package centralworks.factionsutils.modules.ranking;

import com.massivecraft.factions.entity.Faction;
import org.bukkit.entity.Player;

public interface Ranking {

    void update();

    void openInventory(Player p);

    void remove(Faction faction);

    boolean has(Faction faction);

    Integer getPosition(Faction faction);
}
