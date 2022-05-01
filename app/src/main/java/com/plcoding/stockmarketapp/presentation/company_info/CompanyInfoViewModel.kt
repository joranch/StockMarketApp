package com.plcoding.stockmarketapp.presentation.company_info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StockRepository
) : ViewModel() {

    var state by mutableStateOf(CompanyInfoState())

    init {
        viewModelScope.launch {
            val symbol = savedStateHandle.get<String>("symbol") ?: return@launch
            state = state.copy(isLoading = true)
            val companyInfoResult = async { repository.getCompanyInfo(symbol = symbol) }
            val intradayInfoResult = async { repository.getIntradayInfo(symbol = symbol) }

            handleCompanyInfoResult(companyInfoResult.await())
            handleIntradayInfoResult(intradayInfoResult.await())
        }
    }

    private fun handleIntradayInfoResult(result: Resource<List<IntradayInfo>>) {
        when (result) {
            is Resource.Success -> {
                state = state.copy(
                    stockInfo = result.data ?: emptyList(),
                    isLoading = false,
                    error = ""
                )
            }
            is Resource.Error -> {
                state = state.copy(
                    company = null,
                    isLoading = false,
                    error = result.message ?: "Error fetching company info"
                )
            }
            else -> Unit
        }
    }

    private fun handleCompanyInfoResult(result: Resource<CompanyInfo>) {
        when (result) {
            is Resource.Success -> {
                state = state.copy(
                    company = result.data,
                    isLoading = false,
                    error = ""
                )
            }
            is Resource.Error -> {
                state = state.copy(
                    company = null,
                    isLoading = false,
                    error = result.message ?: "Error fetching company info"
                )
            }
            else -> Unit
        }
    }
}