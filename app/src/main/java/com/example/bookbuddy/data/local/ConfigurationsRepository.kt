package com.example.bookbuddy.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ConfigurationsRepository(private val datastore: DataStore<Preferences>){

    companion object{
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val FONT_SIZE = intPreferencesKey("font_size")
    }
    val fontSize: Flow<Int> = datastore.data.catch {
        if(it is IOException){
            emit(emptyPreferences())
        } else{
            throw it
        }
    }.map {preferences->
        preferences[FONT_SIZE]?:56
    }

    val isDarkTheme: Flow<Boolean> = datastore.data.catch {
        if(it is IOException){
            emit(emptyPreferences())
        } else{
            throw it
        }
    }.map {preferences->
        preferences[IS_DARK_THEME]?:false
    }

    suspend fun updateFontSize(fontSize: Int){
        datastore.edit{preferences->
            preferences[FONT_SIZE] = fontSize
        }
    }

    suspend fun updateTheme(isDarkTheme: Boolean){
        datastore.edit { preferences->
            preferences[IS_DARK_THEME] = isDarkTheme
        }
    }
}