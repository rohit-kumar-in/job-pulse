package com.jobpulse.company.mapper;

import com.jobpulse.company.dto.CompanyDTO;
import com.jobpulse.company.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyDTO toDTO(Company company) {
        if (company == null) return null;
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .website(company.getWebsite())
                .build();
    }

    public Company toEntity(CompanyDTO dto) {
        if (dto == null) return null;
        return Company.builder()
                .id(dto.getId())
                .name(dto.getName())
                .website(dto.getWebsite())
                .build();
    }
}
