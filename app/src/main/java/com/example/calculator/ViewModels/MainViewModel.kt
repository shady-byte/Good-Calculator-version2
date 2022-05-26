package com.example.calculator.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private var result = MutableLiveData<String>("")
    private var symbol = MutableLiveData<String>("")
    private var number = MutableLiveData<String>("")
    private var shaked = MutableLiveData<Boolean>(false)
    private var finalResult = ""
    //setters
    fun setResult(value: String) {
        val oldResult = result.value.toString()
        result.postValue(oldResult + value)
    }
    fun setFinalResult(value: String) {
        result.postValue(value)
        finalResult = value
    }
    fun getFinalResult() : String {
       return finalResult
    }
    fun setSymbol(value: String) {
        val oldSymbol = symbol.value.toString()
        if(oldSymbol.length <=4)
            symbol.postValue(oldSymbol + value)
        else
            symbol.postValue("")
    }
    fun setNumber(value: String) {
        number.postValue(value)
    }
    fun setShacked(value: Boolean) {
        shaked.postValue(value)
    }



    //getters
    fun getResult() : LiveData<String> = result
    fun getSymbol() : LiveData<String> = symbol
    fun getNumber() : LiveData<String> = number
    fun getShaked() : LiveData<Boolean> = shaked

    fun clearSymbolAndNumber() {
        symbol.postValue("")
        number .postValue("")
    }
    fun clearResult() {
        result.postValue("")
    }

}