package zerobase.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;
import zerobase.dividend.scraper.Scraper;

import java.util.List;

import static zerobase.dividend.model.constants.CacheKey.KEY_FINANCE;

@Component
@AllArgsConstructor
@Slf4j
public class ScraperScheduler {
    private final CompanyRepository companyRepository;
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;

    @CacheEvict(value = KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {

        List<CompanyEntity> companies = this.companyRepository.findAll();

        for (CompanyEntity company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                    Company.builder()
                            .name(company.getName())
                            .ticker(company.getTicker())
                            .build()
            );
            scrapedResult.getDividendEntities().stream()
                    .map(e -> new DividendEntity(company.getId(), e))
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
