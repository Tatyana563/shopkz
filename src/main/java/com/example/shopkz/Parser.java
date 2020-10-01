package com.example.shopkz;

import com.example.shopkz.model.Item;
import com.example.shopkz.model.MainGroup;
import com.example.shopkz.model.Section;
import com.example.shopkz.repository.CategoryRepository;
import com.example.shopkz.repository.ItemRepository;
import com.example.shopkz.repository.MainGroupRepository;
import com.example.shopkz.repository.SectionRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Component
public class Parser {
    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private static final Set<String> SECTIONS = Set.of("Смартфоны и гаджеты", /*"Комплектующие",*/ "Ноутбуки и компьютеры", "Компьютерная периферия",
            "Оргтехника и расходные материалы", "Сетевое и серверное оборудование", "Телевизоры, аудио, фото, видео", "Бытовая техника и товары для дома", "Товары для геймеров");
    private static final String URL = "https://shop.kz/";

    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MainGroupRepository mainGroupRepository;
    @Autowired
    private ItemRepository itemRepository;

    public Parser() throws IOException {
    }

    @Scheduled(fixedDelay = 100000000)
    @Transactional
    public void getSections() throws IOException {

        Document newsPage = Jsoup.connect(URL).get();
        LOG.info("Получили главную страницу, ищем секции...");
        Elements sectionElements = newsPage.select(".bx-top-nav-container a");
        for (Element sectionElement : sectionElements) {
            String text = sectionElement.text();
            if (SECTIONS.contains(text)) {
                LOG.info("Получаем {}...", text);
                String sectionUrl = sectionElement.absUrl("href");
                Section section = sectionRepository.findOneByUrl(sectionUrl)
                        .orElseGet(() -> sectionRepository.save(new Section(text, sectionUrl)));

                Document groupPage = Jsoup.connect(sectionUrl).get();
                LOG.info("Получили {}, ищем группы...", text);
                Elements groupElements = groupPage.select(".bx_catalog_tile_title a");
                for (Element groupElement : groupElements) {
                    String groupUrl = groupElement.absUrl("href");
                    String groupText = groupElement.text();
                    LOG.info("Группа  {}", groupText);
                    MainGroup group = mainGroupRepository.findOneByUrl(sectionUrl)
                            .orElseGet(() -> mainGroupRepository.save(new MainGroup(groupText, groupUrl, section)));
                    Document itemPage = Jsoup.connect(groupUrl).get();
                    Elements itemElements = itemPage.select(".bx_catalog_item");
                    for (Element itemElement : itemElements) {
                        String itemLink = itemElement.selectFirst(".bx_catalog_item_title a").absUrl("href");
                        String itemText = itemElement.selectFirst(".bx_catalog_item_title a").text();
                        String itemDescription=itemElement.selectFirst(".bx_catalog_item_articul").text();

                        System.out.println(itemLink);
                      //  System.out.println(itemText);
                        LOG.info("Нашли товар {}/{}", itemLink);

                        Item item =  new Item();
                        item.setModel(itemText);
                        item.setUrl(itemLink);
                        item.setDescription(itemDescription);
                        itemRepository.save(item);
                    }
                }
            }
        }
    }
}

