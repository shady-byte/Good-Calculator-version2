package com.example.calculator.Constants

import net.objecthunter.exp4j.ExpressionBuilder
import java.math.RoundingMode
import java.text.DecimalFormat

object GlobalFunctions {
    private val numSymbols = mapOf<String,String>("0" to "-----","1" to ".----",
        "2" to "..---","3" to "...--",
        "4" to "....-","5" to ".....",
        "6" to "-....","7" to "--...",
        "8" to "---..","9" to "----.")

    private val operations = listOf<String>("+","-","*","/")

    fun calculateEquation(equation: String) : String {
        val df = DecimalFormat("#.###")
        df.roundingMode = RoundingMode.UP
        val result = ExpressionBuilder(equation).build().evaluate()
        val calc = df.format(result)
        return calc.toString()
    }

    fun getNumber(symbol: String) : String {
        var newSymbol = symbol
        var value = ""
        if(symbol.startsWith(".") && symbol.isNotEmpty()) {
            for(i in symbol.length..4) {
                newSymbol+="-"
            }
        }else if(symbol.startsWith("-") && symbol.isNotEmpty()){
            for(i in symbol.length..4) {
                newSymbol+="."
            }
        }
        //val x = numSymbols.filter { (key,value) -> value.contentEquals(newSymbol)}
        val key = numSymbols.filter { it.value == newSymbol }.keys
        if (key.isNotEmpty())
            value = key.first()
        return value
    }

    fun getSymbol(num: String) : String? {
        return numSymbols[num]
    }

    fun checkNotOperation(operation: Char) : Boolean{
        for (element in operations)  {
            if(element == operation.toString()) {
                return false
            }
        }
        return true
    }
}