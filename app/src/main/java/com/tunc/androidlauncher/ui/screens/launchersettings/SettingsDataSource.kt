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
                    id = "language",
                    titleResId = R.string.settings_item_language,
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
                )
            )
        )
    )
}