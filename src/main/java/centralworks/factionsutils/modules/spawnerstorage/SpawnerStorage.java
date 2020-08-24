package centralworks.factionsutils.modules.spawnerstorage;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.database.*;
import centralworks.factionsutils.lib.*;
import centralworks.factionsutils.modules.commons.InventoryMethods;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.*;
import com.massivecraft.massivecore.ps.PS;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import rush.sistemas.spawners.MobSpawner;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@Table(name = "spawnerstorage")
public class SpawnerStorage implements Comparable<SpawnerStorage>, Identifier {

    @Key(autoIncrement = false)
    @DataType(dataType = "VARCHAR(16)")
    private String tagOwner;
    private LinkedHashMap<String, Integer> spawnersData = Maps.newLinkedHashMap();
    private List<Object[]> logs = Lists.newArrayList();
    private Long delayStartedIn = 0L;

    public SpawnerStorage(String tagOwner) {
        this.tagOwner = tagOwner;
    }

    @Override
    public String getIdentifier() {
        return tagOwner;
    }

    public LinkedHashMap<EntityType, Integer> getStorage() {
        final LinkedHashMap<EntityType, Integer> map = Maps.newLinkedHashMap();
        spawnersData.forEach((s, i) -> map.put(EntityType.valueOf(s), i));
        return map;
    }

    public void setStorage(LinkedHashMap<EntityType, Integer> map) {
        final LinkedHashMap<String, Integer> result = Maps.newLinkedHashMap();
        map.forEach((entityType, integer) -> result.put(entityType.name(), integer));
        this.spawnersData = result;
    }

    public void addSpawnerData(EntityType entityType, Integer integer) {
        final LinkedHashMap<EntityType, Integer> data = getStorage();
        if (data.containsKey(entityType)) data.replace(entityType, data.get(entityType) + integer);
        else data.put(entityType, integer);
        setStorage(data);
    }

    public void removeSpawnerData(EntityType entityType) {
        final LinkedHashMap<EntityType, Integer> data = getStorage();
        data.remove(entityType);
        setStorage(data);
    }

    public void removeSpawnerData(EntityType entityType, Integer amount) {
        final LinkedHashMap<EntityType, Integer> data = getStorage();
        if (data.containsKey(entityType)) {
            data.replace(entityType, Math.max(0, data.get(entityType) - amount));
            setStorage(data);
        }
    }

    public boolean hasInStorage(EntityType entityType, Integer amount) {
        return getStorage().get(entityType) >= amount;
    }

    public Faction getFaction() {
        return FactionColl.get().getByTag(tagOwner);
    }

    public Boolean existsFaction() {
        return getFaction() != null;
    }

    public void putDelay() {
        setDelayStartedIn(System.currentTimeMillis());
    }

    public boolean inDelay() {
        final Date date = new Date();
        date.setHours(8);
        return getDelayStartedIn() + date.getTime() > System.currentTimeMillis();
    }

    public List<Object[]> getLogsOrder() {
        final List<Object[]> novaLista = Lists.newArrayList(logs);
        Collections.reverse(novaLista);
        return novaLista;
    }

    private void putLog(Object... data) {
        final List<Object[]> logs = Lists.newArrayList(getLogs());
        if (logs.size() == 14) logs.remove(0);
        logs.add(data);
        setLogs(logs);
    }

    public Double getStoragePrice() {
        final Double[] value = {0D};
        getStorage().forEach((entityType, integer) -> {
            final Double price = Main.getSpawnersDetails().get(entityType).getPrice();
            value[0] += price * integer;
        });
        return value[0];
    }

    public Double getStoragePrice(EntityType entityType) {
        return getStorage().get(entityType) * Main.getSpawnersDetails().get(entityType).getPrice();
    }

    public Double getPriceAll() {
        return getPlacedPrice() + getStoragePrice();
    }

    public Double getPriceAll(EntityType entityType) {
        return getPlacedPrice(entityType) + getStoragePrice(entityType);
    }

