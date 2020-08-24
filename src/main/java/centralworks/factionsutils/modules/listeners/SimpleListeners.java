package centralworks.factionsutils.modules.listeners;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.skills.mining.MiningManager;
import com.gmail.nossr50.util.BlockUtils;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.player.UserManager;
import com.google.common.collect.Lists;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SimpleListeners implements Listener {

    public static List<BlockFace> directions;
    public static List<Material> blocksProtection;
    public static List<Material> materialList;
    public List<Material> list;


    public SimpleListeners() {
        directions = Arrays.asList(BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN);
        blocksProtection = Arrays.asList(Material.ENDER_STONE, Material.OBSIDIAN, Material.BEDROCK);
        materialList = Arrays.asList(Material.SAND, Material.GRAVEL, Material.ANVIL);
        list = Lists.newArrayList(Material.COBBLESTONE, Material.STONE, Material.BEDROCK);
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        final Block block = e.getBlock();
        final Player p = e.getPlayer();
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        if (mPlayer.hasFaction() && materialList.contains(block.getType()) && mPlayer.getFaction().isInAttack()) {
            final Chunk chunk = p.getLocation().getChunk();
            if (BoardColl.get().getChunks(mPlayer.getFaction()).stream().anyMatch(ps -> {
                final Chunk asBukkitChunk = ps.asBukkitChunk();
                return asBukkitChunk.getX() == chunk.getX() && asBukkitChunk.getZ() == chunk.getZ();
            })) {
                p.sendMessage("§cSua facção está sobre ataque.");
                e.setCancelled(true);
                return;
            }
        }
        if (mPlayer.hasFaction() && block.getType() == Material.MOB_SPAWNER && directions.stream().anyMatch(blockFace -> blocksProtection.contains(block.getRelative(blockFace).getType()))) {
            p.sendMessage("§cNão pode colocar spawners do lado de blocos de proteção.");
            e.setCancelled(true);
        } else if (mPlayer.hasFaction() && blocksProtection.contains(block.getType()) && directions.stream().anyMatch(blockFace -> block.getRelative(blockFace).getType() == Material.MOB_SPAWNER)) {
            p.sendMessage("§cNão pode colocar blocos de proteção do lado de spawners.");
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void drop(BlockBreakEvent e) {
        final Block block = e.getBlock();
        if (block.getWorld().getName().equalsIgnoreCase("mina")) {
            if (list.stream().anyMatch(material -> block.getDrops().stream().anyMatch(itemStack -> itemStack.getType() == material))) {
                final BlockState blockState = e.getBlock().getState();
                final Player player = e.getPlayer();
                if (!UserManager.hasPlayerDataKey(player) || player.getGameMode() == GameMode.CREATIVE) {
                    return;
                }
                final McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
                final ItemStack heldItem = player.getInventory().getItemInHand();
                if (BlockUtils.affectedBySuperBreaker(blockState) && ItemUtils.isPickaxe(heldItem) && SkillType.MINING.getPermissions(player)) {
                    final MiningManager miningManager = mcMMOPlayer.getMiningManager();
                    miningManager.miningBlockCheck(blockState);
                }
                e.setCancelled(true);
                block.setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void hit(EntityDamageEvent e) {
        final Entity entity = e.getEntity();
        if (entity.getType() == EntityType.CREEPER && e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
            e.setCancelled(true);
    }
}
