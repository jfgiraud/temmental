package com.github.jfgiraud.temmental;

class CommandTok extends Token {

    CommandTok(Cursor cursor) {
        super('#', cursor.clone());
    }

}
