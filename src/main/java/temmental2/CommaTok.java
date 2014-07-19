package temmental2;

class CommaTok extends Token {

    CommaTok(Cursor cursor) {
        super(',', cursor.clone());
    }

}
