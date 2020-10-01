package com.example.shopkz;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    private static final Pattern PRODUCT_NUMBER_PATTERN = Pattern.compile("Артикул:\\s+(\\d+)\\s+(.*)");

    public static void main2(String[] args) throws IOException {
        Document itemPage = Jsoup.connect("https://shop.kz/smartfony/filter/almaty-is-v_nalichii-or-ojidaem-or-dostavim/apply/").get();
        Elements itemElements = itemPage.select(".bx_catalog_item");
        for (Element itemElement : itemElements) {
            Element itemContainer = itemElement.selectFirst(".bx_catalog_item_container");
            String itemData = itemContainer.attr("data-product");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(itemData);
            System.out.println(jsonNode.get("price").asDouble());
            System.out.println(jsonNode.get("name").asText());
            String itemDescription = itemElement.selectFirst(".bx_catalog_item_articul").text();
            Matcher matcher = PRODUCT_NUMBER_PATTERN.matcher(itemDescription);
            if (matcher.find()) {
                String number = matcher.group(1);
                String description = matcher.group(2);
                System.out.println("articul: " + Double.valueOf(number));
                System.out.println("description: " + description);
            }

//


//                    Double itemPrice = Double.valueOf(itemElement.attr("data-price"));
//                    Element a = itemElement.selectFirst("a.title");
            //   String itemText = itemElement.select(".bx_catalog_item_container").attr("data-product");
            //   String itemLink = itemElement.selectFirst(".bx_catalog_item_title a").absUrl("href");
//      String itemText=itemElement.selectFirst(".bx_catalog_item_articul").text();
//            Double itemPrice = Double.valueOf(itemElement.selectFirst("bx_catalog_item_articul b:first-of-type").text());
//            System.out.println(itemLink);
            //  System.out.println(itemPrice);
//            System.out.println(itemText);

        }
    }

    public static void main(String[] args) throws IOException {
        Jsoup.connect("https://shop.kz/smartfony/filter/almaty-is-v_nalichii-or-ojidaem-or-dostavim/apply/?PAGEN_1=2")
                .cookie("sectionSort", "new")
                .get();

        String html2 = Jsoup
                .connect("https://shop.kz/smartfony/filter/almaty-is-v_nalichii-or-ojidaem-or-dostavim/apply/?PAGEN_1=2")
                .cookie("sectionSort", "price-asc")
                .get().html();

        System.out.println("asd");


    }
}
