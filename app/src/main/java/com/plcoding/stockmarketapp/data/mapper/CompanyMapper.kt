package com.plcoding.stockmarketapp.data.mapper

import com.plcoding.stockmarketapp.data.local.CompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.dto.CompanyInfoDto
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing

fun CompanyListingEntity.toCompanyListing() : CompanyListing {
    return CompanyListing(
        name = name,
        exchange = exchange,
        symbol = symbol
    )
}

fun CompanyListing.toCompanyListingEntity() : CompanyListingEntity {
    return CompanyListingEntity(
        name = name,
        exchange = exchange,
        symbol = symbol
    )
}

fun CompanyInfoDto.toCompanyInfo(): CompanyInfo {
    return CompanyInfo(
        symbol = symbol ?: "",
        name = name ?: "",
        country = country ?: "",
        description = description ?: "",
        industry = industry ?: ""
    )
}