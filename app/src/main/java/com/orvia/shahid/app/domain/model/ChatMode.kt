package com.orvia.shahid.app.domain.model

enum class ChatMode(val displayNameRes: Int) {
    ASK_ANYTHING(com.orvia.shahid.app.R.string.mode_ask_anything),
    BUILDER(com.orvia.shahid.app.R.string.mode_builder),
    MASTER_PROMPT(com.orvia.shahid.app.R.string.mode_master_prompt)
}
