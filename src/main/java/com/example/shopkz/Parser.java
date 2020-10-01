package com.example.shopkz;

import com.example.shopkz.model.Category;
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

        Document indexPage = Jsoup.connect(URL).get();
        LOG.info("Получили главную страницу, ищем секции...");
        Elements sectionElements = indexPage.select(".bx-top-nav-container ul.bx-nav-list-1-lvl li.bx-nav-1-lvl");
        for (Element sectionElement : sectionElements) {
            Element sectionAnchor = sectionElement.selectFirst(">a");
            String text = sectionAnchor.text();
            if (SECTIONS.contains(text)) {
                LOG.info("Получаем {}...", text);
                String sectionUrl = sectionAnchor.absUrl("href");
                Section section = sectionRepository.findOneByUrl(sectionUrl)
                        .orElseGet(() -> sectionRepository.save(new Section(text, sectionUrl)));

//                Document groupPage = Jsoup.connect(sectionUrl).get();
                LOG.info("Получили {}, ищем группы...", text);
                Elements groupElements = sectionElement.select("ul.bx-nav-list-2-lvl li.bx-nav-2-lvl");
                for (Element groupElement : groupElements) {
                    Element groupAnchor = groupElement.selectFirst(">a");
                    String groupText = groupAnchor.text();
                    LOG.info("Группа  {}", groupText);
                    MainGroup group = mainGroupRepository.findOneByUrl(sectionUrl)
                            .orElseGet(() -> mainGroupRepository.save(new MainGroup(groupText, null, section)));

                    Elements categoryElements = groupElement.select("ul.bx-nav-list-3-lvl li.bx-nav-3-lvl");
                    for (Element categoryElement : categoryElements) {
                        Element categoryAnchor = categoryElement.selectFirst(">a");
                        String categoryLink = categoryAnchor.absUrl("href");
                        String categoryText = categoryAnchor.text();

//                        String itemLink = itemElement.selectFirst(".bx_catalog_item_title a").absUrl("href");
//                        String itemText = itemElement.selectFirst(".bx_catalog_item_title a").text();
//                        String itemDescription=itemElement.selectFirst(".bx_catalog_item_articul").text();

                        LOG.info("\tКатегория  {}", categoryText);
                        if (!categoryRepository.existsByUrl(sectionUrl)) {
                            categoryRepository.save(new Category(categoryText, categoryLink, group));
                        }
                    }
                }
            }
        }
    }
}

