import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class FileUtils {
	public static Iterable<String> readlines(String filename) throws IOException {
		final FileReader fr = new FileReader(filename);
		final BufferedReader br = new BufferedReader(fr);

		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					String line = this.getLine();

					String getLine() {
						String line = null;
						try {
							line = br.readLine();
						} catch (IOException ioEx) {
							line = null;
						}
						return line;
					}

					@Override
					public boolean hasNext() {
						return this.line != null;
					}

					@Override
					public String next() {
						String retval = this.line;
						this.line = this.getLine();
						return retval;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();

					}
				};

			}

		};

	}
}