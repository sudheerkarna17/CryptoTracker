package com.plcoding.cryptotracker.crypto.presentation.coin_list

import com.plcoding.cryptotracker.crypto.presentation.model.CoinUi

sealed interface CoinListAction {
    data class onCoinClick(val coinUi:CoinUi):CoinListAction
    data object OnRefresh:CoinListAction
}