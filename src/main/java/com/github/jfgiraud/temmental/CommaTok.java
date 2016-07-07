package com.github.jfgiraud.temmental;

class CommaTok extends Token {

    CommaTok(Cursor cursor) {
        super(',', cursor.clone());
    }

}
