package org.ginsim.epilog.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class EpilogFileFilter extends FileFilter {

	private String ext;

	public EpilogFileFilter(String extension) {
		this.ext = extension;
	}

	@Override
	public boolean accept(File f) {
		if (f == null) {
			return false;
		}

		if (f.isDirectory()) {
			return f.canRead();
		}

		String extension = f.getAbsolutePath();
		int pos = extension.lastIndexOf('.');
		if (pos < 0) {
			return false;
		}
		extension = extension.substring(pos + 1);

		if (this.ext.equals(extension))
			return true;

		return false;
	}

	@Override
	public String getDescription() {
		return this.ext.toUpperCase() + " files (." + this.ext + ")";
	}
}