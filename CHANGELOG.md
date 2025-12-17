# Plugin changelog

Spis najważniejszych zmian we wskazanych wersjach pluginu.

## `1.1.0` - 16.12.2025

Większość zmian dotyczy zgłoszonych uwag w [#1](https://github.com/mizpablito/ocTPA/issues/1)

⚠️ Zalecane wygenerowanie nowego pliku `config.yml`

### Zmieniono

- Podbito wersję zależności `paper-api` do wersji `1.21.11-R0.1-SNAPSHOT`
- Logikę komend, główna komenda to `/teleportacja`, w konfiguracji możliwość uruchomienia komend aliasów, usunięcie anglojęzycznych nazw komend.
- Przeniesiono hover z informacją o pluginie z prefiksów do komendy `/teleportacja pomoc`
- Zunifikowano wiadomość akceptacji/odrzucenia teleportacji.
- Pomniejsze poprawki kodu.

### Dodano

- Sprawdzanie bezpieczeństwa lokalizacji docelowej teleportacji.
- Nowe tłumaczenia.
- Komenda `/teleportacja pomoc` zwraca listę komend, koszt teleportacji.

## `1.0.1` - 06.12.2025

### Zmieniono

- Poprawiono logikę komunikatów.
- Dodano komunikat o pobraniu i stanie konta gracza po teleportacji.

## `1.0.0` - 11.11.2025 

Inicjalizacja i wstępny kod pluginu.

Użyto następujących zależności:

- [litecommands](https://docs.rollczi.dev/documentation/litecommands/what-is-litecommands.html)
- [litecommands-adventure](https://docs.rollczi.dev/documentation/litecommands/extensions/kyori-adventure/adventure-kyori.html)
- [adventure-text-minimessage](https://docs.papermc.io/adventure/minimessage/)
- [EconomyCore](https://github.com/TheNewEconomy/EconomyCore)
- [lombok](https://projectlombok.org/)
