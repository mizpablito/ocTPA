package dev.mizio.mcPlugins.ocTPA.repositories;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import dev.mizio.mcPlugins.ocTPA.exceptions.InitEconomyRepoException;
import dev.mizio.mcPlugins.ocTPA.utils.StringUtil;
import net.tnemc.core.TNECore;
import net.tnemc.core.account.PlayerAccount;
import net.tnemc.core.account.holdings.HoldingsEntry;
import net.tnemc.core.api.TNEAPI;
import net.tnemc.core.currency.Currency;
import net.tnemc.core.transaction.TransactionResult;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EconomyRepository {

    private final String msgLoggerPrefix = "[EconomyRepository] ";
    private MainOcTPA plugin;
    private boolean ecoWork = false;
    private TNEAPI ecoAPI;
    private double tpAmount;
    private double tpReturnAmount;
    private String currencyName;
    private Currency currency;

    public EconomyRepository() {
        plugin = MainOcTPA.instance();

        try {
            init();
        } catch (InitEconomyRepoException e) {
            ecoAPI = null;
            ecoWork = false;
//            currency = null;

            plugin.getLogger().warning(msgLoggerPrefix + "Błąd inicjowania repo ekonomii: " + e.getMessage());
            if (plugin.getPluginConfig().isPluginDebug()) e.printStackTrace();
        }

        if (!ecoWork) {
            plugin.getLogger().warning(msgLoggerPrefix + "!! Nie zainicjowano repo ekonomii!");
        }
    }



    private void init() throws InitEconomyRepoException {
        boolean tneLoaded = plugin.getServer().getPluginManager().getPlugin("TheNewEconomy") != null;

        // configs
        boolean usingCost = plugin.getPluginConfig().isTpSetting_cost_enabled();
        currencyName      = plugin.getPluginConfig().getTpSetting_cost_currency();
        tpAmount          = plugin.getPluginConfig().getTpSetting_cost_amount();
        tpReturnAmount    = plugin.getPluginConfig().getTpSetting_cost_return_amount();

        if (!usingCost) {
            throw new InitEconomyRepoException("Koszty teleportacji jest wyłączony w konfiguracji.");
        }

        if (!tneLoaded) {
            throw new InitEconomyRepoException("Plugin TheNewEconomy nie jest załadowany na serwerze!");
        }

        // jeżeli tu jesteśmy, to koszt włączony i TNE istnieje - ładujemy API
        ecoAPI = TNECore.api();

        if (!loadCurrent(currencyName)) {
            throw new InitEconomyRepoException("Nie znaleziono waluty: " + currencyName);
        }

        if (tpAmount <= 0) {
            plugin.getLogger().warning(msgLoggerPrefix + "Koszt teleportacji nie jest dodatni lub jest równy zero (" + tpAmount + ")");
        }

        if (tpReturnAmount <= 0) {
            plugin.getLogger().warning(msgLoggerPrefix + "Koszt powrotu nie jest dodatni lub jest równy zero (" + tpReturnAmount + ")");
        }

        ecoWork = true;
        plugin.getLogger().info(msgLoggerPrefix + "Załadowano Economy Repository");
    }

    private boolean isEconomyRepoLoaded() {
        plugin.debugInfo("isEconomyRepoLoaded( ecoWork: " + ecoWork
                + " | ecoAPI: " + ((ecoAPI == null) ? "is null" : ecoAPI.getClass().getSimpleName())
                + " currencyPluginUse: " + ((currency == null) ? "is null": currency.getIdentifier()) + " )");
        return (ecoWork || ecoAPI != null || currency != null);
    }

    private boolean loadCurrent(String currencyName) {
        Collection<Currency> currencies = ecoAPI.getCurrencies();
        for (Currency currency : currencies) {
            if (currency.getIdentifier().equalsIgnoreCase(currencyName)) {
                plugin.debugInfo(msgLoggerPrefix + "Znaleziono wskazaną walutę " + currency.getIdentifier());
                this.currency = currency;
                return true;
            }
        }
        plugin.debugInfo(msgLoggerPrefix + "Nie znaleziono waluty: " + currencyName);
        return false;
    }

    public boolean hasEnoughMoney(Player player, boolean isReturnRequest) {
        if (!isEconomyRepoLoaded()) {
            if (plugin.getPluginConfig().isPluginDebug()) {
                player.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("economy-service-not-loaded")
                ));
            }
            plugin.debugInfo(msgLoggerPrefix + "Repozytorium nie zostało zainicjowane!");
            return true; // traktujemy jako darmowe
        }

        double amount = isReturnRequest ? tpReturnAmount : tpAmount;

        boolean result = ecoAPI.hasHoldings(
                player.getUniqueId().toString(),
                player.getWorld().getName(),
                currency.getIdentifier(),
                BigDecimal.valueOf(amount)
        );

        plugin.debugInfo(msgLoggerPrefix + "Sprawdzenie środków dla " + player.getName() +
                ": wymagane=" + amount + ", wynik=" + result);

        return result;
    }

    public boolean withdrawMoney(Player player, boolean isReturnRequest) {
        if (!isEconomyRepoLoaded()) {
            if (plugin.getPluginConfig().isPluginDebug()) {
                player.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("economy-service-not-loaded")
                ));
            }
            plugin.debugInfo(msgLoggerPrefix + "Repozytorium nie zostało zainicjowane!");
            return false;
        }

        double amount = isReturnRequest ? tpReturnAmount : tpAmount;

        TransactionResult result = ecoAPI.addHoldings(
                player.getUniqueId().toString(),
                player.getWorld().getName(),
                currency.getIdentifier(),
                BigDecimal.valueOf(-amount),
                plugin.getName()
        );

        plugin.debugInfo(msgLoggerPrefix + "Pobranie środków z konta " + player.getName() +
                ": kwota=" + amount +
                ", status=" + result.isSuccessful() +
                ", message=" + result.getMessage());

        return result.isSuccessful();
    }

    //TODO: po testach skasować
    public boolean depositMoney(Player player, double amount) {
        if (!isEconomyRepoLoaded()) {
            if (plugin.getPluginConfig().isPluginDebug()) {
                player.sendMessage(StringUtil.textFormatting(
                        plugin.getPluginConfig().getTranslations("economy-service-not-loaded")
                ));
            }
            plugin.debugInfo(msgLoggerPrefix + "Repozytorium nie zostało zainicjowane!");
            return false;
        }

        TransactionResult result = ecoAPI.addHoldings(
                player.getUniqueId().toString(),
                player.getWorld().getName(),
                currency.getIdentifier(),
                BigDecimal.valueOf(amount),
                plugin.getName()
        );

        plugin.debugInfo(msgLoggerPrefix + "Wpłata środków dla " + player.getName() +
                ": kwota=" + amount + ", status=" + result.isSuccessful());
        player.sendMessage(msgLoggerPrefix + "Wpłata środków dla " + player.getName() +
                ": kwota=" + amount + ", status=" + result.isSuccessful());

        return result.isSuccessful();
    }

    public boolean isFreeTeleport(boolean isReturnRequest) {
        if (!isEconomyRepoLoaded()) {
            plugin.debugInfo(msgLoggerPrefix + "Teleportacja jest darmowa, ponieważ Repo nie zostało poprawnie zainicjowane!");
            return true;
        }
        double cost = isReturnRequest
                ? tpReturnAmount
                : tpAmount;

        return cost <= 0;
    }

    public String getCurrencySymbol() {
        return currency.getSymbol();
    }

    public String getCostOfTeleportation(boolean isReturnRequest) {
        return (isReturnRequest ? tpReturnAmount : tpAmount) + " " + currency.getSymbol();
    }

    public String getActuallyAccountBalance(Player player) {
        if (!isEconomyRepoLoaded()) {
            return "brak danych";
        }

        try {
            BigDecimal balance = ecoAPI.getHoldings(
                    player.getUniqueId().toString(),
                    player.getWorld().getName(),
                    currency.getIdentifier()
            );

            return balance.toPlainString() + currency.getSymbol();

        } catch (Exception e) {
            plugin.getLogger().warning(msgLoggerPrefix +
                    "Błąd podczas pobierania salda gracza " + player.getName() +
                    ": " + e.getMessage());

            if (plugin.getPluginConfig().isPluginDebug()) {
                e.printStackTrace();
            }

            return "brak danych";
        }
    }
}
