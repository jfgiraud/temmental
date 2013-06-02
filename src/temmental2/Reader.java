package temmental2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reader {
	protected List operations = new ArrayList<>();
	
	protected Reader(List operations) {
		this.operations = new ArrayList<>(operations);
	}
	
	public List getOperations() {
		return operations;
	}

}
