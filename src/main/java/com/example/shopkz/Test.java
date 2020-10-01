package com.example.shopkz;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Document itemPage = Jsoup.connect("https://shop.kz/smartfony/filter/almaty-is-v_nalichii-or-ojidaem-or-dostavim/apply/").get();
        Elements itemElements = itemPage.select(".bx_catalog_item");
        for (Element itemElement : itemElements) {
//                    Double itemPrice = Double.valueOf(itemElement.attr("data-price"));
//                    Element a = itemElement.selectFirst("a.title");
         //   String itemText = itemElement.select(".bx_catalog_item_container").attr("data-product");

       //   String itemLink = itemElement.selectFirst(".bx_catalog_item_title a").absUrl("href");
      String itemText=itemElement.selectFirst(".bx_catalog_item_articul").text();
//            Double itemPrice = Double.valueOf(itemElement.selectFirst("bx_catalog_item_articul b:first-of-type").text());
//            System.out.println(itemLink);
          //  System.out.println(itemPrice);
            System.out.println(itemText);

        }
    }
}
