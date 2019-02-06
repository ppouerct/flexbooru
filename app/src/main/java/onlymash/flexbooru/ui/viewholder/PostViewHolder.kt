package onlymash.flexbooru.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.Placeholder
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe

class PostViewHolder(itemView: View,
                     private val glide: GlideRequests,
                     private val placeholder: Placeholder): RecyclerView.ViewHolder(itemView){

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, placeholder: Placeholder): PostViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post, parent, false)
            return PostViewHolder(view, glide, placeholder)
        }
    }

    private val preview: ImageView = itemView.findViewById(R.id.preview)
    private val previewCard: CardView = itemView.findViewById(R.id.preview_card)
    private var post: Any? = null

    private var itemListener: ItemListener? = null

    init {
        preview.setOnClickListener {
            when (post) {
                is PostDan -> itemListener?.onClickDanItem(post as PostDan)
                is PostMoe -> itemListener?.onClickMoeItem(post as PostMoe)
            }
        }
    }

    fun bind(post: Any?) {
        when (post) {
            is PostDan -> {
                this.post = post
                val placeholderDrawable = when (post.rating) {
                    "s" -> placeholder.s
                    "q" -> placeholder.q
                    else -> placeholder.e
                }
                val lp = previewCard.layoutParams as ConstraintLayout.LayoutParams
                val ratio = post.image_width.toFloat()/post.image_height.toFloat()
                when {
                    ratio > Constants.MAX_ITEM_ASPECT_RATIO -> {
                        lp.dimensionRatio = "H, ${Constants.MAX_ITEM_ASPECT_RATIO}:1"
                    }
                    ratio < Constants.MIN_ITEM_ASPECT_RATIO -> {
                        lp.dimensionRatio = "H, ${Constants.MIN_ITEM_ASPECT_RATIO}:1"
                    }
                    else -> {
                        lp.dimensionRatio = "H, $ratio:1"
                    }
                }
                previewCard.layoutParams = lp
                glide.load(post.preview_file_url)
                    .placeholder(placeholderDrawable)
                    .centerCrop()
                    .into(preview)

            }
            is PostMoe -> {
                this.post = post
                val placeholderDrawable = when (post.rating) {
                    "s" -> placeholder.s
                    "q" -> placeholder.q
                    else -> placeholder.e
                }
                val lp = previewCard.layoutParams as ConstraintLayout.LayoutParams
                val ratio = post.width.toFloat()/post.height.toFloat()
                when {
                    ratio > Constants.MAX_ITEM_ASPECT_RATIO -> {
                        lp.dimensionRatio = "H, ${Constants.MAX_ITEM_ASPECT_RATIO}:1"
                    }
                    ratio < Constants.MIN_ITEM_ASPECT_RATIO -> {
                        lp.dimensionRatio = "H, ${Constants.MIN_ITEM_ASPECT_RATIO}:1"
                    }
                    else -> {
                        lp.dimensionRatio = "H, $ratio:1"
                    }
                }
                previewCard.layoutParams = lp
                glide.load(post.preview_url)
                    .placeholder(placeholderDrawable)
                    .centerCrop()
                    .into(preview)
            }
        }
    }

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickDanItem(post: PostDan)
        fun onClickMoeItem(post: PostMoe)
    }
}