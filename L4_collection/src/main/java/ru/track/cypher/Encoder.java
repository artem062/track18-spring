package ru.track.cypher;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Класс умеет кодировать сообщение используя шифр
 */
public class Encoder {

    /**
     * Метод шифрует символы текста в соответствие с таблицей
     * NOTE: Текст преводится в lower case!
     *
     * Если таблица: {a -> x, b -> y}
     * то текст aB -> xy, AB -> xy, ab -> xy
     *
     * @param cypherTable - таблица подстановки
     * @param text - исходный текст
     * @return зашифрованный текст
     */
    public String encode(@NotNull Map<Character, Character> cypherTable, @NotNull String text) {

        StringBuilder result = new StringBuilder();

        text = text.toLowerCase();

        for (int i = 0; i < text.length(); i++) {

            if (text.charAt(i) >= 'a' && text.charAt(i) <= 'z'){
                result.append(cypherTable.get(text.charAt(i)));
            } else {
                result.append(text.charAt(i));
            }
        }
        System.out.println(result);

        return result.toString();

    }
}
