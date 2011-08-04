package qbql.parser;

public class CykCell implements Cell {
	int[] content = null;
	
	public CykCell( int[] content ) {
		this.content = content;
	}

	public int getSymbol( int index ) {
		return content[index];
	}

	public int getRule( int index ) {
		throw new AssertionError();
	}

	public int getPosition(int index) {
		throw new AssertionError();
	}

	public int size() {
		return content.length;
	}

	public int[] getContent() {
		return content;
	}
}

