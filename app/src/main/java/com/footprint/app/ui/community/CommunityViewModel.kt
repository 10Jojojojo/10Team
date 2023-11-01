package com.footprint.app.ui.community

import androidx.lifecycle.ViewModel
import com.footprint.app.api.model.PostModel
import com.footprint.app.api.model.TagModel

class CommunityViewModel : ViewModel() {

    val dummyTag = listOf(
        TagModel(tag = "Hot"),
        TagModel(tag = "전체 게시글"),
        TagModel(tag = "훈련 꿀팁"),
        TagModel(tag = "정보 교류")
    )
    val dummyPost = listOf(
        PostModel(
            nickname = "user1",
            postDate = "2023-01-01",
            title = "Morning walk with my pet",
            content = "Had a wonderful morning walk with my pet today!",
            likesCount = 120,
            commentsCount = 15
        ),
        PostModel(
            nickname = "user2",
            postDate = "2023-01-05",
            title = "Pet's birthday party",
            content = "Celebrated my pet's 5th birthday. So much fun!",
            likesCount = 85,
            commentsCount = 20
        ),
        PostModel(
            nickname = "user3",
            postDate = "2023-01-10",
            title = "Pet's new toy",
            content = "Bought a new toy for my pet. He loves it!",
            likesCount = 60,
            commentsCount = 8
        ),
        PostModel(
            nickname = "user4",
            postDate = "2023-01-15",
            title = "At the pet park",
            content = "Spent the day at the pet park. Met so many lovely pets.",
            likesCount = 110,
            commentsCount = 10
        ),
        PostModel(
            nickname = "user5",
            postDate = "2023-01-20",
            title = "Pet's new dress",
            content = "Got a new dress for my pet. She looks adorable!",
            likesCount = 95,
            commentsCount = 12
        ),
        PostModel(
            nickname = "user6",
            postDate = "2023-01-25",
            title = "Pet's health checkup",
            content = "Went for a regular health checkup for my pet. All good!",
            likesCount = 80,
            commentsCount = 5
        )
    )
}