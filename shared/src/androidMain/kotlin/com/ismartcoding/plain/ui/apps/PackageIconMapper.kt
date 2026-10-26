package com.ismartcoding.plain.ui.apps

import android.graphics.drawable.Drawable
import coil3.map.Mapper
import coil3.request.Options
import com.ismartcoding.plain.models.Package as AppPackage
import com.ismartcoding.plain.platform.system.getApplicationIcon

internal class PackageIconMapper : Mapper<AppPackage, Drawable> {
    override fun map(data: AppPackage, options: Options): Drawable? = getApplicationIcon(data.id)
}