    public Double getPlacedPrice() {
        final Double[] value = {0D};
        getPlacedSpawners().forEach(location -> {
            final EntityType entityType = ((CreatureSpawner) location.getBlock().getState()).getSpawnedType();
            value[0] += Main.getSpawnersDetails().get(entityType).getPrice();
        });
        return value[0];
    }

    public Double getPlacedPrice(EntityType entityType) {
        return getPlacedSpawners(entityType).size() * Main.getSpawnersDetails().get(entityType).getPrice();
    }

    public Integer getSpawnersAmount() {
        return getPlacedAmount() + getStorageAmount();
    }

    public Integer getSpawnersAmount(EntityType entityType) {
        return getPlacedAmount(entityType) + getStorageAmount(entityType);
    }

    public Integer getPlacedAmount() {
        return getPlacedSpawners().size();
    }

    public Integer getPlacedAmount(EntityType entityType) {
        return getPlacedSpawners(entityType).size();
    }

    public Integer getStorageAmount() {
        final Integer[] value = {0};
        getStorage().forEach((entityType, integer) -> value[0] += integer);
        return value[0];
    }

    public Integer getStorageAmount(EntityType entityType) {
        return getStorage().get(entityType);
    }

    public boolean hasSpawnerInStorage(EntityType entityType) {
        return getStorage().containsKey(entityType) && getStorage().get(entityType) > 0;
    }

    public boolean hasSpawnerInStorage() {
        return getStorageAmount() > 0;
    }

    public boolean hasSpawnerInTerrains() {
        return getPlacedAmount() > 0;
    }

    public boolean hasSpawner(EntityType entityType) {
        return hasSpawnerInStorage(entityType) && hasSpawnerInTerrains(entityType);
    }

    public boolean hasSpawnerInTerrains(EntityType entityType) {
        return getPlacedSpawners(entityType).size() > 0;
    }

    public HashMap<EntityType, Integer> getRelationPlaced() {
        final HashMap<EntityType, Integer> map = Maps.newHashMap();
        for (Location location : getPlacedSpawners()) {
            final EntityType entityType = ((CreatureSpawner) location.getBlock().getState()).getSpawnedType();
            if (map.containsKey(entityType)) map.replace(entityType, map.get(entityType) + 1);
            else map.put(entityType, 1);
        }
        return map;
    }

