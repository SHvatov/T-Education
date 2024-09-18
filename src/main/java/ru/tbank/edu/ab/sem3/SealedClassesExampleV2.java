package ru.tbank.edu.ab.sem3;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;


public class SealedClassesExampleV2 {

    public interface GameEventProducer {

        void setGameEventHandler(GameEventHandler handler);

    }

    public interface NonPlayableCharacter extends GameEventProducer {

    }

    @Data
    public static class Villager implements NonPlayableCharacter {

        private GameEventHandler gameEventHandler;
        private boolean dead = false;
        private int health = 100;

        public void sufferDamageFromZombie(Zombie zombie) {
            health -= zombie.getAttack();
            if (health <= 0) {
                gameEventHandler.handle(new VillagerDiedEvent(this, zombie));
            }
        }

    }

    @Data
    public static class Zombie implements NonPlayableCharacter {

        private GameEventHandler gameEventHandler;
        private final int maxLevel;
        private int level = 1;
        private int attack;

        public void attackVillager(Villager villager) {
            villager.sufferDamageFromZombie(this);
        }

    }

    public sealed interface GameEvent {

    }

    @Data
    public static final class VillagerDiedEvent implements GameEvent {

        private final Villager victim;
        private final Zombie killer;

    }

    @Data
    public static sealed class ZombieEvolvedEvent implements GameEvent {

        private final Zombie zombie;

    }

    public static final class ZombieEvolvedToMaxLevelEvent extends ZombieEvolvedEvent {

        public ZombieEvolvedToMaxLevelEvent(Zombie zombie) {
            super(zombie);
        }

    }

    public static class GameEventHandler {

        boolean supports(GameEvent event) {
            return true;
        }

        void handle(GameEvent event) {
            switch (event) {
                case VillagerDiedEvent villagerDiedEvent -> {
                    villagerDiedEvent.getVictim().setDead(true);
                    handle(new ZombieEvolvedEvent(villagerDiedEvent.getKiller()));
                }
                case ZombieEvolvedToMaxLevelEvent zombieEvolvedToMaxLevelEvent -> {
                    var zombie = zombieEvolvedToMaxLevelEvent.getZombie();
                    zombie.setAttack(zombie.getAttack() * 2);
                    zombie.setLevel(1);
                }
                case ZombieEvolvedEvent zombieEvolvedEvent -> {
                    var zombie = zombieEvolvedEvent.getZombie();
                    zombie.setLevel(zombie.getLevel() + 1);
                    if (zombie.getLevel() >= zombie.getMaxLevel()) {
                        handle(new ZombieEvolvedToMaxLevelEvent(zombie));
                    }
                }
            }
        }

    }

    @RequiredArgsConstructor
    public static class GameEventHandlerV2 extends GameEventHandler {

        private final List<GameEventHandler> handlers;

        @Override
        void handle(GameEvent event) {
            handlers.stream()
                    .filter(it -> it.supports(event)).findFirst()
                    .ifPresent(it -> it.handle(event));
        }

    }

}
