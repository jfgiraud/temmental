package temmental;

class CommandTok extends Token {

    CommandTok(Cursor cursor) {
        super('#', cursor.clone());
    }

}
