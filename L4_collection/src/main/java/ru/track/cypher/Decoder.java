package ru.track.cypher;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Decoder {

    // Расстояние между A-Z -> a-z
    public static final int SYMBOL_DIST = 32;

    private Map<Character, Character> cypher;

    /**
     * Конструктор строит гистограммы открытого домена и зашифрованного домена
     * Сортирует буквы в соответствие с их частотой и создает обратный шифр Map<Character, Character>
     *
     * @param domain - текст по кторому строим гистограмму языка
     */
    public Decoder(@NotNull String domain, @NotNull String encryptedDomain) {
        Map<Character, Integer> domainHist = createHist(domain);
        Map<Character, Integer> encryptedDomainHist = createHist(encryptedDomain);

        cypher = new LinkedHashMap<>();

        List<Character> domainList = new LinkedList<>(domainHist.keySet());
        List<Character> encryptedList = new LinkedList<>(encryptedDomainHist.keySet());

        for (int i = 0; i < domainList.size(); ++i){
            cypher.put(encryptedList.get(i), domainList.get(i));
        }
    }

    public Map<Character, Character> getCypher() {
        return cypher;
    }

    /**
     * Применяет построенный шифр для расшифровки текста
     *
     * @param encoded зашифрованный текст
     * @return расшифровка
     */
    @NotNull
    public String decode(@NotNull String encoded) {
        StringBuilder result = new StringBuilder();

        encoded = encoded.toLowerCase();

        for (int i = 0; i < encoded.length(); ++i){
            if (encoded.charAt(i) >= 'a' && encoded.charAt(i) <= 'z'){
                result.append(cypher.get(encoded.charAt(i)));
            } else {
                result.append(encoded.charAt(i));
            }
        }

        return result.toString();
    }

    /**
     * Считывает входной текст посимвольно, буквы сохраняет в мапу.
     * Большие буквы приводит к маленьким
     *
     *
     * @param text - входной текст
     * @return - мапа с частотой вхождения каждой буквы (Ключ - буква в нижнем регистре)
     * Мапа отсортирована по частоте. При итерировании на первой позиции наиболее частая буква
     */
    @NotNull
    Map<Character, Integer> createHist(@NotNull String text) {

        Map <Character, Integer> hist = new LinkedHashMap<>();

        text = text.toLowerCase();

        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) >= 'a' && text.charAt(i) <= 'z') {
                if (hist.containsKey(text.charAt(i))) {
                    hist.put(text.charAt(i), hist.get(text.charAt(i)) + 1);
                } else {
                    hist.put(text.charAt(i), 1);
                }
            }
        }

        List<Map.Entry<Character, Integer>> listForHist = new ArrayList<>(hist.entrySet());
        Collections.sort(listForHist, (o1, o2) -> o2.getValue() - o1.getValue());

        Map<Character, Integer> sortedHist = new LinkedHashMap<>();

        for (int i = 0; i < listForHist.size(); ++i){
            sortedHist.put(listForHist.get(i).getKey(), listForHist.get(i).getValue());
        }

        return sortedHist;
    }

}