package ru.tech.imageresizershrinker.data.keys

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Keys {
    val SAVE_FOLDER_URI = stringPreferencesKey("saveFolder")
    val NIGHT_MODE = intPreferencesKey("nightMode")
    val DYNAMIC_COLORS = booleanPreferencesKey("dynamicColors")
    val ALLOW_IMAGE_MONET = booleanPreferencesKey("imageMonet")
    val AMOLED_MODE = booleanPreferencesKey("amoledMode")
    val APP_COLOR_TUPLE = stringPreferencesKey("appColorTuple")
    val BORDER_WIDTH = floatPreferencesKey("borderWidth")
    val PRESETS = stringPreferencesKey("presets")
    val COLOR_TUPLES = stringPreferencesKey("color_tuples")
    val FAB_ALIGNMENT = intPreferencesKey("alignment")
    val SHOW_UPDATE_DIALOG = booleanPreferencesKey("showDialog")
    val FILENAME_PREFIX = stringPreferencesKey("filename")
    val SELECTED_EMOJI_INDEX = intPreferencesKey("emoji")
    val ADD_SIZE_TO_FILENAME = booleanPreferencesKey("add_size")
    val IMAGE_PICKER_MODE = intPreferencesKey("picker_mode")
    val SCREEN_ORDER = stringPreferencesKey("order")
    val EMOJI_COUNT = intPreferencesKey("em_count")
    val ADD_ORIGINAL_NAME_TO_FILENAME = booleanPreferencesKey("ADD_ORIGINAL_NAME")
    val ADD_SEQ_NUM_TO_FILENAME = booleanPreferencesKey("ADD_SEQ_NUM")
    val AUTO_CACHE_CLEAR = booleanPreferencesKey("auto_clear")
    val GROUP_OPTIONS_BY_TYPE = booleanPreferencesKey("group_options")
    val RANDOMIZE_FILENAME = booleanPreferencesKey("rand-filename")
    val SELECTED_FONT_INDEX = intPreferencesKey("font")
    val FONT_SCALE = floatPreferencesKey("font_scale")
    val ALLOW_CRASHLYTICS = booleanPreferencesKey("allow_crashlytics")
    val ALLOW_ANALYTICS = booleanPreferencesKey("allow_analytics")
    val ALLOW_BETAS = booleanPreferencesKey("allow_betas")
    val ALLOW_SHADOWS_INSTEAD_OF_BORDERS = booleanPreferencesKey("ALLOW_SHADOWS_INSTEAD_OF_BORDERS")
    val APP_OPEN_COUNT = intPreferencesKey("APP_OPEN_COUNT")
    val LOCK_DRAW_ORIENTATION = booleanPreferencesKey("LOCK_DRAW_ORIENTATION")
}