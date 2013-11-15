package temmental2;

class CommandTok extends Token {

    CommandTok(Cursor cursor) {
        super('#', cursor.clone());
    }

}
