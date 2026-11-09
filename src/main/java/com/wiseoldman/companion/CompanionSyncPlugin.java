package com.wiseoldman.companion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.EnumMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Model;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.Quest;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.game.ItemManager;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.http.api.loottracker.LootRecordType;
import net.runelite.client.util.Filepath;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
    name = "Wise Old Man Companion",
    internalName = "wise-old-man-companion",
    description = "Exports your own account observations to local files for an optional companion dashboard",
    configName = "wiseoldmancompanionplugin",
    enabledByDefault = false,
    tags = {"companion", "ironman", "bank", "goals", "local", "export"}
)
public class CompanionSyncPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private Gson gson;
    @Inject private ItemManager itemManager;
    private boolean itemsDirty;
    private boolean bankDirty;
    private long accountHash;
    private long itemSequence;
    private JsonArray observedBank;
    private String bankObservedAt;
    private AtomicReference<JsonObject> pendingItems;
    private JsonArray previousOffers;
    private boolean offersKnown;
    private final EnumMap<Skill, SkillXpProgress> skillProgress = new EnumMap<>(Skill.class);
    private JsonObject previousSkillData;
    private int xpReadyTick;
    private GoalActivity goalActivity;
    private JsonObject questStates;
    private boolean questsDirty;
    private int nextQuestTick;
    private int lastAccountCaptureTick;

    private int[] previousEquipment;
    private int exportCountdown = -1;
    private int exportRetries;
    private String changedAt;
    private String session;
    private long sequence;
    private ExecutorService writer;
    private AtomicReference<JsonObject> pending;
    private int[] previousLevels;
    private int[] previousAutocast;
    private int weaponId = -1;
    private int checkedWeaponId = -1;
    private int checkedAtTick = -100;
    private BlowpipeDartData loadedDarts;

    @Override
    protected void startUp()
    {
        previousEquipment = null;
        previousLevels = null;
        previousAutocast = null;
        weaponId = -1;
        loadedDarts = null;
        exportCountdown = -1;
        exportRetries = 0;
        session = UUID.randomUUID().toString();
        sequence = 0;
        pending = new AtomicReference<>();
        pendingItems = new AtomicReference<>();
        previousOffers = null;
        offersKnown = false;
        skillProgress.clear();
        previousSkillData = null;
        xpReadyTick = client.getTickCount() + 3;
        goalActivity = new GoalActivity();
        questStates = null;
        questsDirty = true;
        nextQuestTick = 0;
        lastAccountCaptureTick = -100;
        itemsDirty = true;
        bankDirty = true;
        accountHash = 0;
        itemSequence = 0;
        observedBank = null;
        bankObservedAt = null;
        writer = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "wiseoldman-snapshot-writer");
            thread.setDaemon(true);
            return thread;
        });
        TextureColor.setClient(client);
        log.info("[Wise Old Man] Combined character sync started.");
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            queueExport();
        }
    }

    @Override
    protected void shutDown()
    {
        previousEquipment = null;
        exportCountdown = -1;
        pending.set(null);
        pendingItems.set(null);
        writer.shutdownNow();
        TextureColor.setClient(null);
        log.info("[Wise Old Man] Combined character sync stopped.");
    }

    private void queueExport()
    {
        // One game tick lets RuneLite apply the new appearance before capture.
        exportCountdown = 1;
        exportRetries = 0;
        changedAt = Instant.now().toString();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() != GameState.LOGGED_IN)
        {
            previousEquipment = null;
            previousAutocast = null;
            loadedDarts = null;
            checkedWeaponId = -1;
            exportCountdown = -1;
            previousOffers = null;
            offersKnown = false;
            skillProgress.clear();
            previousSkillData = null;
            questsDirty = true;
        }
        else
        {
            xpReadyTick = client.getTickCount() + 3;
            itemsDirty = true;
            bankDirty = true;
            queueExport();
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        questsDirty = true;
    }

    @Subscribe
    public void onLootReceived(LootReceived event)
    {
        if (client.getGameState() != GameState.LOGGED_IN || accountHash == 0
            || client.getAccountHash() != accountHash || event.getName() == null || event.getItems() == null
            || (event.getType() != LootRecordType.NPC && event.getType() != LootRecordType.EVENT))
        {
            return;
        }
        goalActivity.loot(Text.removeTags(event.getName()), event.getType() == LootRecordType.NPC,
            event.getAmount(), event.getItems(), itemManager::canonicalize);
        itemsDirty = true;
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (client.getGameState() != GameState.LOGGED_IN || event.getSkill() == Skill.OVERALL)
        {
            return;
        }
        SkillXpProgress progress = skillProgress.get(event.getSkill());
        if (progress == null || client.getTickCount() < xpReadyTick)
        {
            skillProgress.put(event.getSkill(), new SkillXpProgress(event.getXp()));
        }
        else
        {
            progress.observe(event.getXp(), Instant.now().toString());
        }
        itemsDirty = true;
    }

    @Subscribe
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event)
    {
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            offersKnown = true;
            itemsDirty = true;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() == InventoryID.INV || event.getContainerId() == InventoryID.WORN
            || event.getContainerId() == InventoryID.BANK)
        {
            itemsDirty = true;
            bankDirty |= event.getContainerId() == InventoryID.BANK;
        }
        if (event.getContainerId() != InventoryID.WORN || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        ItemContainer equipment = event.getItemContainer();
        if (equipment == null)
        {
            return;
        }
        Item[] items = equipment.getItems();
        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        int nextWeaponId = weapon == null ? -1 : weapon.getId();
        if (weaponId != nextWeaponId)
        {
            weaponId = nextWeaponId;
            loadedDarts = null;
            checkedWeaponId = -1;
        }
        int[] ids = Arrays.stream(items).mapToInt(item -> item == null ? -1 : item.getId()).toArray();
        if (!Arrays.equals(previousEquipment, ids))
        {
            previousEquipment = ids;
            queueExport();
            log.debug("[Wise Old Man] Equipment changed; snapshot queued.");
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!BlowpipeDartData.isBlowpipe(event.getItemId()))
        {
            return;
        }
        String option = Text.removeTags(event.getMenuOption()).toLowerCase(Locale.ROOT);
        if (option.equals("check"))
        {
            checkedWeaponId = event.getItemId();
            checkedAtTick = client.getTickCount();
        }
        else if (option.equals("unload") || option.equals("uncharge") || option.equals("use"))
        {
            // A reload may change dart type without changing the weapon item ID.
            loadedDarts = null;
            checkedWeaponId = event.getItemId();
            checkedAtTick = client.getTickCount();
            queueExport();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
        {
            return;
        }
        String message = Text.removeTags(event.getMessage());
        if (client.getGameState() == GameState.LOGGED_IN && accountHash != 0
            && client.getAccountHash() == accountHash && goalActivity.message(message))
        {
            itemsDirty = true;
        }
        if (checkedWeaponId == weaponId && client.getTickCount() - checkedAtTick <= 3)
        {
            BlowpipeDartData parsed = BlowpipeDartData.parse(message);
            if (parsed != null)
            {
                loadedDarts = parsed;
                checkedWeaponId = -1;
                queueExport();
                return;
            }
        }
        if (BlowpipeDartData.isBlowpipe(weaponId) && message.toLowerCase(Locale.ROOT).contains("blowpipe")
            && (message.contains("run out") || message.contains("contains no darts") || message.contains("needs to be charged")))
        {
            loadedDarts = null;
            queueExport();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            captureAccountItems();
            int[] autocast = {client.getVarbitValue(VarbitID.AUTOCAST_SET),
                client.getVarbitValue(VarbitID.AUTOCAST_SPELL), client.getVarpValue(VarPlayerID.AUTOCAST_SPELL_OBJ)};
            if (!Arrays.equals(previousAutocast, autocast))
            {
                previousAutocast = autocast;
                log.debug("[Wise Old Man] Autocast selection: set={}, spell={}, object={}", autocast[0], autocast[1], autocast[2]);
                if (exportCountdown < 0)
                {
                    queueExport();
                }
            }
            Skill[] skills = {Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS,
                Skill.MAGIC, Skill.RANGED, Skill.PRAYER, Skill.MINING, Skill.HERBLORE};
            int[] levels = Arrays.stream(skills).mapToInt(client::getRealSkillLevel).toArray();
            if (!Arrays.equals(previousLevels, levels))
            {
                previousLevels = levels;
                if (exportCountdown < 0)
                {
                    queueExport();
                }
            }
        }
        if (exportCountdown < 0 || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        if (--exportCountdown > 0)
        {
            return;
        }
        exportCountdown = -1;
        Player player = client.getLocalPlayer();
        Model model = player == null ? null : player.getModel();
        ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
        if (model == null || equipment == null)
        {
            if (++exportRetries <= 3)
            {
                exportCountdown = 1;
            }
            return;
        }
        try
        {
            Item currentWeapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
            int currentWeaponId = currentWeapon == null ? -1 : currentWeapon.getId();
            if (weaponId != currentWeaponId)
            {
                weaponId = currentWeaponId;
                loadedDarts = null;
            }
            JsonObject snapshot = CharacterSnapshot.capture(model);
            JsonObject gear = new JsonObject();
            for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
            {
                String name = slot.name().toLowerCase(Locale.ROOT);
                switch (slot)
                {
                    case AMULET: name = "neck"; break;
                    case GLOVES: name = "hands"; break;
                    case BOOTS: name = "feet"; break;
                    case ARMS: case HAIR: case JAW: continue;
                    default: break;
                }
                Item item = equipment.getItem(slot.getSlotIdx());
                if (item != null && item.getId() >= 0)
                {
                    JsonObject value = new JsonObject();
                    value.addProperty("id", item.getId());
                    value.addProperty("name", client.getItemDefinition(item.getId()).getName());
                    gear.add(name, value);
                }
            }
            snapshot.add("equipment", gear);
            JsonObject combat = new JsonObject();
            JsonObject skills = new JsonObject();
            skills.addProperty("atk", client.getRealSkillLevel(Skill.ATTACK));
            skills.addProperty("str", client.getRealSkillLevel(Skill.STRENGTH));
            skills.addProperty("def", client.getRealSkillLevel(Skill.DEFENCE));
            skills.addProperty("hp", client.getRealSkillLevel(Skill.HITPOINTS));
            skills.addProperty("magic", client.getRealSkillLevel(Skill.MAGIC));
            skills.addProperty("ranged", client.getRealSkillLevel(Skill.RANGED));
            skills.addProperty("prayer", client.getRealSkillLevel(Skill.PRAYER));
            skills.addProperty("mining", client.getRealSkillLevel(Skill.MINING));
            skills.addProperty("herblore", client.getRealSkillLevel(Skill.HERBLORE));
            combat.add("skills", skills);
            combat.addProperty("schemaVersion", 2);
            JsonObject autocast = new JsonObject();
            int spellObject = client.getVarpValue(VarPlayerID.AUTOCAST_SPELL_OBJ);
            boolean enabled = client.getVarbitValue(VarbitID.AUTOCAST_SET) != 0
                && client.getVarbitValue(VarbitID.AUTOCAST_SPELL) != 0 && spellObject > 0;
            String spellName = enabled ? AutocastSpellNames.resolve(spellObject, client.getItemDefinition(spellObject).getName()) : null;
            if (spellName == null || spellName.equals("null") || spellName.isBlank())
            {
                enabled = false;
            }
            autocast.addProperty("enabled", enabled);
            if (enabled)
            {
                autocast.addProperty("name", Text.removeTags(spellName));
            }
            combat.add("autocast", autocast);
            if (BlowpipeDartData.isBlowpipe(weaponId))
            {
                JsonObject blowpipe = new JsonObject();
                blowpipe.addProperty("weaponId", weaponId);
                blowpipe.addProperty("verified", loadedDarts != null);
                if (loadedDarts != null)
                {
                    blowpipe.addProperty("dartId", loadedDarts.dartId);
                    blowpipe.addProperty("quantityAtCheck", loadedDarts.quantity);
                    blowpipe.addProperty("scalesAtCheck", loadedDarts.scales);
                }
                combat.add("blowpipe", blowpipe);
            }
            snapshot.add("combat", combat);
            snapshot.addProperty("revision", session + "-" + (++sequence));
            snapshot.addProperty("changed_at", changedAt);
            snapshot.addProperty("captured_at", Instant.now().toString());
            AtomicReference<JsonObject> queue = pending;
            queue.set(snapshot);
            writer.execute(() -> writePending(queue));
        }
        catch (Exception error)
        {
            log.error("[Wise Old Man] Snapshot capture failed.", error);
        }
    }

    private void writePending(AtomicReference<JsonObject> queue)
    {
        writePending(queue, "snapshot.json");
    }

    private JsonArray accountContainer(int id)
    {
        ItemContainer container = client.getItemContainer(id);
        return AccountItemTotals.encode(container == null ? null : container.getItems(), itemManager::canonicalize,
            itemId -> Text.removeTags(itemManager.getItemComposition(itemId).getName()));
    }

    private void captureAccountItems()
    {
        if (client.getTickCount() - lastAccountCaptureTick >= 30)
        {
            itemsDirty = true;
        }
        long nextHash = client.getAccountHash();
        if (nextHash == 0)
        {
            return;
        }
        if (accountHash != nextHash)
        {
            accountHash = nextHash;
            goalActivity = new GoalActivity();
            questStates = null;
            questsDirty = true;
            nextQuestTick = 0;
            skillProgress.clear();
            previousSkillData = null;
            xpReadyTick = client.getTickCount() + 3;
            previousOffers = null;
            observedBank = null;
            bankObservedAt = null;
            itemsDirty = true;
            bankDirty = true;
        }
        if (questsDirty && client.getTickCount() >= Math.max(xpReadyTick, nextQuestTick))
        {
            JsonObject observedQuests = new JsonObject();
            try
            {
                for (Quest quest : Quest.values())
                {
                    observedQuests.addProperty(quest.getName(), quest.getState(client).name());
                }
            }
            catch (RuntimeException error)
            {
                observedQuests = null;
                log.debug("[Wise Old Man] Quest state not ready; will retry.", error);
            }
            if (observedQuests != null && !observedQuests.equals(questStates))
            {
                questStates = observedQuests;
                itemsDirty = true;
            }
            questsDirty = observedQuests == null;
            nextQuestTick = client.getTickCount() + 3;
        }
        JsonObject skillData = new JsonObject();
        for (Skill skill : Skill.values())
        {
            if (skill == Skill.OVERALL)
            {
                continue;
            }
            int xp = client.getSkillExperience(skill);
            SkillXpProgress progress = skillProgress.computeIfAbsent(skill, key -> new SkillXpProgress(xp));
            if (client.getTickCount() < xpReadyTick)
            {
                progress.xp = xp;
                progress.lastGain = 0;
                progress.gainAt = null;
            }
            else
            {
                progress.observe(xp, Instant.now().toString());
            }
            JsonObject value = new JsonObject();
            value.addProperty("level", client.getRealSkillLevel(skill));
            value.addProperty("xp", xp);
            value.addProperty("lastGain", progress.lastGain);
            value.addProperty("gainAt", progress.gainAt);
            skillData.add(skill.getName(), value);
        }
        if (!skillData.equals(previousSkillData))
        {
            previousSkillData = skillData;
            itemsDirty = true;
        }
        JsonArray offers = ExchangeOfferData.encode(client.getGrandExchangeOffers(),
            id -> Text.removeTags(itemManager.getItemComposition(id).getName()));
        if (offers.size() > 0)
        {
            offersKnown = true;
        }
        if (!offers.equals(previousOffers))
        {
            previousOffers = offers;
            itemsDirty = true;
        }
        net.runelite.api.widgets.Widget bankWidget = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
        boolean bankOpen = bankWidget != null && !bankWidget.isHidden();
        if (bankOpen && bankDirty && client.getItemContainer(InventoryID.BANK) != null)
        {
            observedBank = accountContainer(InventoryID.BANK);
            bankObservedAt = Instant.now().toString();
            bankDirty = false;
            itemsDirty = true;
        }
        if (!itemsDirty || client.getItemContainer(InventoryID.INV) == null
            || client.getItemContainer(InventoryID.WORN) == null)
        {
            return;
        }
        JsonObject value = new JsonObject();
        value.addProperty("schemaVersion", 1);
        value.addProperty("accountKey", UUID.nameUUIDFromBytes(("wom-account-" + accountHash)
            .getBytes(StandardCharsets.UTF_8)).toString());
        value.addProperty("revision", session + "-items-" + (++itemSequence));
        value.addProperty("captured_at", Instant.now().toString());
        value.add("worn", accountContainer(InventoryID.WORN));
        value.add("inventory", accountContainer(InventoryID.INV));
        value.addProperty("bankKnown", observedBank != null);
        value.add("bank", observedBank == null ? new JsonArray() : observedBank);
        value.addProperty("bankObservedAt", bankObservedAt);
        value.addProperty("offersKnown", offersKnown);
        value.add("offers", offers);
        value.add("skills", skillData);
        value.add("activity", goalActivity.encode(id -> Text.removeTags(itemManager.getItemComposition(id).getName())));
        if (questStates != null)
        {
            value.add("quests", questStates);
        }
        itemsDirty = false;
        lastAccountCaptureTick = client.getTickCount();
        AtomicReference<JsonObject> queue = pendingItems;
        queue.set(value);
        writer.execute(() -> writePending(queue, "account-items.json"));
    }

    private void writePending(AtomicReference<JsonObject> queue, String filename)
    {
        JsonObject snapshot = queue.getAndSet(null);
        if (snapshot == null || Thread.currentThread().isInterrupted())
        {
            return;
        }
        try
        {
            Filepath folder = getPluginDirectory();
            folder.createDirectories();
            Filepath temp = folder.join(filename + ".tmp");
            temp.write(gson.toJson(snapshot));
            temp.moveTo(folder.join(filename), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            log.debug("[Wise Old Man] Snapshot {} written.", snapshot.get("revision"));
        }
        catch (Exception error)
        {
            log.error("[Wise Old Man] Snapshot write failed.", error);
        }
    }
}
