package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import zerobase.dividend.exception.impl.NoCompanyException;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;

import java.util.List;
import java.util.stream.Collectors;

import static zerobase.dividend.model.constants.CacheKey.KEY_FINANCE;

@Service
@AllArgsConstructor
@Slf4j
public class FinanceService {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    @Cacheable(key = "#companyName", value = KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(NoCompanyException::new);
        List<DividendEntity> dividendEntities = this.dividendRepository
                .findAllByCompanyId(company.getId());
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> Dividend.builder()
                        .date(e.getDate())
                        .dividend(e.getDividend())
                        .build())
                .collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
                .ticker(company.getTicker())
                .name(company.getName())
                .build(),dividends);
    }
}
