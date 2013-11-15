package temmental2;


class ToApplyTok extends Token {

	ToApplyTok(Cursor cursor) {
		super(':', cursor.clone());
	}
	
}
