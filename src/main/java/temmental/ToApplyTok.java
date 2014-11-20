package temmental;


class ToApplyTok extends Token {

    ToApplyTok(Cursor cursor) {
        super(':', cursor.clone());
    }

}
