package dev.mizio.mcPlugins.ocTPA.services;

import dev.mizio.mcPlugins.ocTPA.repositories.EconomyRepository;

import org.bukkit.entity.Player;

public class EconomyService {

    protected EconomyRepository repository;

    public EconomyService() {
        repository = new EconomyRepository();
    }

    public void closeService() {
        repository = null;
    }

    public String getCurrencySymbol() {
        return repository.getCurrencySymbol();
    }

    public String getCostOfTeleportation(boolean isReturnRequest) {
        return repository.getCostOfTeleportation(isReturnRequest);
    }

    public String getActuallyAccountBalance(Player player) {
        return repository.getActuallyAccountBalance(player);
    }

    public boolean isFreeTeleport(boolean isReturnRequest) {
        return repository.isFreeTeleport(isReturnRequest);
    }

    public boolean hasEnough(Player player, boolean isReturnRequest) {
        return repository.hasEnoughMoney(player, isReturnRequest);
    }

    public boolean withdraw(Player player, boolean isReturnRequest) {
        return repository.withdrawMoney(player, isReturnRequest);
    }

    //TODO: po testach skasować
    public boolean deposit(Player player, double amount) {
        return repository.depositMoney(player, amount);
    }
}
