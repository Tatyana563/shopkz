package com.example.shopkz;

import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test2 {
    public static final String URL = "https://shop.kz/";

    public static void main(String[] args) {
        String text = "background-image: url('//static.shop.kz/upload/iblock/855/149297_1.jpg')";

//        Pattern pattern = Pattern.compile("(\\()(.*)\\)");
//        Matcher matcher = pattern.matcher(text);
//        while (matcher.find()) {
//            System.out.println("FOUND");


        Pattern pattern = Pattern.compile("(.*\\:)");
        Matcher matcher = pattern.matcher(URL);
        while (matcher.find()) {
            String http = matcher.group(1);
          //  String https2=matcher.group(2);
            System.out.println(matcher.group(1)/*+matcher.group(2)*/);

    String  imageResult= text.replace("background-image: url",http);
    String result = imageResult.replace("('","");
    String result2=result.replace("')","");
        System.out.println(result2);


        }
    }
}

//background-image: url('//static.shop.kz/upload/iblock/855/149297_1.jpg')