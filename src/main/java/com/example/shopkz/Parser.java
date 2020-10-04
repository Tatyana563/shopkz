package com.example.shopkz;

import com.example.shopkz.model.Category;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Parser {
    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private static final Set<String> SECTIONS = Set.of("Смартфоны и гаджеты", "Комплектующие", "Ноутбуки и компьютеры", "Компьютерная периферия",
            "Оргтехника и расходные материалы", "Сетевое и серверное оборудование", "Телевизоры, аудио, фото, видео", "Бытовая техника и товары для дома", "Товары для геймеров");
    public static final String URL = "https://shop.kz/";

    private static final long ONE_SECOND_MS = 1000L;
    private static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
    private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
    private static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
    private static final long ONE_WEEK_MS = 7 * ONE_DAY_MS;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MainGroupRepository mainGroupRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Value("${shopkz.api.chunk-size}")
    private Integer chunkSize;
    @Value("${shopkz.thread-pool.pool-size}")
    private Integer threadPoolSize;

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
                        LOG.info("\tКатегория  {}", categoryText);
                        if (!categoryRepository.existsByUrl(sectionUrl)) {
                            categoryRepository.save(new Category(categoryText, categoryLink, group));
                        }
                    }
                }
            }
        }
    }

    @Scheduled(initialDelay = 1200, fixedDelay = ONE_WEEK_MS)
    @Transactional
    public void getItemInfo() throws InterruptedException {
        LOG.info("Получаем дополнитульную информацию о товарe...");
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        int page = 0;
        List<Category> categories;

        // 1. offset + limit
        // 2. page + pageSize
        //   offset = page * pageSize;  limit = pageSize;
        while (!(categories = categoryRepository.getChunk(PageRequest.of(page++, chunkSize))).isEmpty()) {
            LOG.info("Получили из базы {} категорий", categories.size());
            CountDownLatch latch = new CountDownLatch(categories.size());
            for (Category category : categories) {
                executorService.execute(new ItemsUpdateTask(itemRepository, category, latch));
            }
            LOG.info("Задачи запущены, ожидаем завершения выполнения...");
            latch.await();
            LOG.info("Задачи выполнены, следующая порция...");
        }
        executorService.shutdown();
    }
}

