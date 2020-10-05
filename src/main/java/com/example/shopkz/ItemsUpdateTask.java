package com.example.shopkz;


import com.example.shopkz.model.Category;
import com.example.shopkz.model.Item;
import com.example.shopkz.repository.ItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.shopkz.Parser.URL;


public class ItemsUpdateTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ItemsUpdateTask.class);

    private final ItemRepository itemRepository;
    private final Category category;
    private final CountDownLatch latch;


    private static final String PAGE_URL_CONSTANT = "filter/almaty-is-v_nalichii-or-ojidaem-or-dostavim/apply/?PAGEN_1=%d";
    private static final Pattern PRODUCT_NUMBER_PATTERN = Pattern.compile("Артикул:\\s+(\\d+)\\s+(.*)");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("(.*)(url)(\\()(\')(.*)(\')\\)");
    public ItemsUpdateTask(ItemRepository itemRepository, Category category, CountDownLatch latch) {
        this.itemRepository = itemRepository;
        this.category = category;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            String categoryUrl = category.getUrl();
            String firstPageUrl = String.format(categoryUrl + PAGE_URL_CONSTANT, 1);

            Document firstPage = Jsoup.connect(firstPageUrl)
                    .cookie("sectionSort", "new")
                    .get();
            int totalPages = getTotalPages(firstPage);
            parseItems(firstPage);
            for (int i = 2; i <= totalPages; i++) {
                LOG.info("Получаем список товаров - страница {}", i);
                parseItems(Jsoup.connect(String.format(categoryUrl + PAGE_URL_CONSTANT, i))
                        .cookie("sectionSort", "new")
                        .get());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    private int getTotalPages(Document firstPage) {
        Element lastPage = firstPage.selectFirst(".bx-pagination-container > ul > li:nth-last-of-type(2)>a");
        if (lastPage != null) {
            String text = lastPage.text();
            return Integer.parseInt(text);
        }
        return 0;
    }

    private void parseItems(Document itemPage) throws JsonProcessingException {

        Elements itemElements = itemPage.select(".bx_catalog_item");
        for (Element itemElement : itemElements) {


            Element itemContainer = itemElement.selectFirst(".bx_catalog_item_container");
            String itemData = itemContainer.attr("data-product");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(itemData);
            double itemPrice = jsonNode.get("price").asDouble();
            String itemText = jsonNode.get("name").asText();
            Integer itemCode = jsonNode.get("id").asInt();
            Element descriptionContainer = itemElement.selectFirst(".bx_catalog_item_articul");
            String itemDescription = null;
            String imageUrl = null;
            String itemImage = itemElement.select("a.bx_catalog_item_images").attr("style");
            if (descriptionContainer != null) {
                String descriptionText = descriptionContainer.text();
                Matcher matcher = PRODUCT_NUMBER_PATTERN.matcher(descriptionText);
                if (matcher.find()) {
                    itemDescription = matcher.group(2);
                }
                Pattern pattern = Pattern.compile("(.*\\:)");
                Matcher urlMatcher = pattern.matcher(URL);
                while (urlMatcher.find()) {
                    String http = urlMatcher.group(1);
                    Matcher imageMather = IMAGE_PATTERN.matcher(itemImage);
                    while (imageMather.find()) {
                        String str = imageMather.group(5);
                       imageUrl=http+str;
                   }
                }

                Item item = itemRepository.findOneByCode(itemCode).orElseGet(() -> new Item(itemCode));
                String itemLink = itemElement.selectFirst(".bx_catalog_item_title a").absUrl("href");
                item.setModel(itemText);
                item.setDescription(itemDescription);
                item.setPrice(itemPrice);
                item.setImage(imageUrl);
                item.setUrl(itemLink);
                item.setAvailable(itemElement.select(".product_available .availability.v_0").isEmpty());
                item.setCategory(category);
                itemRepository.save(item);
            }
        }
    }
}


