package ru.tbank.edu.ab.sem3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class SealedClassesExampleV2Test {

    @Test
    @DisplayName("Игра - V2")
    void test() {
        var handler = new SealedClassesExampleV2.GameEventHandler();

        var zombie = new SealedClassesExampleV2.Zombie(2) {{
            setAttack(100);
            setGameEventHandler(handler);
        }};

        var villager = new SealedClassesExampleV2.Villager() {{
            setGameEventHandler(handler);
        }};

        zombie.attackVillager(villager);

        System.out.println(villager);
        System.out.println(zombie);
    }

}
