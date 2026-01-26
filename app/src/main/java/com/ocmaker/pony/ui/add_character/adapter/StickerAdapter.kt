package com.ocmaker.pony.ui.add_character.adapter

import com.ocmaker.pony.core.base.BaseAdapter
import com.ocmaker.pony.core.extensions.loadImage
import com.ocmaker.pony.core.extensions.loadImageSticker
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.data.model.SelectedModel
import com.ocmaker.pony.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImageSticker(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}