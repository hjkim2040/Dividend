package zerobase.dividend.persist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zerobase.dividend.model.Company;

@Getter
@NoArgsConstructor
@Entity(name = "COMPANY")
@ToString
public class CompanyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ticker;
    private String name;

    public CompanyEntity(Company company) {
        this.ticker = company.getTicker();
        this.name = company.getName();
    }

}
