package com.plcoding.stockmarketapp.data.repository

import com.plcoding.stockmarketapp.data.csv.CompanyListingsParser
import com.plcoding.stockmarketapp.data.csv.CsvParser
import com.plcoding.stockmarketapp.data.local.StockDatabase
import com.plcoding.stockmarketapp.data.mapper.toCompanyInfo
import com.plcoding.stockmarketapp.data.mapper.toCompanyListing
import com.plcoding.stockmarketapp.data.mapper.toCompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.StockApi
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CsvParser<CompanyListing>,
    private val intradayInfoParser: CsvParser<IntradayInfo>
): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))

            val localListings = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListings.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListings.isEmpty() && query.isNullOrBlank()
            val shouldLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(shouldLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }

            val remoteListing = try {
                val response = api.getListings()
                companyListingsParser.parse(response.byteStream())
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Failed to parse data from api"))
                null
            } catch (e: HttpException){
                e.printStackTrace()
                emit(Resource.Error("Failed to fetch data from api"))
                null
            }

            remoteListing?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity()}
                )
                emit(Resource.Success(
                    data = dao.searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }

        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val result = api.getIntradayInfo(symbol)
            val data = intradayInfoParser.parse(result.byteStream())
            Resource.Success(data)
        } catch (e: IOException) {
            e.printStackTrace()
            return Resource.Error("Couldn't load intrday info from api")
        }catch (e: HttpException) {
            e.printStackTrace()
            return Resource.Error("Couldn't fetch intrday info from api")
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try {
            val response = api.getCompanyInfo(symbol)
            Resource.Success(response.toCompanyInfo())
        }catch (e: IOException) {
            e.printStackTrace()
            return Resource.Error("Couldn't load company info from api")
        }catch (e: HttpException) {
            e.printStackTrace()
            return Resource.Error("Couldn't fetch company info from api")
        }
    }

}