package ru.track.cypher;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Вспомогательные методы шифрования/дешифрования
 */
public class CypherUtil {

    public static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyz";

    /**
     * Генерирует таблицу подстановки - то есть каждой буква алфавита ставится в соответствие другая буква
     * Не должно быть пересечений (a -> x, b -> x). Маппинг уникальный
     *
     * @return таблицу подстановки шифра
     */
    @NotNull
    public static Map<Character, Character> generateCypher() {

        Map<Character, Character> genCypher = new HashMap<>();

        List<Character> code = new ArrayList<>();

        for (int i = 0; i < SYMBOLS.length(); i++) {
            code.add(SYMBOLS.charAt(i));
        }

        Collections.shuffle(code);

        for (int i = 0; i < SYMBOLS.length(); i++) {
            genCypher.put(SYMBOLS.charAt(i), code.get(i));
        }
        return genCypher;
    }

}