    public Map<EntityType, Integer> getStorageFix() {
        return getStorage().entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public HashMap<EntityType, Integer> getRelationToAllSpawners() {
        final HashMap<EntityType, Integer> map = Maps.newHashMap(getStorageFix());
        getRelationPlaced().forEach((entityType, integer) -> {
            if (map.containsKey(entityType)) map.replace(entityType, map.get(entityType) + integer);
            else map.put(entityType, integer);
        });
        return map;
    }

    public List<Location> getPlacedSpawners() {
        final List<Location> locations = Lists.newArrayList();
        if (BoardColl.get().hasClaimed(getFaction())) {
            for (PS ps : BoardColl.get().getChunks(getFaction())) {
                final Chunk chunk = ps.asBukkitWorld().getChunkAt(ps.asBukkitChunk().getX(), ps.asBukkitChunk().getZ());
                for (BlockState tileEntity : chunk.getTileEntities()) {
                    if (tileEntity.getBlock().getType() == Material.AIR)
                        continue;
                    if (tileEntity.getType() == Material.MOB_SPAWNER) locations.add(tileEntity.getLocation());
                }
            }
        }
        return locations;
    }

    public List<Location> getPlacedSpawners(EntityType entityType) {
        return getPlacedSpawners().stream().filter(location -> ((CreatureSpawner) location.getBlock().getState()).getSpawnedType() == entityType).collect(Collectors.toList());
    }

    public boolean run(Movement movement, Object... data) {
        switch (movement) {
            case STOCK:
                try {
                    final Player p = Bukkit.getPlayer("" + data[0]);
                    final MPlayer mPlayer = MPlayerColl.get().get(p);
                    if (next(mPlayer, null)) {
                        final EntityType entityType = EntityType.valueOf("" + data[1]);
                        final int amount = Integer.parseInt("" + data[2]);
                        addSpawnerData(entityType, amount);
                        putLog(p.getName(), entityType, movement, amount);
                        save();
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            case REMOVE:
                try {
                    final Player p = Bukkit.getPlayer("" + data[0]);
                    final MPlayer mPlayer = MPlayerColl.get().get(p);
                    if (next(mPlayer, Rel.OFFICER)) {
                        final EntityType entityType = EntityType.valueOf("" + data[1]);
                        final int amount = Integer.parseInt("" + data[2]);
                        if (!hasInStorage(entityType, amount)) return false;
                        removeSpawnerData(entityType, amount);
                        new InventoryMethods(p, MobSpawner.get(entityType.name(), 1), amount).andGive();
                        putLog(p.getName(), entityType, movement, amount);
                        save();
                        return true;
                    }
                    return false;
                } catch (Exception ignored) {
                    return false;
                }
            case COLLECT_ALL:
                try {
                    final Player p = Bukkit.getPlayer("" + data[0]);
                    final MPlayer mPlayer = MPlayerColl.get().get(p);
                    if (next(mPlayer, Rel.LEADER)) {
                        final HashMap<EntityType, Integer> placed = getRelationPlaced();
                        if (placed.isEmpty()) return false;
                        placed.forEach((entityType, integer) -> run(Movement.STOCK, p.getName(), entityType, integer));
                        getPlacedSpawners().forEach(location -> location.getBlock().setType(Material.AIR));
                        save();
                        return true;
                    }
                    return false;
                } catch (Exception ignored) {
                    return false;
                }
            case STOCK_ALL:
                try {
                    final Player p = Bukkit.getPlayer("" + data[0]);
                    final MPlayer mPlayer = MPlayerColl.get().get(p);
                    if (next(mPlayer, Rel.MEMBER)) {
                        stockFromInventory(p);
                        save();
                        return true;
                    }
                    return false;
                } catch (Exception ignored) {
                    return false;
                }
            default:
                return false;
        }
    }

    public boolean next(MPlayer mPlayer, Rel rel) {
        final Player p = mPlayer.getPlayer();
        if (mPlayer.hasFaction() && mPlayer.getFaction().getTag().equals(getTagOwner())) {
            if (rel != null) {
                switch (rel) {
                    case OFFICER:
                        if (mPlayer.getRole() == Rel.OFFICER || mPlayer.getRole() == Rel.LEADER) return true;
                        p.closeInventory();
                        p.sendMessage("§cVocê não possui permissões suficientes.");
                        return false;
                    case LEADER:
                        if (mPlayer.getRole() == Rel.LEADER) return true;
                        p.closeInventory();
                        p.sendMessage("§cVocê não possui permissões suficientes.");
                        return false;
                    default:
                        return true;
                }
            } else return true;
        }
        p.closeInventory();
        p.sendMessage("§cOcorreu um erro! Talvez a facção foi deletada... Você foi expulso?!");
        return false;
    }

    public void stockFromInventory(Player p) {
        final HashMap<EntityType, Integer> map = Maps.newHashMap();
        for (ItemStack content : p.getInventory().getContents()) {
            try {
                final net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(content);
                final NBTTagCompound tag = nmsCopy.getTag();
                if (tag.hasKey("Entity")) {
                    final EntityType type = EntityType.valueOf(tag.getString("Entity"));
                    if (map.containsKey(type)) map.replace(type, map.get(type) + content.getAmount());
                    else map.put(type, content.getAmount());
                }
            } catch (Exception ignored) {
            }
        }
        if (map.isEmpty()) return;
        map.forEach((entityType, integer) -> {
            new InventoryMethods(p, MobSpawner.get(entityType.name(), 1), integer).andRemove();
            run(Movement.STOCK, p.getName(), entityType, integer);
        });
    }

    public void save() {
        QueriesSync.supply(this).commit();
    }

    public void delete() {
        QueriesSync.supply(this).delete();
    }

    public void deepSave() {
        QueriesSync.supply(this).commit();
    }

    public void deepDelete() {
        QueriesSync.supply(this).delete();
    }

    @Override
    public int compareTo(SpawnerStorage o) {
        return o.getPriceAll().compareTo(getPriceAll());
    }

    // Inventário principal
    public void openInventory(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        if (next(mPlayer, Rel.MEMBER)) {
            final InventoryBuilder inventory = new InventoryBuilder(Main.get(), 3, "§8Geradores");
            inventory.clear();
            inventory.setCancellable(true);
            inventory.setItem(10, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eInformações:").lore(
                    "§fRanking: §7" + Main.getSpawnerRanking().getPosition(getFaction()) + "º",
                    "§fValor total: §2R$ §7" + FormatBalance.format(getPriceAll()),
                    "  §f§l• §fColocados: §2R$ §7" + FormatBalance.format(getPlacedPrice()),
                    "  §f§l• §fArmazenados: §2R$ §7" + FormatBalance.format(getStoragePrice())
            ).setSkullUrl("http://textures.minecraft.net/texture/badc048a7ce78f7dad72a07da27d85c0916881e5522eeed1e3daf217a38c1a"));
            inventory.setItem(12, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§aSpawners").setSkullUrl("http://textures.minecraft.net/texture/647e2e5d55b6d04943519bed2557c6329e33b60b909dee8923cd88b115210").lore(
                    !hasSpawnerInStorage() ? "§cA sua facção não possui nenhum spawner armazenado." :
                            "§7Clique para acessar a lista de geradores armazenados."
            ).onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> {
                if (!spawnerStorage.getStorageFix().isEmpty()) spawnerStorage.openListSpawnersInventory(p, 1);
            })));
            inventory.setItem(13, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§5Moderar geradores.").setSkullUrl("http://textures.minecraft.net/texture/1b8fe1c44acbeeb918d38bc42d550bedd5c3dd049889fd9eeea1160ab8b6a").lore(
                    "§7Clique para acessar o menu de moderação de geradores."
            ).onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> spawnerStorage.openUtilsMenu(p))));
            inventory.setItem(14, new Item(Material.BOOK).name("§bLogs").lore(
                    getLogs().isEmpty() ? "§cNão há nenhuma movimentação recente." :
                            getFaction().getLeader() != mPlayer ? "§cApenas o líder pode acessar as logs de movimentação de spawners." :
                                    "§7Clique para acessar as logs de movimentação de spawners."
            ).onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> {
                if (!spawnerStorage.getLogs().isEmpty() && spawnerStorage.getFaction().getLeader() == mPlayer)
                    openLogsMenu(p);
            })));
            inventory.open(p);
        }
    }

    //Abrir as logs de operações
    public void openLogsMenu(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        if (next(mPlayer, Rel.LEADER)) {
            final InventoryBuilder inventory = new InventoryBuilder(Main.get(), 4, "§8Geradores");
            inventory.clear();
            inventory.setCancellable(true);
            if (logs.isEmpty()) {
                inventory.setItem(22, new Item(Material.WEB).name("§cSem resultados").lore("§7Não foi encontrado nenhum spawner armazenado.").onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> spawnerStorage.openInventory(p))));
            } else {
                final List<Integer> slots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25);
                for (int count = 0; slots.size() > count && logs.size() > count; count++){
                    final Object[] data = getLogsOrder().get(count);
                    final OfflinePlayer user = Bukkit.getOfflinePlayer("" + data[0]);
                    final EntityType entityType = EntityType.valueOf("" + data[1]);
                    final Movement movement = Movement.valueOf(String.valueOf(data[2]));
                    final int amount = Integer.parseInt(data[3].toString().replace(".0", ""));
                    inventory.setItem(slots.get(count), new Item(Material.BOOK).name("§eLog §f#§7" + (count + 1)).lore(
                            "§fMovimentação: §7" + movement.getName(),
                            "§fUsuário: §7" + user.getName(),
                            "§fCriatura: §7" + EntityName.valueOf(entityType).getName(),
                            "§fQuantidade: §7" + amount
                    ));
                }
            }
            inventory.open(p);
        }
    }

    //Abrir o menu de funções do sistema de geradores
    public void openUtilsMenu(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        if (next(mPlayer, Rel.MEMBER)) {
            final InventoryBuilder inventory = new InventoryBuilder(Main.get(), 3, "§8Geradores");
            inventory.clear();
            inventory.setCancellable(true);
            inventory.setItem(10, new Item(Material.CHEST).name("§eArmazenar").lore("§7Clique para armazenar os spawners.").onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> spawnerStorage.openAddSpawnersTaskInventory(p))));
            inventory.setItem(11, new Item(Material.ENDER_CHEST).name("§eArmazenar todos").lore("§7Clique para armazenar todos os spawners do seu inventário.").onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> {
                p.closeInventory();
                Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> {
                    if (spawnerStorage.run(Movement.STOCK_ALL, p.getName()))
                        p.sendMessage("§aTodos os spawners do seu inventário foram armazenados.");
                    else p.sendMessage("§cVocê não possui nenhum spawner no seu inventário.");
                });
            })));
            inventory.setItem(15, new Item(Material.DIAMOND_PICKAXE).hideAttributes().name("§aColetar todos").lore(
                    getPlacedSpawners().isEmpty() ? "§cNão há nenhum spawner colocado nos terrenos de sua facção." :
                            mPlayer.getRole() == Rel.LEADER ? "§7Clique para coletar todos os geradores colocados." : "§cApenas um líder pode remover todos spawners placed."
            ).onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> {
                p.closeInventory();
                if (inDelay()) {
                    p.sendMessage("§cAguarde para poder usar esse sistema novamente.");
                    return;
                }
                if (mPlayer.getRole() == Rel.LEADER && !spawnerStorage.getPlacedSpawners().isEmpty()) {
                    if (spawnerStorage.getFaction().isInAttack()) {
                        p.sendMessage("§cSua facção está em ataque.");
                        return;
                    }
                    if (spawnerStorage.run(Movement.COLLECT_ALL, p.getName())) {
                        p.sendMessage("§aTodos os geradores colocados foram armazenados.");
                        spawnerStorage.putDelay();
                        spawnerStorage.save();
                    } else p.sendMessage("§cSua facção não possui nenhum spawner colocado.");
                }
            })));
            inventory.open(p);
        }
    }

    public void openAddSpawnersTaskInventory(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        if (next(mPlayer, Rel.MEMBER)) {
            final InventoryBuilder inventory = new InventoryBuilder(Main.get(), 3, "§8Geradores");
            inventory.clear();
            inventory.setCancellable(true);
            inventory.setItem(13, new Item(Material.PAPER).name("§eInfo: ").lore("§7Clique em spawners do seu inventário para armazena-los."));
            inventory.onClickPlayerInv(event -> QueriesSync.supply(this).queue(spawnerStorage -> {
                try {
                    final ItemStack itemStack = event.getCurrentItem();
                    final net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);
                    final NBTTagCompound tag = nmsCopy.getTag();
                    if (tag.hasKey("Entity")) {
                        final EntityType entityType = EntityType.valueOf(tag.getString("Entity"));
                        spawnerStorage.run(Movement.STOCK, p.getName(), entityType, itemStack.getAmount());
                        event.getWhoClicked().getInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    }
                } catch (Exception ignored) {
                }
            }));
            inventory.open(p);
        }
    }

    //Abrir lista de spawners
    public void openListSpawnersInventory(Player p, int page) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        if (next(mPlayer, Rel.OFFICER)) {
            final InventoryBuilder inventory = new InventoryBuilder(Main.get(), 3, "§8Geradores");
            inventory.clear();
            inventory.setCancellable(true);
            final HashMap<EntityType, Integer> spawners = Maps.newHashMap(getStorageFix());
            if (spawners.isEmpty()) {
                inventory.setItem(13, new Item(Material.WEB).name("§cSem resultados").lore("§7Não foi encontrado nenhum spawner armazenado.").onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> spawnerStorage.openInventory(p))));
            } else {
                final List<Integer> slots = Arrays.asList(11, 12, 13, 14, 15);
                final double pages = Math.ceil(spawners.size() / 5.0);
                if (page != pages)
                    inventory.setItem(16, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eAvançar").lore("§7Clique para ir a página " + (page + 1)).setSkullUrl("http://textures.minecraft.net/texture/715445da16fab67fcd827f71bae9c1d2f90c73eb2c1bd1ef8d8396cd8e8").onClick(inventoryClickEvent -> QueriesSync.supply(this).queue(spawnerStorage -> spawnerStorage.openListSpawnersInventory(p, page + 1))));
                if (page != 1)
                    inventory.setItem(10, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eVoltar").lore("§7Clique para ir a página " + (page - 1)).setSkullUrl("http://textures.minecraft.net/texture/eed78822576317b048eea92227cd85f7afcc44148dcb832733baccb8eb56fa1").onClick(inventoryClickEvent -> QueriesSync.supply(this).queue(spawnerStorage -> spawnerStorage.openListSpawnersInventory(p, page - 1))));
                final int calculate = page - 1;
                final List<EntityType> subListSpawners = Lists.newArrayList(spawners.keySet()).subList(calculate, Math.min(calculate + 5, spawners.size()));
                int count = 0;
                while (count < subListSpawners.size()) {
                    final EntityType entityType = subListSpawners.get(count);
                    final Integer amount = getStorageAmount(entityType);
                    final Double price = getStoragePrice(entityType);
                    inventory.setItem(slots.get(count), Main.getHead(entityType)
                            .lore("§fValor: §2R$ §7" + FormatBalance.format(Main.getSpawnersDetails().get(entityType).getPrice()),
                                    "§fValor total: §2R$ §7" + FormatBalance.format(price),
                                    "§fQuantidade: §7" + amount)
                            .addLines(mPlayer.getRole() == Rel.OFFICER || mPlayer.getRole() == Rel.LEADER ? Arrays.asList("", "§7Clique para coletar os spawners.") : null)
                            .onClick(event -> QueriesSync.supply(this).queue(spawnerStorage -> {
                                if (next(mPlayer, Rel.OFFICER)) {
                                    p.closeInventory();
                                    EditGeradoresC.getInstance().addPlayer(p, entityType, spawnerStorage);
                                    Arrays.asList(
                                            "            §aFactions - Geradores",
                                            "",
                                            "§aDigite a quantidade de geradores que deseja remover: "
                                    ).forEach(p::sendMessage);
                                }
                            })));
                    count++;
                }
            }
            inventory.open(p);
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class EditGeradoresC implements Listener {

        private static EditGeradoresC instance;
        private final HashMap<Player, Object[]> map = Maps.newHashMap();

        public static EditGeradoresC getInstance() {
            return instance == null ? instance = new EditGeradoresC() : instance;
        }

        @EventHandler
        public void quit(final PlayerQuitEvent e) {
            final Player p = e.getPlayer();
            if (this.hasPlayer(p)) {
                this.removePlayer(p);
            }
        }

        public void addPlayer(final Player p, final Object... objects) {
            if (this.hasPlayer(p)) getMap().replace(p, objects);
            else getMap().put(p, objects);
        }

        public void removePlayer(final Player p) {
            getMap().remove(p);
        }

        public boolean hasPlayer(final Player p) {
            return getMap().containsKey(p);
        }

        public Object[] get(final Player p) {
            return getMap().get(p);
        }

    }
}
