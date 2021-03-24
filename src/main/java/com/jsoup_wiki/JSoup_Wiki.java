/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jsoup_wiki;

import org.jsoup.Jsoup;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author m
 */
public class JSoup_Wiki {

    static int counter = 1;
    static Boolean found = false;
    static Optional<Entry<String, Integer>> href;
    static Map<String, Integer> mapa = new HashMap<>();
    static Map<String, Integer> mapa2 = new HashMap<>();
    static String adres;
    static long start;

    public static void main(String[] args) throws InterruptedException, IOException {

        start = System.currentTimeMillis();
        JSoup_Wiki.findRoute();
    }

    public static void findRoute() throws InterruptedException, IOException {
//Przypisywanie strony startowej
        //3 kroki
        Connection connect = Jsoup.connect("https://pl.wikipedia.org/wiki/Klosz").timeout(15000);
        //2 kroki
        //Connection con1 = Jsoup.connect("https://pl.wikipedia.org/wiki/Wieś").timeout(15000);
        //Losowa strona
        //Connection connect = Jsoup.connect("https://pl.wikipedia.org/wiki/Specjalna:Losowa_strona").timeout(15000);
        System.out.println("Startowa strona to: " + connect.get().baseUri());

//Przypisywanie strony docelowej
        //3 kroki
        Connection con1 = Jsoup.connect("https://pl.wikipedia.org/wiki/Wyłącznik").timeout(15000);
        //2 kroki
        //Connection con1 = Jsoup.connect("https://pl.wikipedia.org/wiki/Piwo").timeout(15000);
        //Losowa strona
        //Connection con1 = Jsoup.connect("https://pl.wikipedia.org/wiki/Specjalna:Losowa_strona").timeout(15000);
        adres = con1.get().baseUri();
        System.out.println("Docelowa strona to: " + adres);

//pobieranie i sprawdzanie linków ze strony startowej
        Document document = connect.get();
        Elements linki = document.select("#bodyContent div:not(.catlinks) a[href^=\"/wiki/\"]");
        for (Element e : linki) {
            if (e.attr("href").equals(adres)) {
                found = true;
                System.out.println("Znleziono w: " + counter + " krokach");
                break;
            }
            if (!(e.attr("href").contains(":") || e.attr("href").contains("/wiki/Wiki"))) {
                mapa.put("https://pl.wikipedia.org" + e.attr("href"), counter);
            }
        }

//przeszukiwanie aż do skutku kolejnych podstron
        while (!found) {
            System.out.println("Jestem w kroku: " + counter);
            //executorService.submit(new checkNextPage(mapa));
            //executorService.shutdown();
//            try {
//            executorService.awaitTermination(1, TimeUnit.HOURS);
//            } catch (InterruptedException e) {
//            e.printStackTrace();
//            }
            checkNextPage(mapa);
            mapa.putAll(mapa2);
            mapa2.clear();
        }
        long stop = System.currentTimeMillis();
        System.out.println("Time: " + (stop - start) + " ms");
        System.out.println("Koniec");
    }

    public static void checkNextPage(Map<String, Integer> mapaDoSprawdzenia) {
//warunek kończący szukanie - findAny dla wielu wątków
        href = mapaDoSprawdzenia.entrySet().stream().filter(a -> a.getKey().equals(adres)).findFirst();
        if (href.isPresent()) {
            found = true;
            System.out.println("Znalazłem! Potrzebna ilość kroków: " + href.get().getValue());
        } else {
//uzupełniamy mapa2 znalezionymi linkami niespełniającymi kryteriów zakończenia szukania
            mapaDoSprawdzenia.forEach((key, value) -> {
                //System.out.println("Kataloguje " + key + " " + value);
                try {
//pobieranie adresu ze spisu i pobieranie źródła
                    Connection connect2 = Jsoup.connect(key);
                    Document document2 = connect2.get();
//wybieranie linków
                    Elements fragment = document2.select("#bodyContent div:not(.catlinks) a[href^=\"/wiki/\"]");
//filtrowanie i dodawanie linków
                    for (Element e : fragment) {
//filtrowanie niechcianych elementów, czyli zawierających ":" lub "/wiki/Wiki"
                        if (!(e.attr("href").contains(":") || e.attr("href").contains("/wiki/Wiki"))) {
                            mapa2.put("https://pl.wikipedia.org" + e.attr("href"), value + 1);
                            counter = value + 1;
                        }
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            });
        }
    }
}
