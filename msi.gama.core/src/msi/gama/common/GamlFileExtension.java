package msi.gama.common;

public class GamlFileExtension {

	public final static String GAML_FILE = ".gaml";
	public final static String EXPERIMENT_FILE = ".experiment";
	public final static String MODEL_FILE = ".model";
	public final static String SPECIES_FILE = ".species";

	public static final boolean isGaml(final String fileName) {
		return fileName != null && fileName.endsWith(GAML_FILE);
	}

	public static final boolean isExperiment(final String fileName) {
		return fileName != null && fileName.endsWith(EXPERIMENT_FILE);
	}

	public static final boolean isModel(final String fileName) {
		return fileName != null && fileName.endsWith(MODEL_FILE);
	}

	public static final boolean isSpecies(final String fileName) {
		return fileName != null && fileName.endsWith(SPECIES_FILE);
	}

	public static final boolean isAny(final String fileName) {
		return isGaml(fileName) || isExperiment(fileName) || isModel(fileName) || isSpecies(fileName);
	}
}
