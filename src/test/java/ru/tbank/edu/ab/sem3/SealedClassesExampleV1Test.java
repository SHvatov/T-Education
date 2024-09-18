package ru.tbank.edu.ab.sem3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tbank.edu.ab.sem3.SealedClassesExampleV1.GameEventHandler;
import ru.tbank.edu.ab.sem3.SealedClassesExampleV1.Villager;
import ru.tbank.edu.ab.sem3.SealedClassesExampleV1.Zombie;

class SealedClassesExampleV1Test {

    @Test
    @DisplayName("Игра - V1")
    void test() {
        var handler = new GameEventHandler();

        var zombie = new Zombie(2, 100) {{
            setGameEventHandler(handler);
        }};

        var villager = new Villager() {{
            setGameEventHandler(handler);
        }};

        zombie.attackVillager(villager);

        System.out.println(villager);
        System.out.println(zombie);
    }
}
