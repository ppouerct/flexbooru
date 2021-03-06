/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.repository.comment

import onlymash.flexbooru.api.ApiUrlHelper
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.entity.CommentAction
import onlymash.flexbooru.entity.CommentDan
import onlymash.flexbooru.repository.BasePageKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

//Danbooru comment data source
class CommentDanDataSource(private val danbooruApi: DanbooruApi,
                           private val commentAction: CommentAction,
                           retryExecutor: Executor
) : BasePageKeyedDataSource<Int, CommentDan>(retryExecutor){

    override fun loadInitialRequest(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CommentDan>) {
        val scheme = commentAction.scheme
        val host = commentAction.host
        val url = if (commentAction.post_id > 0) {
            ApiUrlHelper.getDanPostCommentUrl(commentAction, 1)
        } else {
            ApiUrlHelper.getDanPostsCommentUrl(commentAction, 1)
        }
        val response = danbooruApi.getComments(url).execute()
        val data = response.body() ?: mutableListOf()
        val comments: MutableList<CommentDan> = mutableListOf()
        data.forEach {
            it.scheme = scheme
            it.host = host
            if (!it.is_deleted) {
                comments.add(it)
            }
        }
        if (data.size < commentAction.limit) {
            callback.onResult(comments, null, null)
        } else {
            callback.onResult(comments, null, 2)
        }
    }

    override fun loadAfterRequest(params: LoadParams<Int>, callback: LoadCallback<Int, CommentDan>) {
        val page = params.key
        val url = if (commentAction.post_id > 0) {
            ApiUrlHelper.getDanPostCommentUrl(commentAction, page)
        } else {
            ApiUrlHelper.getDanPostsCommentUrl(commentAction, page)
        }
        danbooruApi.getComments(url).enqueue(object : retrofit2.Callback<MutableList<CommentDan>> {
            override fun onFailure(call: Call<MutableList<CommentDan>>, t: Throwable) {
                loadAfterOnFailed(t.message ?: "unknown err", params, callback)
            }
            override fun onResponse(call: Call<MutableList<CommentDan>>, response: Response<MutableList<CommentDan>>) {
                if (response.isSuccessful) {
                    val data = response.body() ?: mutableListOf()
                    val scheme = commentAction.scheme
                    val host = commentAction.host
                    val comments: MutableList<CommentDan> = mutableListOf()
                    data.forEach {
                        it.scheme = scheme
                        it.host = host
                        if (!it.is_deleted) {
                            comments.add(it)
                        }
                    }
                    loadAfterOnSuccess()
                    if (data.size < commentAction.limit) {
                        callback.onResult(comments, null)
                    } else {
                        callback.onResult(comments, page + 1)
                    }
                } else {
                    loadAfterOnFailed("error code: ${response.code()}", params, callback)
                }
            }
        })
    }
}