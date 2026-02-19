import com.tunc.androidlauncher.R
import com.tunc.androidlauncher.ui.screens.launchersettings.models.SettingModel
import com.tunc.androidlauncher.ui.screens.launchersettings.models.SettingsTitleModel

object SettingsDataSource {

    val menuData: List<SettingsTitleModel> = listOf(
        SettingsTitleModel(
            id = "appearance",
            titleResId = R.string.settings_section_appearance,
            settings = listOf(
                SettingModel(
                    id = "theme",
                    titleResId = R.string.settings_item_theme,
                ),
                SettingModel(
                    id = "layout",
                    titleResId = R.string.settings_item_grid_size,
                ),
                SettingModel(
                    id = "icons",
                    titleResId = R.string.settings_item_icons,
                ),
                SettingModel(
                    id = "font",
                    titleResId = R.string.settings_item_font,
                )
            )
        ),

        SettingsTitleModel(
            id = "gestures",
            titleResId = R.string.settings_section_gestures,
            settings = listOf(
                SettingModel(
                    id = "double_tap",
                    titleResId = R.string.settings_item_double_tap,
                ),
                SettingModel(
                    id = "swipe_up",
                    titleResId = R.string.settings_item_swipe_up,
                ),
                SettingModel(
                    id = "swipe_down",
                    titleResId = R.string.settings_item_swipe_down,
                )
            )
        ),

        SettingsTitleModel(
            id = "privacy",
            titleResId = R.string.settings_section_privacy,
            settings = listOf(
                SettingModel(
                    id = "hidden_apps",
                    titleResId = R.string.settings_item_hidden_apps,
                ),
                SettingModel(
                    id = "app_lock",
                    titleResId = R.string.settings_item_app_lock
                ),
                SettingModel(
                    id = "secure_vault",
                    titleResId = R.string.settings_item_secure_vault,
                )
            )
        ),

        SettingsTitleModel(
            id = "system",
            titleResId = R.string.settings_section_system,
            settings = listOf(
                SettingModel(
                    id = "backup",
                    titleResId = R.string.settings_item_backup,
                ),
                SettingModel(
                    id = "about",
                    titleResId = R.string.settings_item_about,
                ),
                SettingModel(
                    id = "restart",
                    titleResId = R.string.settings_item_restart,
                )
            )
        )
    )
}